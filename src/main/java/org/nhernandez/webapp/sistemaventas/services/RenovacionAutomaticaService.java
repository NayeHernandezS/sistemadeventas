package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoApiClient;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoCheckoutService;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoException;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoUrls;
import org.nhernandez.webapp.sistemaventas.repositories.SuscripcionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

/**
 * Renovacion automatica mensual via Mercado Pago Preapproval (Suscripciones).
 */
@Service
public class RenovacionAutomaticaService {

    private static final Logger log = LoggerFactory.getLogger(RenovacionAutomaticaService.class);
    public static final String AUTO_REF_PREFIX = "auto_";
    private static final String REF_SEP = "__";

    private final MercadoPagoCheckoutService mercadoPagoCheckoutService;
    private final MercadoPagoApiClient mercadoPagoApiClient;
    private final SuscripcionRepository suscripcionRepository;
    private final SuscripcionService suscripcionService;
    private final PlanLimiteService planLimiteService;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    public RenovacionAutomaticaService(MercadoPagoCheckoutService mercadoPagoCheckoutService,
                                       MercadoPagoApiClient mercadoPagoApiClient,
                                       SuscripcionRepository suscripcionRepository,
                                       SuscripcionService suscripcionService,
                                       PlanLimiteService planLimiteService) {
        this.mercadoPagoCheckoutService = mercadoPagoCheckoutService;
        this.mercadoPagoApiClient = mercadoPagoApiClient;
        this.suscripcionRepository = suscripcionRepository;
        this.suscripcionService = suscripcionService;
        this.planLimiteService = planLimiteService;
    }

    public boolean disponible() {
        return mercadoPagoCheckoutService.habilitado();
    }

    public String iniciarActivacion(String username, String planCodigo, String baseUrlPublica, String payerEmail) {
        if (!disponible()) {
            throw new ServiceJdbcException(
                    "Mercado Pago no esta configurado para renovacion automatica.", null);
        }
        PlanSuscripcion plan = PlanSuscripcion.porCodigo(planCodigo)
                .orElseThrow(() -> new ServiceJdbcException("Plan no valido", null));
        planLimiteService.validarPlanContratable(username, plan.getCodigo());

        String backUrl = MercadoPagoUrls.resolverUrlRetorno(
                appBaseUrl, baseUrlPublica, "/suscripcion/auto-renovar-exito");
        if (backUrl == null) {
            throw new ServiceJdbcException(MercadoPagoUrls.MENSAJE_BACK_URL_PREAPPROVAL, null);
        }
        String ref = construirReferencia(username, plan.getCodigo());

        try {
            MercadoPagoApiClient.PreapprovalCreada creada = mercadoPagoApiClient.crearPreapproval(
                    new MercadoPagoApiClient.PreapprovalRequest(
                            plan.getNombre() + " - renovacion mensual",
                            plan.getPrecioMensual(),
                            ref,
                            backUrl,
                            MercadoPagoUrls.urlNotificacionOpcional(appBaseUrl, baseUrlPublica),
                            payerEmail
                    ));
            log.info("Preapproval MP iniciado tenant={} plan={} id={}", username, plan.getCodigo(),
                    creada.preapprovalId());
            return creada.initPoint();
        } catch (MercadoPagoException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    public void cancelar(String username) {
        try {
            Suscripcion s = suscripcionRepository.porUsername(username);
            if (s == null || !s.isRenovacionAutomatica() || s.getMpPreapprovalId() == null) {
                throw new ServiceJdbcException("No tienes renovacion automatica activa", null);
            }
            if (disponible()) {
                mercadoPagoApiClient.cancelarPreapproval(s.getMpPreapprovalId());
            }
            suscripcionRepository.desactivarRenovacionAutomatica(username);
            log.info("Renovacion automatica cancelada tenant={}", username);
        } catch (MercadoPagoException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    public void procesarPreapprovalPorId(String preapprovalId) {
        if (!disponible() || preapprovalId == null || preapprovalId.isBlank()) {
            return;
        }
        Optional<MercadoPagoApiClient.PreapprovalMercadoPago> preapp = mercadoPagoApiClient.consultarPreapproval(
                preapprovalId.trim());
        preapp.ifPresent(this::aplicarPreapprovalSiAutorizado);
    }

    public void aplicarPagoRecurrente(MercadoPagoApiClient.PagoMercadoPago pagoMp) {
        if (!"approved".equalsIgnoreCase(pagoMp.status())) {
            return;
        }
        Optional<AutoRef> ref = parsearReferencia(pagoMp.externalReference());
        if (ref.isPresent()) {
            extenderMesRenovacion(ref.get().username(), ref.get().planCodigo(), pagoMp.transactionAmount());
            return;
        }
        if (pagoMp.preapprovalId() != null && !pagoMp.preapprovalId().isBlank()) {
            try {
                Suscripcion s = suscripcionRepository.porPreapprovalId(pagoMp.preapprovalId());
                if (s != null) {
                    extenderMesRenovacion(s.getUsername(), s.getPlanCodigo(), pagoMp.transactionAmount());
                }
            } catch (SQLException e) {
                throw new ServiceJdbcException(e.getMessage(), e);
            }
        }
    }

    private void aplicarPreapprovalSiAutorizado(MercadoPagoApiClient.PreapprovalMercadoPago preapp) {
        if (!"authorized".equalsIgnoreCase(preapp.status())) {
            log.debug("Preapproval {} en estado {}", preapp.preapprovalId(), preapp.status());
            return;
        }
        Optional<AutoRef> ref = parsearReferencia(preapp.externalReference());
        if (ref.isEmpty()) {
            log.warn("Preapproval {} sin referencia valida: {}", preapp.preapprovalId(), preapp.externalReference());
            return;
        }
        try {
            suscripcionRepository.activarRenovacionAutomatica(
                    ref.get().username(), ref.get().planCodigo(), preapp.preapprovalId());
            suscripcionService.extenderSuscripcionMeses(ref.get().username(), 1);
            log.info("Renovacion automatica activada tenant={} plan={}", ref.get().username(), ref.get().planCodigo());
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private void extenderMesRenovacion(String username, String planCodigo, BigDecimal montoRecibido) {
        BigDecimal esperado = suscripcionService.precioPorMes(planCodigo);
        if (!MercadoPagoCheckoutService.montosCoinciden(esperado, montoRecibido)) {
            log.warn("Pago recurrente MP monto inesperado tenant={} esperado={} recibido={}",
                    username, esperado, montoRecibido);
            return;
        }
        suscripcionService.extenderSuscripcionMeses(username, 1);
        log.info("Suscripcion extendida 1 mes por cobro recurrente MP tenant={}", username);
    }

    public static String construirReferencia(String username, String planCodigo) {
        return AUTO_REF_PREFIX + username.trim() + REF_SEP + planCodigo.trim().toUpperCase(Locale.ROOT);
    }

    public static Optional<AutoRef> parsearReferencia(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            return Optional.empty();
        }
        String ref = externalReference.trim();
        if (!ref.startsWith(AUTO_REF_PREFIX)) {
            return Optional.empty();
        }
        String cuerpo = ref.substring(AUTO_REF_PREFIX.length());
        int sep = cuerpo.indexOf(REF_SEP);
        if (sep <= 0 || sep + REF_SEP.length() >= cuerpo.length()) {
            return Optional.empty();
        }
        String username = cuerpo.substring(0, sep);
        String plan = cuerpo.substring(sep + REF_SEP.length());
        if (username.isBlank() || plan.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new AutoRef(username, plan.toUpperCase(Locale.ROOT)));
    }

    public record AutoRef(String username, String planCodigo) {
    }
}
