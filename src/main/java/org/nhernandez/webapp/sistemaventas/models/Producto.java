package org.nhernandez.webapp.sistemaventas.models;

import org.nhernandez.webapp.sistemaventas.util.UnidadMedidaUtil;

import java.io.Serializable;
import java.time.LocalDate;

public class Producto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String nombre;
    private Categoria categoria;
    private int precio;
    private int precioCompra;
    private int porcentajeGanancia;
    private int existencias;
    private String unidadMedida = "pza";
    private String sku;
    private LocalDate fechaRegistro;
    private String ownerUsername;
    private TipoItem tipoItem = TipoItem.PRODUCTO;

    public Producto() {
    }

    public Producto(Long id, String nombre, String tipo, int precio) {
        this.id = id;
        this.nombre = nombre;
        Categoria categoria = new Categoria();
        categoria.setNombre(tipo);
        this.categoria = categoria;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public int getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(int precioCompra) {
        this.precioCompra = precioCompra;
    }

    public int getPorcentajeGanancia() {
        return porcentajeGanancia;
    }

    public void setPorcentajeGanancia(int porcentajeGanancia) {
        this.porcentajeGanancia = Math.max(0, porcentajeGanancia);
    }

    /** Precio de venta sugerido: compra + porcentaje de ganancia deseado. */
    public int calcularPrecioVentaPorGanancia() {
        if (precioCompra <= 0 || porcentajeGanancia <= 0) {
            return 0;
        }
        return (int) Math.round(precioCompra * (1 + porcentajeGanancia / 100.0));
    }

    public int getExistencias() {
        return existencias;
    }

    public void setExistencias(int existencias) {
        this.existencias = existencias;
    }

    public String getUnidadMedida() {
        return unidadMedida != null ? unidadMedida : "pza";
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = UnidadMedidaUtil.normalizar(unidadMedida);
    }

    public String getExistenciasFormateadas() {
        return UnidadMedidaUtil.formatear(existencias, getUnidadMedida());
    }

    public String getExistenciasCantidadDisplay() {
        return UnidadMedidaUtil.desdeUnidadBase(existencias, getUnidadMedida())
                .stripTrailingZeros()
                .toPlainString();
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public TipoItem getTipoItem() {
        return tipoItem != null ? tipoItem : TipoItem.PRODUCTO;
    }

    public void setTipoItem(TipoItem tipoItem) {
        this.tipoItem = tipoItem != null ? tipoItem : TipoItem.PRODUCTO;
    }

    public boolean esServicio() {
        return getTipoItem() == TipoItem.SERVICIO;
    }

    /** Expuesto para EL/JSP ({@code ${p.esServicio}}). */
    public boolean getEsServicio() {
        return esServicio();
    }

    public boolean esProducto() {
        return !esServicio();
    }

    public boolean estaAgotado() {
        return esProducto() && existencias <= 0;
    }

    /** Expuesto para EL/JSP ({@code ${p.estaAgotado}}). */
    public boolean isEstaAgotado() {
        return estaAgotado();
    }

    public boolean esStockBajoUmbral(int umbral) {
        if (!esProducto() || existencias <= 0 || umbral <= 0) {
            return false;
        }
        return existencias <= UnidadMedidaUtil.umbralAUnidadBase(umbral, getUnidadMedida());
    }

    public String claseFilaInventario(int umbral) {
        if (esServicio()) {
            return "";
        }
        if (estaAgotado()) {
            return "table-danger";
        }
        if (esStockBajoUmbral(umbral)) {
            return "table-warning";
        }
        return "";
    }

    /** Margen en pesos: precio de venta menos precio de compra. */
    public int getMargen() {
        if (!tieneMargenCalculable()) {
            return 0;
        }
        return precio - precioCompra;
    }

    /** Porcentaje de ganancia real sobre el precio de compra (misma base que el formulario). */
    public int getMargenPorcentaje() {
        if (!tieneMargenCalculable()) {
            return 0;
        }
        return Math.round((getMargen() * 100f) / precioCompra);
    }

    public boolean tieneMargenCalculable() {
        return !esServicio() && precioCompra > 0 && precio > 0;
    }

    /** Expuesto para EL/JSP ({@code ${p.tieneMargenCalculable}}). */
    public boolean getTieneMargenCalculable() {
        return tieneMargenCalculable();
    }
}
