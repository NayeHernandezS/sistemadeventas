package org.nhernandez.webapp.sistemaventas.models;

public class DatosFiscalesNegocio {

    private String tenantUsername;
    private String rfc;
    private String razonSocial;
    private String email;
    private String direccion;
    private String usoCfdi;

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

    public boolean tieneDatos() {
        return campoConValor(rfc)
                || campoConValor(razonSocial)
                || campoConValor(email)
                || campoConValor(direccion)
                || campoConValor(usoCfdi);
    }

    private static boolean campoConValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}
