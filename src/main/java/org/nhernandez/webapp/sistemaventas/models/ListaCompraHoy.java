package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListaCompraHoy implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fecha;
    private int stockMinimo;
    private int totalProductos;
    private int totalUnidadesSugeridas;
    private int costoEstimadoTotal;
    private List<ProductoCompraSugerida> productos = new ArrayList<>();

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(int totalProductos) {
        this.totalProductos = totalProductos;
    }

    public int getTotalUnidadesSugeridas() {
        return totalUnidadesSugeridas;
    }

    public void setTotalUnidadesSugeridas(int totalUnidadesSugeridas) {
        this.totalUnidadesSugeridas = totalUnidadesSugeridas;
    }

    public int getCostoEstimadoTotal() {
        return costoEstimadoTotal;
    }

    public void setCostoEstimadoTotal(int costoEstimadoTotal) {
        this.costoEstimadoTotal = costoEstimadoTotal;
    }

    public List<ProductoCompraSugerida> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoCompraSugerida> productos) {
        this.productos = productos != null ? productos : new ArrayList<>();
    }
}
