package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;

public class ProductoCompraSugerida implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productoId;
    private String nombre;
    private String sku;
    private String categoria;
    private int existencias;
    private int stockMinimo;
    private String unidadMedida = "pza";
    private String existenciasTexto;
    private String nivelAlerta;
    private int cantidadSugerida;
    private String cantidadSugeridaTexto;
    private int unidadesVendidas7d;
    private int precioCompra;
    private int costoEstimadoReposicion;

    public boolean isAgotado() {
        return "AGOTADO".equals(nivelAlerta);
    }

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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getExistencias() {
        return existencias;
    }

    public void setExistencias(int existencias) {
        this.existencias = existencias;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getExistenciasTexto() {
        return existenciasTexto;
    }

    public void setExistenciasTexto(String existenciasTexto) {
        this.existenciasTexto = existenciasTexto;
    }

    public String getNivelAlerta() {
        return nivelAlerta;
    }

    public void setNivelAlerta(String nivelAlerta) {
        this.nivelAlerta = nivelAlerta;
    }

    public int getCantidadSugerida() {
        return cantidadSugerida;
    }

    public void setCantidadSugerida(int cantidadSugerida) {
        this.cantidadSugerida = cantidadSugerida;
    }

    public String getCantidadSugeridaTexto() {
        return cantidadSugeridaTexto;
    }

    public void setCantidadSugeridaTexto(String cantidadSugeridaTexto) {
        this.cantidadSugeridaTexto = cantidadSugeridaTexto;
    }

    public int getUnidadesVendidas7d() {
        return unidadesVendidas7d;
    }

    public void setUnidadesVendidas7d(int unidadesVendidas7d) {
        this.unidadesVendidas7d = unidadesVendidas7d;
    }

    public int getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(int precioCompra) {
        this.precioCompra = precioCompra;
    }

    public int getCostoEstimadoReposicion() {
        return costoEstimadoReposicion;
    }

    public void setCostoEstimadoReposicion(int costoEstimadoReposicion) {
        this.costoEstimadoReposicion = costoEstimadoReposicion;
    }
}
