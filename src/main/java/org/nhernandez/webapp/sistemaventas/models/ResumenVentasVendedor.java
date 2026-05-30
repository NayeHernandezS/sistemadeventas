package org.nhernandez.webapp.sistemaventas.models;

public class ResumenVentasVendedor {

    private int cantidadTickets;
    private long totalImporte;

    public ResumenVentasVendedor() {
    }

    public ResumenVentasVendedor(int cantidadTickets, long totalImporte) {
        this.cantidadTickets = cantidadTickets;
        this.totalImporte = totalImporte;
    }

    public int getCantidadTickets() {
        return cantidadTickets;
    }

    public void setCantidadTickets(int cantidadTickets) {
        this.cantidadTickets = cantidadTickets;
    }

    public long getTotalImporte() {
        return totalImporte;
    }

    public void setTotalImporte(long totalImporte) {
        this.totalImporte = totalImporte;
    }
}
