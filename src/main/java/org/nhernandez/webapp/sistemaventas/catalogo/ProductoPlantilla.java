package org.nhernandez.webapp.sistemaventas.catalogo;

public record ProductoPlantilla(
        String nombre,
        String sku,
        String categoria,
        int precio,
        int existencias
) {
}
