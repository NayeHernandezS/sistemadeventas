package org.nhernandez.webapp.sistemaventas.models;

public class Usuario {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String rol;
    private String adminOwner;
    private String tipoNegocio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getAdminOwner() {
        return adminOwner;
    }

    public void setAdminOwner(String adminOwner) {
        this.adminOwner = adminOwner;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public void setTipoNegocio(String tipoNegocio) {
        this.tipoNegocio = tipoNegocio;
    }
}
