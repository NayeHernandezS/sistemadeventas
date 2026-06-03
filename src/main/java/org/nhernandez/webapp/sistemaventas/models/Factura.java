package org.nhernandez.webapp.sistemaventas.models;

import java.time.LocalDateTime;

public class Factura {

    private Long id;
    private Long ticketId;
    private Long clienteId;
    private String folioFactura;
    private String rfc;
    private String razonSocial;
    private String email;
    private String direccion;
    private String usoCfdi;
    private LocalDateTime fechaEmision;
    private String codigoPostalReceptor;
    private String cfdiUuid;
    private String cfdiEstado = "INFORMATIVO";
    private String cfdiMensaje;
    private String cfdiProveedorId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getFolioFactura() {
        return folioFactura;
    }

    public void setFolioFactura(String folioFactura) {
        this.folioFactura = folioFactura;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getUsoCfdi() {
        return usoCfdi;
    }

    public void setUsoCfdi(String usoCfdi) {
        this.usoCfdi = usoCfdi;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getCodigoPostalReceptor() {
        return codigoPostalReceptor;
    }

    public void setCodigoPostalReceptor(String codigoPostalReceptor) {
        this.codigoPostalReceptor = codigoPostalReceptor;
    }

    public String getCfdiUuid() {
        return cfdiUuid;
    }

    public void setCfdiUuid(String cfdiUuid) {
        this.cfdiUuid = cfdiUuid;
    }

    public String getCfdiEstado() {
        return cfdiEstado;
    }

    public void setCfdiEstado(String cfdiEstado) {
        this.cfdiEstado = cfdiEstado;
    }

    public String getCfdiMensaje() {
        return cfdiMensaje;
    }

    public void setCfdiMensaje(String cfdiMensaje) {
        this.cfdiMensaje = cfdiMensaje;
    }

    public String getCfdiProveedorId() {
        return cfdiProveedorId;
    }

    public void setCfdiProveedorId(String cfdiProveedorId) {
        this.cfdiProveedorId = cfdiProveedorId;
    }

    public boolean estaTimbrada() {
        return "TIMBRADO".equalsIgnoreCase(cfdiEstado) && cfdiUuid != null && !cfdiUuid.isBlank();
    }
}
