package org.nhernandez.webapp.sistemaventas.models;

import java.util.Locale;
import java.util.Optional;

public enum EstadoCita {

    PROGRAMADA("Programada"),
    CONFIRMADA("Confirmada"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoCita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getName() {
        return name();
    }

    public static Optional<EstadoCita> porCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(EstadoCita.valueOf(codigo.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
