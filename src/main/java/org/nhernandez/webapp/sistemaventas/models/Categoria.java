package org.nhernandez.webapp.sistemaventas.models;

import java.io.Serializable;

public class Categoria implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String nombre;
    private String ownerUsername;

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

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
