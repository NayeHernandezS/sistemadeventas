package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;
import java.math.BigDecimal;

public class RecetaLinea implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long recetaId;
    private Long insumoProductoId;
    private String insumoNombre;
    private BigDecimal cantidad = BigDecimal.ONE;
    private String unidad = "pza";
    private int costoLinea;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecetaId() {
        return recetaId;
    }

    public void setRecetaId(Long recetaId) {
        this.recetaId = recetaId;
    }

    public Long getInsumoProductoId() {
        return insumoProductoId;
    }

    public void setInsumoProductoId(Long insumoProductoId) {
        this.insumoProductoId = insumoProductoId;
    }

    public String getInsumoNombre() {
        return insumoNombre;
    }

    public void setInsumoNombre(String insumoNombre) {
        this.insumoNombre = insumoNombre;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public int getCostoLinea() {
        return costoLinea;
    }

    public void setCostoLinea(int costoLinea) {
        this.costoLinea = costoLinea;
    }
}
