package org.nhernandez.webapp.sistemaventas.models;

/**
 * Linea de ticket con cantidades disponibles para devolver.
 */
public class LineaDevolucionVista {

    private Long productoId;
    private String nombreProducto;
    private int precioUnitario;
    private int cantidadVendida;
    private int cantidadYaDevuelta;
    private int cantidadDisponible;

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

    public int getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(int precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(int cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public int getCantidadYaDevuelta() {
        return cantidadYaDevuelta;
    }

    public void setCantidadYaDevuelta(int cantidadYaDevuelta) {
        this.cantidadYaDevuelta = cantidadYaDevuelta;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }
}
