package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.CierreCajaDia;
import org.nhernandez.webapp.sistemaventas.models.ProductoVentaRanking;
import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CierreCajaServiceImplTest {

    private static final LocalDate HOY = LocalDate.of(2026, 6, 9);

    @Mock
    private ReporteService reporteService;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private CierreCajaServiceImpl cierreCajaService;

    @Test
    void generar_admin_incluyeVentasPorVendedorYTopProductos() throws SQLException {
        TicketVenta t1 = ticket(1L, "ana", 400);
        TicketVenta t2 = ticket(2L, "luis", 600);
        ReporteVentas reporteHoy = reporte(List.of(t1, t2), 1000, 100, 900, 2);
        ReporteVentas reporteAyer = reporte(List.of(), 0, 0, 0, 0);

        when(reporteService.generar("tienda1", "admin", true, "", HOY, HOY)).thenReturn(reporteHoy);
        when(reporteService.generar("tienda1", "admin", true, "", HOY.minusDays(1), HOY.minusDays(1)))
                .thenReturn(reporteAyer);
        when(ticketRepository.topProductosVendidosPorTenant(eq("tienda1"), any(), any(), eq(5)))
                .thenReturn(List.of(ranking("Refresco", 10, 150)));

        CierreCajaDia cierre = cierreCajaService.generar("tienda1", "admin", true, HOY);

        assertEquals("2026-06-09", cierre.getFecha());
        assertEquals(2, cierre.getCantidadTickets());
        assertEquals(900, cierre.getTotalNeto());
        assertEquals(0, cierre.getTotalNetoAyer());
        assertEquals(900, cierre.getDiferenciaNetoAyer());
        assertTrue(cierre.isEsAdmin());
        assertEquals(2, cierre.getVentasPorVendedor().size());
        assertEquals("luis", cierre.getVentasPorVendedor().get(0).getVendedor());
        assertEquals(600, cierre.getVentasPorVendedor().get(0).getTotalNeto());
        assertEquals(1, cierre.getTopProductos().size());
        assertEquals("Refresco", cierre.getTopProductos().get(0).getNombreProducto());
    }

    @Test
    void generar_vendedor_filtraPorUsuarioSinDesglose() throws SQLException {
        TicketVenta ticket = ticket(1L, "ana", 250);
        ReporteVentas reporteHoy = reporte(List.of(ticket), 250, 0, 250, 1);
        ReporteVentas reporteAyer = reporte(List.of(ticket), 250, 0, 250, 1);

        when(reporteService.generar("tienda1", "ana", false, "ana", HOY, HOY)).thenReturn(reporteHoy);
        when(reporteService.generar("tienda1", "ana", false, "ana", HOY.minusDays(1), HOY.minusDays(1)))
                .thenReturn(reporteAyer);
        when(ticketRepository.topProductosVendidosPorTenant(eq("tienda1"), any(), any(), eq(5)))
                .thenReturn(List.of());

        CierreCajaDia cierre = cierreCajaService.generar("tienda1", "ana", false, HOY);

        assertEquals("ana", cierre.getVendedorFiltro());
        assertFalse(cierre.isEsAdmin());
        assertTrue(cierre.getVentasPorVendedor().isEmpty());
        assertEquals(250, cierre.getTotalNeto());
        assertEquals(0, cierre.getDiferenciaNetoAyer());
    }

    private ReporteVentas reporte(List<TicketVenta> tickets, int bruto, int devuelto, int neto, int cantidad) {
        ReporteVentas r = new ReporteVentas();
        r.setTicketsFiltrados(tickets);
        r.setTotalFiltradoBruto(bruto);
        r.setTotalDevueltoFiltrado(devuelto);
        r.setTotalFiltradoNeto(neto);
        r.setCantidadFiltrada(cantidad);
        r.setDevueltoPorTicketId(Map.of(1L, devuelto));
        return r;
    }

    private TicketVenta ticket(Long id, String vendedor, int total) {
        TicketVenta t = new TicketVenta();
        t.setId(id);
        t.setFolio("TCK-" + id);
        t.setUsernameVendedor(vendedor);
        t.setTenantOwner("tienda1");
        t.setFechaVenta(LocalDateTime.of(2026, 6, 9, 12, 0));
        t.setTotal(total);
        t.setEstado("ACTIVO");
        return t;
    }

    private ProductoVentaRanking ranking(String nombre, int unidades, int importe) {
        ProductoVentaRanking p = new ProductoVentaRanking();
        p.setNombreProducto(nombre);
        p.setUnidadesVendidas(unidades);
        p.setImporteTotal(importe);
        return p;
    }
}
