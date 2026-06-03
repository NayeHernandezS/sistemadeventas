package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;

public class ProductoVentaRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productoId;
    private String nombreProducto;
    private int unidadesVendidas;
    private long importeTotal;

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public void setUnidadesVendidas(int unidadesVendidas) {
        this.unidadesVendidas = unidadesVendidas;
    }

    public long getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(long importeTotal) {
        this.importeTotal = importeTotal;
    }
}
