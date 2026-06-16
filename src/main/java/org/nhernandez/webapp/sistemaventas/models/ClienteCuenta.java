package org.nhernandez.webapp.sistemaventas.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vista de una cuenta de negocio (tenant ADMIN) para el panel de plataforma.
 */
public class ClienteCuenta {

    private Long id;
    private String username;
    private String email;
    private String tipoNegocio;
    private int cantidadVendedores;
    private LocalDateTime fechaFinSuscripcion;
    private boolean enPeriodoPrueba;
    private String estadoSuscripcion;
    private boolean vigente;
    private String planCodigo;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime ultimoAccesoNegocio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(String tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
    }

    public int getCantidadVendedores() {
        return cantidadVendedores;
    }

    public void setCantidadVendedores(int cantidadVendedores) {
        this.cantidadVendedores = cantidadVendedores;
    }

    public LocalDateTime getFechaFinSuscripcion() {
        return fechaFinSuscripcion;
    }

    public void setFechaFinSuscripcion(LocalDateTime fechaFinSuscripcion) {
        this.fechaFinSuscripcion = fechaFinSuscripcion;
    }

    public boolean isEnPeriodoPrueba() {
        return enPeriodoPrueba;
    }

    public void setEnPeriodoPrueba(boolean enPeriodoPrueba) {
        this.enPeriodoPrueba = enPeriodoPrueba;
    }

    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(String estadoSuscripcion) {
        this.estadoSuscripcion = estadoSuscripcion;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }

    public String getPlanCodigo() {
        return planCodigo;
    }

    public void setPlanCodigo(String planCodigo) {
        this.planCodigo = planCodigo;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public LocalDateTime getUltimoAccesoNegocio() {
        return ultimoAccesoNegocio;
    }

    public void setUltimoAccesoNegocio(LocalDateTime ultimoAccesoNegocio) {
        this.ultimoAccesoNegocio = ultimoAccesoNegocio;
    }

    public boolean ultimoAccesoNegocioEsHoy() {
        return ultimoAccesoNegocio != null && ultimoAccesoNegocio.toLocalDate().equals(LocalDate.now());
    }
}
