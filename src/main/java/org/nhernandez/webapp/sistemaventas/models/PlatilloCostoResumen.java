package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;

public class PlatilloCostoResumen implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productoId;
    private String nombre;
    private String categoria;
    private int precioVenta;
    private int costoReceta;
    private int margenPesos;
    private int margenPorcentaje;
    private int cantidadLineas;
    private boolean tieneReceta;

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(int precioVenta) {
        this.precioVenta = precioVenta;
    }

    public int getCostoReceta() {
        return costoReceta;
    }

    public void setCostoReceta(int costoReceta) {
        this.costoReceta = costoReceta;
    }

    public int getMargenPesos() {
        return margenPesos;
    }

    public void setMargenPesos(int margenPesos) {
        this.margenPesos = margenPesos;
    }

    public int getMargenPorcentaje() {
        return margenPorcentaje;
    }

    public void setMargenPorcentaje(int margenPorcentaje) {
        this.margenPorcentaje = margenPorcentaje;
    }

    public int getCantidadLineas() {
        return cantidadLineas;
    }

    public void setCantidadLineas(int cantidadLineas) {
        this.cantidadLineas = cantidadLineas;
    }

    public boolean isTieneReceta() {
        return tieneReceta;
    }

    public void setTieneReceta(boolean tieneReceta) {
        this.tieneReceta = tieneReceta;
    }

    /** Expuesto para EL/JSP ({@code ${r.tieneReceta}}). */
    public boolean getTieneReceta() {
        return tieneReceta;
    }

    public boolean tieneCostoCalculable() {
        return tieneReceta && costoReceta > 0 && precioVenta > 0;
    }

    /** Expuesto para EL/JSP ({@code ${r.tieneCostoCalculable}}). */
    public boolean getTieneCostoCalculable() {
        return tieneCostoCalculable();
    }
}
