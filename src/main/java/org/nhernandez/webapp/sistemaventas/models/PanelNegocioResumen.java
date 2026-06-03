package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PanelNegocioResumen implements Serializable {

    private static final long serialVersionUID = 1L;

    private int ticketsHoy;
    private long netoHoy;
    private int ticketsSemana;
    private long netoSemana;
    private int ticketsMes;
    private long netoMes;
    private List<ProductoVentaRanking> topProductosSemana = new ArrayList<>();

    public int getTicketsHoy() {
        return ticketsHoy;
    }

    public void setTicketsHoy(int ticketsHoy) {
        this.ticketsHoy = ticketsHoy;
    }

    public long getNetoHoy() {
        return netoHoy;
    }

    public void setNetoHoy(long netoHoy) {
        this.netoHoy = netoHoy;
    }

    public int getTicketsSemana() {
        return ticketsSemana;
    }

    public void setTicketsSemana(int ticketsSemana) {
        this.ticketsSemana = ticketsSemana;
    }

    public long getNetoSemana() {
        return netoSemana;
    }

    public void setNetoSemana(long netoSemana) {
        this.netoSemana = netoSemana;
    }

    public int getTicketsMes() {
        return ticketsMes;
    }

    public void setTicketsMes(int ticketsMes) {
        this.ticketsMes = ticketsMes;
    }

    public long getNetoMes() {
        return netoMes;
    }

    public void setNetoMes(long netoMes) {
        this.netoMes = netoMes;
    }

    public List<ProductoVentaRanking> getTopProductosSemana() {
        return topProductosSemana;
    }

    public void setTopProductosSemana(List<ProductoVentaRanking> topProductosSemana) {
        this.topProductosSemana = topProductosSemana != null ? topProductosSemana : new ArrayList<>();
    }
}
