package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostico de configuracion Mercado Pago para operacion en produccion.
 */
public class MercadoPagoEstado implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean habilitado;
    private boolean tokenValido;
    private boolean webhookSecretConfigurado;
    private boolean webhookUrlPublica;
    private boolean autoReturnHttps;
    private String webhookUrl;
    private int diasExpiracionPendiente;
    private List<String> advertencias = new ArrayList<>();

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public boolean isTokenValido() {
        return tokenValido;
    }

    public void setTokenValido(boolean tokenValido) {
        this.tokenValido = tokenValido;
    }

    public boolean isWebhookSecretConfigurado() {
        return webhookSecretConfigurado;
    }

    public void setWebhookSecretConfigurado(boolean webhookSecretConfigurado) {
        this.webhookSecretConfigurado = webhookSecretConfigurado;
    }

    public boolean isWebhookUrlPublica() {
        return webhookUrlPublica;
    }

    public void setWebhookUrlPublica(boolean webhookUrlPublica) {
        this.webhookUrlPublica = webhookUrlPublica;
    }

    public boolean isAutoReturnHttps() {
        return autoReturnHttps;
    }

    public void setAutoReturnHttps(boolean autoReturnHttps) {
        this.autoReturnHttps = autoReturnHttps;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public int getDiasExpiracionPendiente() {
        return diasExpiracionPendiente;
    }

    public void setDiasExpiracionPendiente(int diasExpiracionPendiente) {
        this.diasExpiracionPendiente = diasExpiracionPendiente;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }

    public void setAdvertencias(List<String> advertencias) {
        this.advertencias = advertencias != null ? advertencias : new ArrayList<>();
    }

    public boolean isListoProduccion() {
        return habilitado && tokenValido && webhookSecretConfigurado
                && webhookUrlPublica && autoReturnHttps && advertencias.isEmpty();
    }
}
