package org.nhernandez.webapp.sistemaventas.models;

import java.time.LocalDateTime;

public class Suscripcion {

    private Long id;
    private String username;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private boolean enPeriodoPrueba;
    private String estado;
    private String planCodigo = "EMPRENDEDOR";
    private boolean renovacionAutomatica;
    private String mpPreapprovalId;

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

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isEnPeriodoPrueba() {
        return enPeriodoPrueba;
    }

    public void setEnPeriodoPrueba(boolean enPeriodoPrueba) {
        this.enPeriodoPrueba = enPeriodoPrueba;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean estaVigente() {
        return fechaFin != null && !LocalDateTime.now().isAfter(fechaFin);
    }

    public boolean estaSuspendida() {
        return estado != null && "SUSPENDIDA".equalsIgnoreCase(estado.trim());
    }

    /** Acceso al sistema: vigente por fecha y no suspendida por plataforma. */
    public boolean permiteAcceso() {
        return estaVigente() && !estaSuspendida();
    }

    public String getPlanCodigo() {
        return planCodigo;
    }

    public void setPlanCodigo(String planCodigo) {
        this.planCodigo = planCodigo;
    }

    public boolean isRenovacionAutomatica() {
        return renovacionAutomatica;
    }

    public void setRenovacionAutomatica(boolean renovacionAutomatica) {
        this.renovacionAutomatica = renovacionAutomatica;
    }

    public String getMpPreapprovalId() {
        return mpPreapprovalId;
    }

    public void setMpPreapprovalId(String mpPreapprovalId) {
        this.mpPreapprovalId = mpPreapprovalId;
    }
}
