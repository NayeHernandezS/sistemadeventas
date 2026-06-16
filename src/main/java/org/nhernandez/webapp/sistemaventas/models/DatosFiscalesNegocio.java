package org.nhernandez.webapp.sistemaventas.models;

public class DatosFiscalesNegocio {

    private String tenantUsername;
    private String rfc;
    private String razonSocial;
    private String email;
    private String direccion;
    private String usoCfdi;
    private String codigoPostal;
    private String regimenFiscal;
    private String facturamaUsername;
    private String facturamaPasswordEnc;
    private boolean facturamaSandbox = true;
    private boolean cfdiHabilitado;

    public String getTenantUsername() {
        return tenantUsername;
    }

    public void setTenantUsername(String tenantUsername) {
        this.tenantUsername = tenantUsername;
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

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getRegimenFiscal() {
        return regimenFiscal;
    }

    public void setRegimenFiscal(String regimenFiscal) {
        this.regimenFiscal = regimenFiscal;
    }

    public String getFacturamaUsername() {
        return facturamaUsername;
    }

    public void setFacturamaUsername(String facturamaUsername) {
        this.facturamaUsername = facturamaUsername;
    }

    public String getFacturamaPasswordEnc() {
        return facturamaPasswordEnc;
    }

    public void setFacturamaPasswordEnc(String facturamaPasswordEnc) {
        this.facturamaPasswordEnc = facturamaPasswordEnc;
    }

    public boolean isFacturamaSandbox() {
        return facturamaSandbox;
    }

    public void setFacturamaSandbox(boolean facturamaSandbox) {
        this.facturamaSandbox = facturamaSandbox;
    }

    public boolean isCfdiHabilitado() {
        return cfdiHabilitado;
    }

    public void setCfdiHabilitado(boolean cfdiHabilitado) {
        this.cfdiHabilitado = cfdiHabilitado;
    }

    public boolean tieneFacturamaConfigurado() {
        return campoConValor(facturamaUsername) && campoConValor(facturamaPasswordEnc);
    }

    public boolean listoParaTimbrarEmisor() {
        return campoConValor(rfc) && campoConValor(razonSocial)
                && campoConValor(codigoPostal) && campoConValor(regimenFiscal);
    }

    public boolean tieneDatos() {
        return campoConValor(rfc)
                || campoConValor(razonSocial)
                || campoConValor(email)
                || campoConValor(direccion)
                || campoConValor(usoCfdi)
                || campoConValor(codigoPostal)
                || campoConValor(regimenFiscal);
    }

    private static boolean campoConValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}
