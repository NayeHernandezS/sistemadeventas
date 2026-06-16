package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;

public class Receta implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String tenantOwner;
    private Long productoId;

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
}
