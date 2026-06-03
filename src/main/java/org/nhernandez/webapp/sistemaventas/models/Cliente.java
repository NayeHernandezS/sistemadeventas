package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String tenantOwner;
    private String nombre;
    private String rfc;
    private String razonSocial;
    private String email;
    private String codigoPostal;
    private String usoCfdi;
    private boolean activo = true;
    private LocalDateTime fechaRegistro;

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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getUsoCfdi() {
        return usoCfdi;
    }

    public void setUsoCfdi(String usoCfdi) {
        this.usoCfdi = usoCfdi;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /** Nombre fiscal para facturacion: razon social o nombre de contacto. */
    public String nombreFiscal() {
        if (razonSocial != null && !razonSocial.isBlank()) {
            return razonSocial.trim();
        }
        return nombre != null ? nombre.trim() : "";
    }
}
