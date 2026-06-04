package org.nhernandez.webapp.sistemaventas.util;

/**
 * Plantilla de un servicio sugerido (nombre + categoria agrupadora).
 */
public record SugerenciaServicio(String nombre, String categoria) {

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }
}
