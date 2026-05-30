package org.nhernandez.webapp.sistemaventas.util;

import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporteCsvExporterTest {

    private final ReporteCsvExporter exporter = new ReporteCsvExporter();

    @Test
    void escapar_envuelveValoresConComa() {
        assertEquals("\"a,b\"", ReporteCsvExporter.escapar("a,b"));
    }

    @Test
    void exportar_incluyeResumenYDetalle() {
        TicketVenta ticket = new TicketVenta();
        ticket.setId(1L);
        ticket.setFolio("TCK-001");
        ticket.setUsernameVendedor("vendedor1");
        ticket.setFechaVenta(LocalDateTime.of(2026, 5, 29, 10, 30));
        ticket.setTotal(500);
        ticket.setEstado("ACTIVO");

        ReporteVentas reporte = new ReporteVentas();
        reporte.setTicketsFiltrados(List.of(ticket));
        reporte.setDevueltoPorTicketId(Map.of(1L, 100));
        reporte.setCantidadFiltrada(1);
        reporte.setTotalFiltradoBruto(500);
        reporte.setTotalDevueltoFiltrado(100);
        reporte.setTotalFiltradoNeto(400);

        String csv = new String(exporter.exportar(reporte), StandardCharsets.UTF_8);

        assertTrue(csv.contains("Total neto,400"));
        assertTrue(csv.contains("TCK-001,vendedor1"));
        assertTrue(csv.contains(",500,100,400"));
    }
}
