package org.nhernandez.webapp.ferreteria.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketVenta {
    private Long id;
    private String folio;
    private String usernameVendedor;
    private LocalDateTime fechaVenta;
    private int total;
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
}
