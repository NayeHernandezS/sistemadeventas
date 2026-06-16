package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CierreCajaDia implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fecha;
    private boolean esAdmin;
    private String vendedorFiltro;
    private int cantidadTickets;
    private int totalBruto;
    private int totalDevuelto;
    private int totalNeto;
    private int cantidadTicketsAyer;
    private int totalNetoAyer;
    private List<TicketVenta> tickets = new ArrayList<>();
    private Map<Long, Integer> devueltoPorTicketId = new HashMap<>();
    private List<ProductoVentaRanking> topProductos = new ArrayList<>();
    private List<VentaPorVendedorResumen> ventasPorVendedor = new ArrayList<>();

    public int totalNeto(TicketVenta ticket) {
        if (ticket == null || ticket.getId() == null) {
            return 0;
        }
        int devuelto = devueltoPorTicketId.getOrDefault(ticket.getId(), 0);
        return Math.max(0, ticket.getTotal() - devuelto);
    }

    public int totalDevuelto(TicketVenta ticket) {
        if (ticket == null || ticket.getId() == null) {
            return 0;
        }
        return devueltoPorTicketId.getOrDefault(ticket.getId(), 0);
    }

    public int getDiferenciaNetoAyer() {
        return totalNeto - totalNetoAyer;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public boolean isEsAdmin() {
        return esAdmin;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    public String getVendedorFiltro() {
        return vendedorFiltro;
    }

    public void setVendedorFiltro(String vendedorFiltro) {
        this.vendedorFiltro = vendedorFiltro;
    }

    public int getCantidadTickets() {
        return cantidadTickets;
    }

    public void setCantidadTickets(int cantidadTickets) {
        this.cantidadTickets = cantidadTickets;
    }

    public int getTotalBruto() {
        return totalBruto;
    }

    public void setTotalBruto(int totalBruto) {
        this.totalBruto = totalBruto;
    }

    public int getTotalDevuelto() {
        return totalDevuelto;
    }

    public void setTotalDevuelto(int totalDevuelto) {
        this.totalDevuelto = totalDevuelto;
    }

    public int getTotalNeto() {
        return totalNeto;
    }

    public void setTotalNeto(int totalNeto) {
        this.totalNeto = totalNeto;
    }

    public int getCantidadTicketsAyer() {
        return cantidadTicketsAyer;
    }

    public void setCantidadTicketsAyer(int cantidadTicketsAyer) {
        this.cantidadTicketsAyer = cantidadTicketsAyer;
    }

    public int getTotalNetoAyer() {
        return totalNetoAyer;
    }

    public void setTotalNetoAyer(int totalNetoAyer) {
        this.totalNetoAyer = totalNetoAyer;
    }

    public List<TicketVenta> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketVenta> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
    }

    public Map<Long, Integer> getDevueltoPorTicketId() {
        return devueltoPorTicketId;
    }

    public void setDevueltoPorTicketId(Map<Long, Integer> devueltoPorTicketId) {
        this.devueltoPorTicketId = devueltoPorTicketId != null ? devueltoPorTicketId : new HashMap<>();
    }

    public List<ProductoVentaRanking> getTopProductos() {
        return topProductos;
    }

    public void setTopProductos(List<ProductoVentaRanking> topProductos) {
        this.topProductos = topProductos != null ? topProductos : new ArrayList<>();
    }

    public List<VentaPorVendedorResumen> getVentasPorVendedor() {
        return ventasPorVendedor;
    }

    public void setVentasPorVendedor(List<VentaPorVendedorResumen> ventasPorVendedor) {
        this.ventasPorVendedor = ventasPorVendedor != null ? ventasPorVendedor : new ArrayList<>();
    }
}
