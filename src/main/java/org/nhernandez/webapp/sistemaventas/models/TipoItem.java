package org.nhernandez.webapp.sistemaventas.models;

import java.util.Locale;
import java.util.Optional;

public enum TipoItem {

    PRODUCTO("Producto"),
    SERVICIO("Servicio");

    private final String etiqueta;

    TipoItem(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Expuesto para EL/JSP ({@code ${tipo.name}}). */
    public String getName() {
        return name();
    }

    public static Optional<TipoItem> porCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(TipoItem.valueOf(codigo.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
