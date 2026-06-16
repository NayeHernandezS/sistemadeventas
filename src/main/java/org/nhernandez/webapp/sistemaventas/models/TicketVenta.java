package org.nhernandez.webapp.sistemaventas.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketVenta {
    private Long id;
    private String folio;
    private String usernameVendedor;
    private String tenantOwner;
    private LocalDateTime fechaVenta;
    private int total;
    /** ACTIVO, DEVUELTO_PARCIAL, DEVUELTO_TOTAL */
    private String estado = "ACTIVO";
    private String nombreCliente;
    private List<TicketItem> items = new ArrayList<>();

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

    public String getUsernameVendedor() {
        return usernameVendedor;
    }

    public void setUsernameVendedor(String usernameVendedor) {
        this.usernameVendedor = usernameVendedor;
    }

    public String getTenantOwner() {
        return tenantOwner;
    }

    public void setTenantOwner(String tenantOwner) {
        this.tenantOwner = tenantOwner;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<TicketItem> getItems() {
        return items;
    }

    public void setItems(List<TicketItem> items) {
        this.items = items;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public boolean tieneNombreCliente() {
        return nombreCliente != null && !nombreCliente.isBlank();
    }

    /** Expuesto para EL/JSP ({@code ${ticket.tieneNombreCliente}}). */
    public boolean getTieneNombreCliente() {
        return tieneNombreCliente();
    }

    public boolean permiteDevolucion() {
        return estado == null || "ACTIVO".equals(estado) || "DEVUELTO_PARCIAL".equals(estado);
    }
}
