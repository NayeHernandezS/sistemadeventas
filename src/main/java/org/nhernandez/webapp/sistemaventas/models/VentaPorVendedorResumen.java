package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;

public class VentaPorVendedorResumen implements Serializable {

    private static final long serialVersionUID = 1L;

    private String vendedor;
    private int cantidadTickets;
    private int totalNeto;

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public int getCantidadTickets() {
        return cantidadTickets;
    }

    public void setCantidadTickets(int cantidadTickets) {
        this.cantidadTickets = cantidadTickets;
    }

    public int getTotalNeto() {
        return totalNeto;
    }

    public void setTotalNeto(int totalNeto) {
        this.totalNeto = totalNeto;
    }
}
