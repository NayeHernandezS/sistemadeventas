package org.nhernandez.webapp.sistemaventas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercadopago")
public class MercadoPagoProperties {

    /**
     * Access Token de produccion o prueba (TEST-... / APP_USR-...).
     */
    private String accessToken = "";

    /**
     * Forzar activacion/desactivacion. Si vacio, se activa cuando hay access-token.
     */
    private Boolean enabled;

    private String currencyId = "MXN";

    /**
     * Secret de firma de webhooks (panel MP → Webhooks → Configurar). Vacio = no validar (solo desarrollo).
     */
    private String webhookSecret = "";

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }
}
