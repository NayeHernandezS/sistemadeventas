package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.config.MercadoPagoProperties;
import org.nhernandez.webapp.sistemaventas.models.MercadoPagoEstado;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoCheckoutService;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoUrls;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoProduccionService {

    private final MercadoPagoProperties properties;
    private final MercadoPagoCheckoutService checkoutService;
    private final PagoSuscripcionExpiracionService expiracionService;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    public MercadoPagoProduccionService(MercadoPagoProperties properties,
                                        MercadoPagoCheckoutService checkoutService,
                                        PagoSuscripcionExpiracionService expiracionService) {
        this.properties = properties;
        this.checkoutService = checkoutService;
        this.expiracionService = expiracionService;
    }

    public MercadoPagoEstado evaluar() {
        MercadoPagoEstado estado = new MercadoPagoEstado();
        estado.setDiasExpiracionPendiente(expiracionService.getDiasMercadoPago());

        boolean tokenValido = MercadoPagoCheckoutService.tokenConfigurado(properties.getAccessToken());
        estado.setTokenValido(tokenValido);
        estado.setHabilitado(checkoutService.habilitado());

        boolean secretOk = properties.getWebhookSecret() != null && !properties.getWebhookSecret().isBlank();
        estado.setWebhookSecretConfigurado(secretOk);

        String base = baseUrlEfectiva();
        String webhook = base != null ? MercadoPagoUrls.urlNotificacionOpcional(base, null) : null;
        estado.setWebhookUrl(webhook);
        estado.setWebhookUrlPublica(webhook != null);

        String urlExito = base != null ? base + "/suscripcion/pago-exitoso" : null;
        estado.setAutoReturnHttps(urlExito != null && MercadoPagoUrls.admiteAutoReturn(urlExito));

        List<String> advertencias = new ArrayList<>();
        if (tokenPresenteSinValidar()) {
            advertencias.add("MERCADOPAGO_ACCESS_TOKEN parece placeholder o invalido. Usa credenciales del panel de desarrolladores.");
        } else if (!tokenValido && estado.isHabilitado()) {
            advertencias.add("Mercado Pago esta forzado (MERCADOPAGO_ENABLED=true) pero el token no es valido.");
        }
        if (tokenValido && !secretOk) {
            advertencias.add("Configura MERCADOPAGO_WEBHOOK_SECRET en produccion para validar notificaciones.");
        }
        if (tokenValido && (base == null || !MercadoPagoUrls.admiteWebhook(base))) {
            advertencias.add("APP_BASE_URL debe ser HTTPS publico (sin localhost) para webhooks y retorno automatico.");
        }
        if (tokenValido && base != null && !estado.isAutoReturnHttps()) {
            advertencias.add("Mercado Pago no podra redirigir al sitio tras el pago hasta que APP_BASE_URL sea HTTPS valido.");
        }
        estado.setAdvertencias(advertencias);
        return estado;
    }

    private String baseUrlEfectiva() {
        if (appBaseUrl != null && !appBaseUrl.isBlank() && !MercadoPagoUrls.esPlaceholder(appBaseUrl)) {
            return MercadoPagoUrls.sinBarraFinal(appBaseUrl.trim());
        }
        return null;
    }

    private boolean tokenPresenteSinValidar() {
        String token = properties.getAccessToken();
        return token != null && !token.isBlank()
                && !MercadoPagoCheckoutService.tokenConfigurado(token);
    }
}
