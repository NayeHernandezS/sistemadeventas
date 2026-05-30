package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.PreferenciasTenant;
import org.nhernandez.webapp.sistemaventas.models.ResumenVentasVendedor;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.PreferenciasTenantRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilFase3ServicesTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PreferenciasTenantRepository preferenciasTenantRepository;

    @InjectMocks
    private ActividadVendedorService actividadVendedorService;

    @InjectMocks
    private PreferenciasTenantServiceImpl preferenciasTenantService;

    @Test
    void actividadVendedor_ticketsRecientes() throws SQLException {
        TicketVenta ticket = new TicketVenta();
        ticket.setFolio("TCK-1");
        when(ticketRepository.listarRecientesPorVendedor("vendedor1", 5)).thenReturn(List.of(ticket));

        List<TicketVenta> result = actividadVendedorService.ticketsRecientes("vendedor1");

        assertEquals(1, result.size());
    }

    @Test
    void actividadVendedor_resumenMesActual() throws SQLException {
        when(ticketRepository.resumenPorVendedorEnPeriodo(eq("vendedor1"), any(), any()))
                .thenReturn(new ResumenVentasVendedor(3, 1500));

        ResumenVentasVendedor resumen = actividadVendedorService.resumenMesActual("vendedor1");

        assertEquals(3, resumen.getCantidadTickets());
        assertEquals(1500, resumen.getTotalImporte());
    }

    @Test
    void preferenciasTenant_guardaStockMinimo() throws SQLException {
        preferenciasTenantService.guardarStockMinimo("tienda1", 8);

        verify(preferenciasTenantRepository).guardar(any(PreferenciasTenant.class));
    }

    @Test
    void preferenciasTenant_rechazaUmbralInvalido() {
        assertThrows(ServiceJdbcException.class,
                () -> preferenciasTenantService.guardarStockMinimo("tienda1", 0));
    }

    @Test
    void preferenciasTenant_resuelveGlobalSiNoHayPreferencia() throws SQLException {
        when(preferenciasTenantRepository.porTenant("tienda1")).thenReturn(null);

        assertEquals(5, preferenciasTenantService.resolverStockMinimo("tienda1", 5));
    }
}
