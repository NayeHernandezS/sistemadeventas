package org.nhernandez.webapp.sistemaventas.pagos.mercadopago;

import com.fasterxml.jackson.databind.JsonNode;
import org.nhernandez.webapp.sistemaventas.config.MercadoPagoProperties;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.services.RenovacionAutomaticaService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class MercadoPagoCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoCheckoutService.class);
    public static final String REF_PREFIX = "pago_";

    private final MercadoPagoProperties properties;
    private final MercadoPagoApiClient apiClient;
    private final SuscripcionService suscripcionService;
    private final RenovacionAutomaticaService renovacionAutomaticaService;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    public MercadoPagoCheckoutService(MercadoPagoProperties properties,
                                      MercadoPagoApiClient apiClient,
                                      SuscripcionService suscripcionService,
                                      @Lazy RenovacionAutomaticaService renovacionAutomaticaService) {
        this.properties = properties;
        this.apiClient = apiClient;
        this.suscripcionService = suscripcionService;
        this.renovacionAutomaticaService = renovacionAutomaticaService;
    }

    public boolean habilitado() {
        if (properties.getEnabled() != null) {
            return properties.getEnabled() && tokenPresente();
        }
        return tokenPresente();
    }

    public String iniciarCheckout(String username, int meses, String planCodigo, String baseUrlSolicitud) {
        if (!habilitado()) {
            throw new ServiceJdbcException(
                    "Mercado Pago no esta configurado. Agrega MERCADOPAGO_ACCESS_TOKEN en .env o solicita pago manual.",
                    null);
        }
        String base = resolverBaseUrl(baseUrlSolicitud);
        String initPoint = suscripcionService.iniciarPagoMercadoPago(username, meses, planCodigo, base);
        log.info("Checkout Mercado Pago iniciado para tenant={} plan={} meses={}", username, planCodigo, meses);
        return initPoint;
    }

    public void procesarNotificacion(String topic, String id, JsonNode cuerpoWebhook) {
        if (esTopicPreapproval(topic)) {
            String preapprovalId = resolverPreapprovalId(topic, id, cuerpoWebhook);
            if (preapprovalId != null) {
                renovacionAutomaticaService.procesarPreapprovalPorId(preapprovalId);
            }
            return;
        }
        Long paymentId = resolverPaymentId(topic, id, cuerpoWebhook);
        if (paymentId == null) {
            return;
        }
        procesarPagoPorId(paymentId);
    }

    public void procesarPagoPorId(long paymentId) {
        if (!habilitado()) {
            return;
        }
        Optional<MercadoPagoApiClient.PagoMercadoPago> pagoMp = apiClient.consultarPago(paymentId);
        pagoMp.ifPresent(this::aplicarSiAprobado);
    }

    /**
     * Respaldo en local sin webhook: consulta MP por cada pago PENDIENTE del tenant
     * y confirma si ya fue aprobado (p. ej. el usuario volvio con el boton Atras del navegador).
     */
    public int sincronizarPagosPendientesDelTenant(String tenant) {
        if (!habilitado() || tenant == null || tenant.isBlank()) {
            return 0;
        }
        var pendientes = suscripcionService.pagosPendientesDelTenant(tenant).stream()
                .filter(p -> "MERCADOPAGO".equalsIgnoreCase(p.getCanal()) && p.getId() != null)
                .toList();
        if (pendientes.isEmpty()) {
            return 0;
        }
        try {
            for (PagoSuscripcion pago : pendientes) {
                String ref = REF_PREFIX + pago.getId();
                for (MercadoPagoApiClient.PagoMercadoPago pagoMp : apiClient.buscarPagosPorReferenciaExterna(ref)) {
                    if ("approved".equalsIgnoreCase(pagoMp.status())) {
                        aplicarSiAprobado(pagoMp);
                        break;
                    }
                }
            }
        } catch (MercadoPagoException e) {
            log.warn("No se pudo sincronizar pagos MP para tenant={}: {}", tenant, e.getMessage());
            return 0;
        }
        long mpPendientesDespues = suscripcionService.pagosPendientesDelTenant(tenant).stream()
                .filter(p -> "MERCADOPAGO".equalsIgnoreCase(p.getCanal()))
                .count();
        int confirmados = pendientes.size() - (int) mpPendientesDespues;
        if (confirmados > 0) {
            log.info("Sincronizados {} pago(s) MP aprobados para tenant={}", confirmados, tenant);
        }
        return confirmados;
    }

    /** Mercado Pago no registro ningun intento de pago para este checkout (checkout abandonado). */
    public boolean checkoutSinPagoEnMercadoPago(PagoSuscripcion pago) {
        if (!habilitado() || pago == null || pago.getId() == null
                || !"MERCADOPAGO".equalsIgnoreCase(pago.getCanal())) {
            return false;
        }
        try {
            return apiClient.buscarPagosPorReferenciaExterna(REF_PREFIX + pago.getId()).isEmpty();
        } catch (MercadoPagoException e) {
            log.warn("No se pudo verificar checkout MP para pago {}: {}", pago.getId(), e.getMessage());
            return false;
        }
    }

    private void aplicarSiAprobado(MercadoPagoApiClient.PagoMercadoPago pagoMp) {
        if (!"approved".equalsIgnoreCase(pagoMp.status())) {
            log.debug("Pago MP {} en estado {}, sin confirmar suscripcion", pagoMp.paymentId(), pagoMp.status());
            return;
        }
        if (RenovacionAutomaticaService.parsearReferencia(pagoMp.externalReference()).isPresent()
                || (pagoMp.preapprovalId() != null && !pagoMp.preapprovalId().isBlank())) {
            renovacionAutomaticaService.aplicarPagoRecurrente(pagoMp);
            return;
        }
        Long pagoId = parsearReferenciaInterna(pagoMp.externalReference());
        if (pagoId == null) {
            log.warn("Pago MP {} sin external_reference valida: {}", pagoMp.paymentId(), pagoMp.externalReference());
            return;
        }
        suscripcionService.confirmarPagoMercadoPago(pagoId, pagoMp.paymentId(), pagoMp.transactionAmount(),
                pagoMp.currencyId());
    }

    private static boolean esTopicPreapproval(String topic) {
        if (topic == null || topic.isBlank()) {
            return false;
        }
        String t = topic.trim().toLowerCase();
        return t.contains("preapproval") || t.contains("subscription");
    }

    private static String resolverPreapprovalId(String topic, String id, JsonNode cuerpo) {
        if (id != null && !id.isBlank() && esTopicPreapproval(topic)) {
            return id.trim();
        }
        if (cuerpo != null) {
            JsonNode data = cuerpo.get("data");
            if (data != null && data.has("id")) {
                return data.get("id").asText();
            }
        }
        return null;
    }

    private Long resolverPaymentId(String topic, String id, JsonNode cuerpo) {
        if (id != null && !id.isBlank()) {
            if (topic == null || topic.isBlank() || "payment".equalsIgnoreCase(topic)) {
                try {
                    return Long.parseLong(id.trim());
                } catch (NumberFormatException ignored) {
                    // continuar con cuerpo JSON
                }
            }
        }
        if (cuerpo == null) {
            return null;
        }
        JsonNode data = cuerpo.get("data");
        if (data != null && data.has("id")) {
            String dataId = data.get("id").asText();
            try {
                return Long.parseLong(dataId);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public static Long parsearReferenciaInterna(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            return null;
        }
        String ref = externalReference.trim();
        if (ref.startsWith(REF_PREFIX)) {
            try {
                return Long.parseLong(ref.substring(REF_PREFIX.length()));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        try {
            return Long.parseLong(ref);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean montosCoinciden(BigDecimal esperado, BigDecimal recibido) {
        if (esperado == null || recibido == null) {
            return false;
        }
        return esperado.setScale(2, RoundingMode.HALF_UP)
                .compareTo(recibido.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    private String resolverBaseUrl(String baseUrlSolicitud) {
        try {
            return MercadoPagoUrls.resolverBase(appBaseUrl, baseUrlSolicitud);
        } catch (IllegalArgumentException e) {
            throw new ServiceJdbcException(
                    "Configura APP_BASE_URL con HTTPS real o prueba desde http://localhost:8080 sin dominio falso.",
                    null);
        }
    }

    private boolean tokenPresente() {
        return tokenConfigurado(properties.getAccessToken());
    }

    /**
     * Token real de MP es largo (TEST-... o APP_USR-...). Rechaza placeholders del .env.example.
     */
    public static boolean tokenConfigurado(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String t = token.trim();
        if (t.equalsIgnoreCase("TEST-tu-token") || t.contains("tu-token") || t.contains("tu_token")) {
            return false;
        }
        boolean prefijoValido = t.startsWith("TEST-") || t.startsWith("APP_USR-") || t.startsWith("APP_USR");
        return prefijoValido && t.length() >= 30;
    }
}
