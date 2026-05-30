package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.DevolucionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private DevolucionRepository devolucionRepository;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    @Test
    void generar_calculaTotalesNetosConDevoluciones() throws SQLException {
        TicketVenta ticket1 = ticket(1L, "vendedor1", 500, LocalDateTime.now());
        TicketVenta ticket2 = ticket(2L, "vendedor1", 300, LocalDateTime.now());

        when(ticketRepository.listarPorTenant("tienda1")).thenReturn(List.of(ticket1, ticket2));
        when(devolucionRepository.totalesDevueltosPorTenant("tienda1"))
                .thenReturn(Map.of(1L, 200));

        ReporteVentas reporte = reporteService.generar(
                "tienda1", "admin", true, "", null, null);

        assertEquals(800, reporte.getTotalFiltradoBruto());
        assertEquals(200, reporte.getTotalDevueltoFiltrado());
        assertEquals(600, reporte.getTotalFiltradoNeto());
        assertEquals(300, reporte.totalNeto(ticket1));
        assertEquals(300, reporte.totalNeto(ticket2));
    }

    @Test
    void generar_filtraPorVendedor() throws SQLException {
        TicketVenta t1 = ticket(1L, "ana", 100, LocalDateTime.now());
        TicketVenta t2 = ticket(2L, "luis", 200, LocalDateTime.now());

        when(ticketRepository.listarPorTenant("tienda1")).thenReturn(List.of(t1, t2));
        when(devolucionRepository.totalesDevueltosPorTenant("tienda1")).thenReturn(Map.of());

        ReporteVentas reporte = reporteService.generar(
                "tienda1", "admin", true, "ana", null, null);

        assertEquals(1, reporte.getCantidadFiltrada());
        assertEquals(100, reporte.getTotalFiltradoNeto());
    }

    private TicketVenta ticket(Long id, String vendedor, int total, LocalDateTime fecha) {
        TicketVenta t = new TicketVenta();
        t.setId(id);
        t.setFolio("TCK-" + id);
        t.setUsernameVendedor(vendedor);
        t.setTenantOwner("tienda1");
        t.setFechaVenta(fecha);
        t.setTotal(total);
        t.setEstado("ACTIVO");
        return t;
    }
}
