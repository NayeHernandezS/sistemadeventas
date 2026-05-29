package org.nhernandez.webapp.sistemaventas.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Devolucion {

    private Long id;
    private String folio;
    private Long ticketId;
    private String ticketFolio;
    private String tenantOwner;
    private String usernameRegistro;
    private LocalDateTime fechaDevolucion;
    private String motivo;
    private int totalDevuelto;
    private List<DevolucionItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketFolio() {
        return ticketFolio;
    }

    public void setTicketFolio(String ticketFolio) {
        this.ticketFolio = ticketFolio;
    }

    public String getTenantOwner() {
        return tenantOwner;
    }

    public void setTenantOwner(String tenantOwner) {
        this.tenantOwner = tenantOwner;
    }

    public String getUsernameRegistro() {
        return usernameRegistro;
    }

    public void setUsernameRegistro(String usernameRegistro) {
        this.usernameRegistro = usernameRegistro;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public int getTotalDevuelto() {
        return totalDevuelto;
    }

    public void setTotalDevuelto(int totalDevuelto) {
        this.totalDevuelto = totalDevuelto;
    }

    public List<DevolucionItem> getItems() {
        return items;
    }

    public void setItems(List<DevolucionItem> items) {
        this.items = items;
    }
}
