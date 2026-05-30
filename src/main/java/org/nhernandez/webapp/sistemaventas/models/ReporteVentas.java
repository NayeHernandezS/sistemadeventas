package org.nhernandez.webapp.sistemaventas.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resultado agregado para la vista de reportes de ventas.
 */
public class ReporteVentas {

    private List<TicketVenta> ticketsFiltrados;
    private List<TicketVenta> ticketsDia;
    private List<TicketVenta> ticketsSemana;
    private List<TicketVenta> ticketsMes;
    private Map<Long, Integer> devueltoPorTicketId = new HashMap<>();
    private Set<String> vendedores;
    private String vendedorSeleccionado;
    private String fechaInicio;
    private String fechaFin;
    private int cantidadFiltrada;
    private int totalFiltradoBruto;
    private int totalFiltradoNeto;
    private int totalDevueltoFiltrado;
    private int cantidadDia;
    private int totalDiaBruto;
    private int totalDiaNeto;
    private int cantidadSemana;
    private int totalSemanaBruto;
    private int totalSemanaNeto;
    private int cantidadMes;
    private int totalMesBruto;
    private int totalMesNeto;

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

    public List<TicketVenta> getTicketsFiltrados() {
        return ticketsFiltrados;
    }

    public void setTicketsFiltrados(List<TicketVenta> ticketsFiltrados) {
        this.ticketsFiltrados = ticketsFiltrados;
    }

    public List<TicketVenta> getTicketsDia() {
        return ticketsDia;
    }

    public void setTicketsDia(List<TicketVenta> ticketsDia) {
        this.ticketsDia = ticketsDia;
    }

    public List<TicketVenta> getTicketsSemana() {
        return ticketsSemana;
    }

    public void setTicketsSemana(List<TicketVenta> ticketsSemana) {
        this.ticketsSemana = ticketsSemana;
    }

    public List<TicketVenta> getTicketsMes() {
        return ticketsMes;
    }

    public void setTicketsMes(List<TicketVenta> ticketsMes) {
        this.ticketsMes = ticketsMes;
    }

    public Map<Long, Integer> getDevueltoPorTicketId() {
        return devueltoPorTicketId;
    }

    public void setDevueltoPorTicketId(Map<Long, Integer> devueltoPorTicketId) {
        this.devueltoPorTicketId = devueltoPorTicketId != null ? devueltoPorTicketId : new HashMap<>();
    }

    public Set<String> getVendedores() {
        return vendedores;
    }

    public void setVendedores(Set<String> vendedores) {
        this.vendedores = vendedores;
    }

    public String getVendedorSeleccionado() {
        return vendedorSeleccionado;
    }

    public void setVendedorSeleccionado(String vendedorSeleccionado) {
        this.vendedorSeleccionado = vendedorSeleccionado;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getCantidadFiltrada() {
        return cantidadFiltrada;
    }

    public void setCantidadFiltrada(int cantidadFiltrada) {
        this.cantidadFiltrada = cantidadFiltrada;
    }

    public int getTotalFiltradoBruto() {
        return totalFiltradoBruto;
    }

    public void setTotalFiltradoBruto(int totalFiltradoBruto) {
        this.totalFiltradoBruto = totalFiltradoBruto;
    }

    public int getTotalFiltradoNeto() {
        return totalFiltradoNeto;
    }

    public void setTotalFiltradoNeto(int totalFiltradoNeto) {
        this.totalFiltradoNeto = totalFiltradoNeto;
    }

    public int getTotalDevueltoFiltrado() {
        return totalDevueltoFiltrado;
    }

    public void setTotalDevueltoFiltrado(int totalDevueltoFiltrado) {
        this.totalDevueltoFiltrado = totalDevueltoFiltrado;
    }

    public int getCantidadDia() {
        return cantidadDia;
    }

    public void setCantidadDia(int cantidadDia) {
        this.cantidadDia = cantidadDia;
    }

    public int getTotalDiaBruto() {
        return totalDiaBruto;
    }

    public void setTotalDiaBruto(int totalDiaBruto) {
        this.totalDiaBruto = totalDiaBruto;
    }

    public int getTotalDiaNeto() {
        return totalDiaNeto;
    }

    public void setTotalDiaNeto(int totalDiaNeto) {
        this.totalDiaNeto = totalDiaNeto;
    }

    public int getCantidadSemana() {
        return cantidadSemana;
    }

    public void setCantidadSemana(int cantidadSemana) {
        this.cantidadSemana = cantidadSemana;
    }

    public int getTotalSemanaBruto() {
        return totalSemanaBruto;
    }

    public void setTotalSemanaBruto(int totalSemanaBruto) {
        this.totalSemanaBruto = totalSemanaBruto;
    }

    public int getTotalSemanaNeto() {
        return totalSemanaNeto;
    }

    public void setTotalSemanaNeto(int totalSemanaNeto) {
        this.totalSemanaNeto = totalSemanaNeto;
    }

    public int getCantidadMes() {
        return cantidadMes;
    }

    public void setCantidadMes(int cantidadMes) {
        this.cantidadMes = cantidadMes;
    }

    public int getTotalMesBruto() {
        return totalMesBruto;
    }

    public void setTotalMesBruto(int totalMesBruto) {
        this.totalMesBruto = totalMesBruto;
    }

    public int getTotalMesNeto() {
        return totalMesNeto;
    }

    public void setTotalMesNeto(int totalMesNeto) {
        this.totalMesNeto = totalMesNeto;
    }
}
