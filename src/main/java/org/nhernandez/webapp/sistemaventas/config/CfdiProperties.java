package org.nhernandez.webapp.sistemaventas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cfdi")
public class CfdiProperties {

    private Boolean enabled;
    private String facturamaUsername = "";
    private String facturamaPassword = "";
    private boolean facturamaSandbox = true;

    public boolean habilitado() {
        if (enabled != null) {
            return enabled;
        }
        return facturamaUsername != null && !facturamaUsername.isBlank()
                && facturamaPassword != null && !facturamaPassword.isBlank();
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getFacturamaUsername() {
        return facturamaUsername;
    }

    public void setFacturamaUsername(String facturamaUsername) {
        this.facturamaUsername = facturamaUsername;
    }

    public String getFacturamaPassword() {
        return facturamaPassword;
    }

    public void setFacturamaPassword(String facturamaPassword) {
        this.facturamaPassword = facturamaPassword;
    }

    public boolean isFacturamaSandbox() {
        return facturamaSandbox;
    }

    public void setFacturamaSandbox(boolean facturamaSandbox) {
        this.facturamaSandbox = facturamaSandbox;
    }

    public String baseUrl() {
        return facturamaSandbox ? "https://apisandbox.facturama.mx" : "https://api.facturama.mx";
    }
}
