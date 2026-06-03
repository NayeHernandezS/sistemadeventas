package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.PanelNegocioResumen;
import org.nhernandez.webapp.sistemaventas.models.ProductoVentaRanking;
import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PanelNegocioServiceImplTest {

    @Mock
    private ReporteService reporteService;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private PanelNegocioServiceImpl service;

    @Test
    void resumenParaAdmin_combinaReporteYRanking() throws SQLException {
        ReporteVentas reporte = new ReporteVentas();
        reporte.setCantidadDia(3);
        reporte.setTotalDiaNeto(1500);
        reporte.setCantidadSemana(10);
        reporte.setTotalSemanaNeto(4200);
        reporte.setCantidadMes(25);
        reporte.setTotalMesNeto(9800);

        ProductoVentaRanking top = new ProductoVentaRanking();
        top.setNombreProducto("Agua");
        top.setUnidadesVendidas(20);

        when(reporteService.generar(eq("tienda1"), eq("admin1"), eq(true), eq(""), any(), any()))
                .thenReturn(reporte);
        when(ticketRepository.topProductosVendidosPorTenant(
                eq("tienda1"), any(LocalDateTime.class), any(LocalDateTime.class), eq(3)))
                .thenReturn(List.of(top));

        PanelNegocioResumen panel = service.resumenParaAdmin("tienda1", "admin1");

        assertEquals(3, panel.getTicketsHoy());
        assertEquals(1500, panel.getNetoHoy());
        assertEquals(1, panel.getTopProductosSemana().size());
        assertEquals("Agua", panel.getTopProductosSemana().get(0).getNombreProducto());
    }
}
