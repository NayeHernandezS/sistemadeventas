package org.nhernandez.webapp.sistemaventas.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoSuscripcion {

    private Long id;
    private String username;
    private int meses;
    private BigDecimal monto;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaConfirmacion;
    private String estado;
    private String notas;
    private String mpPreferenceId;
    private String mpPaymentId;
    private String canal = "MANUAL";
    private String planCodigo = "EMPRENDEDOR";

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

    public int getMeses() {
        return meses;
    }

    public void setMeses(int meses) {
        this.meses = meses;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getPlanCodigo() {
        return planCodigo;
    }

    public void setPlanCodigo(String planCodigo) {
        this.planCodigo = planCodigo;
    }

    public String getMpPreferenceId() {
        return mpPreferenceId;
    }

    public void setMpPreferenceId(String mpPreferenceId) {
        this.mpPreferenceId = mpPreferenceId;
    }

    public String getMpPaymentId() {
        return mpPaymentId;
    }

    public void setMpPaymentId(String mpPaymentId) {
        this.mpPaymentId = mpPaymentId;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }
}
