package org.nhernandez.webapp.sistemaventas.web;

public record EscaneoCarroRespuesta(
        boolean ok,
        String mensaje,
        String productoNombre,
        int totalCarro,
        int cantidadItems,
        Long productoId,
        int cantidadProducto,
        int importeLinea,
        int precioUnitario
) {
}
