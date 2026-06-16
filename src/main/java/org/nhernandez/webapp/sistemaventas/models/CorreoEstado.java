package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Diagnostico de SMTP para correos transaccionales (suscripcion, recuperacion). */
public class CorreoEstado implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean smtpConfigurado;
    private boolean remitenteConfigurado;
    private boolean baseUrlHttps;
    private String smtpHost;
    private String mailFrom;
    private String appBaseUrl;
    private List<String> advertencias = new ArrayList<>();

    public boolean isSmtpConfigurado() {
        return smtpConfigurado;
    }

    public void setSmtpConfigurado(boolean smtpConfigurado) {
        this.smtpConfigurado = smtpConfigurado;
    }

    public boolean isRemitenteConfigurado() {
        return remitenteConfigurado;
    }

    public void setRemitenteConfigurado(boolean remitenteConfigurado) {
        this.remitenteConfigurado = remitenteConfigurado;
    }

    public boolean isBaseUrlHttps() {
        return baseUrlHttps;
    }

    public void setBaseUrlHttps(boolean baseUrlHttps) {
        this.baseUrlHttps = baseUrlHttps;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getAppBaseUrl() {
        return appBaseUrl;
    }

    public void setAppBaseUrl(String appBaseUrl) {
        this.appBaseUrl = appBaseUrl;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }

    public void setAdvertencias(List<String> advertencias) {
        this.advertencias = advertencias != null ? advertencias : new ArrayList<>();
    }

    public boolean isListoProduccion() {
        return smtpConfigurado && remitenteConfigurado && baseUrlHttps && advertencias.isEmpty();
    }
}
