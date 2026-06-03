package org.nhernandez.webapp.sistemaventas.models;

public class PreferenciasTenant {

    private String tenantUsername;
    private Integer stockMinimo;
    private boolean onboardingCompletado;
    private String logoFilename;

    public String getTenantUsername() {
        return tenantUsername;
    }

    public void setTenantUsername(String tenantUsername) {
        this.tenantUsername = tenantUsername;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isOnboardingCompletado() {
        return onboardingCompletado;
    }

    public void setOnboardingCompletado(boolean onboardingCompletado) {
        this.onboardingCompletado = onboardingCompletado;
    }

    public String getLogoFilename() {
        return logoFilename;
    }

    public void setLogoFilename(String logoFilename) {
        this.logoFilename = logoFilename;
    }
}
