package org.nhernandez.webapp.sistemaventas.models;

import java.util.Locale;
import java.util.Optional;

public enum TipoMovimientoInventario {

    ENTRADA("Entrada de mercancia"),
    SALIDA("Salida / merma"),
    AJUSTE("Ajuste a cantidad fija");

    private final String etiqueta;

    TipoMovimientoInventario(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Optional<TipoMovimientoInventario> porCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(TipoMovimientoInventario.valueOf(codigo.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
