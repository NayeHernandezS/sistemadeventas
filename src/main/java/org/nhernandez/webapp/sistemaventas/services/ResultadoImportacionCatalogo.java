package org.nhernandez.webapp.sistemaventas.services;

public record ResultadoImportacionCatalogo(
        int importados,
        int omitidos,
        int totalPlantilla
) {
}
