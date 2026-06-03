package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MovimientoInventario implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String tenantOwner;
    private Long productoId;
    private String nombreProducto;
    private TipoMovimientoInventario tipo;
    private int cantidad;
    private int existenciasAntes;
    private int existenciasDespues;
    private String motivo;
    private String username;
    private LocalDateTime fecha;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantOwner() {
        return tenantOwner;
    }

    public void setTenantOwner(String tenantOwner) {
        this.tenantOwner = tenantOwner;
    }

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

    public TipoMovimientoInventario getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimientoInventario tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getExistenciasAntes() {
        return existenciasAntes;
    }

    public void setExistenciasAntes(int existenciasAntes) {
        this.existenciasAntes = existenciasAntes;
    }

    public int getExistenciasDespues() {
        return existenciasDespues;
    }

    public void setExistenciasDespues(int existenciasDespues) {
        this.existenciasDespues = existenciasDespues;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
