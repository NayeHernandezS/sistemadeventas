package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CitaServicio implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String tenantOwner;
    private Long productoId;
    private Long clienteId;
    private LocalDateTime fechaHora;
    private int duracionMinutos = 30;
    private EstadoCita estado = EstadoCita.PROGRAMADA;
    private String notas;
    private String usernameRegistro;
    private LocalDateTime fechaRegistro;
    private Long ticketId;

    private String servicioNombre;
    private String clienteNombre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantOwner() {
        return tenantOwner;
    }

    public void setTenantOwner(String tenantOwner) {
        this.tenantOwner = tenantOwner;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public EstadoCita getEstado() {
        return estado != null ? estado : EstadoCita.PROGRAMADA;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado != null ? estado : EstadoCita.PROGRAMADA;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getUsernameRegistro() {
        return usernameRegistro;
    }

    public void setUsernameRegistro(String usernameRegistro) {
        this.usernameRegistro = usernameRegistro;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getServicioNombre() {
        return servicioNombre;
    }

    public void setServicioNombre(String servicioNombre) {
        this.servicioNombre = servicioNombre;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public boolean isEditable() {
        return getEstado() != EstadoCita.COMPLETADA && getEstado() != EstadoCita.CANCELADA;
    }

    public LocalDateTime getFechaHoraFin() {
        if (fechaHora == null) {
            return null;
        }
        return fechaHora.plusMinutes(Math.max(duracionMinutos, 1));
    }
}
