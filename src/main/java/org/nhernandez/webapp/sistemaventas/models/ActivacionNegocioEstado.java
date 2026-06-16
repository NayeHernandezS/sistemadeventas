package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;

public class ActivacionNegocioEstado implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalProductos;
    private int totalVentas;
    private boolean catalogoListo;
    private boolean primeraVentaRegistrada;
    private boolean onboardingCompletado;

    public boolean isActivacionCompleta() {
        return primeraVentaRegistrada;
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(int totalProductos) {
        this.totalProductos = totalProductos;
    }

    public int getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(int totalVentas) {
        this.totalVentas = totalVentas;
    }

    public boolean isCatalogoListo() {
        return catalogoListo;
    }

    public void setCatalogoListo(boolean catalogoListo) {
        this.catalogoListo = catalogoListo;
    }

    public boolean isPrimeraVentaRegistrada() {
        return primeraVentaRegistrada;
    }

    public void setPrimeraVentaRegistrada(boolean primeraVentaRegistrada) {
        this.primeraVentaRegistrada = primeraVentaRegistrada;
    }

    public boolean isOnboardingCompletado() {
        return onboardingCompletado;
    }

    public void setOnboardingCompletado(boolean onboardingCompletado) {
        this.onboardingCompletado = onboardingCompletado;
    }
}
