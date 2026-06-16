package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketConsultaServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketConsultaServiceImpl service;

    @Test
    void listar_sinTexto_adminUsaListadoPorTenant() throws SQLException {
        TicketVenta ticket = new TicketVenta();
        ticket.setFolio("TCK-1");
        when(ticketRepository.listarPorTenant("tienda1")).thenReturn(List.of(ticket));

        List<TicketVenta> result = service.listar("tienda1", Optional.empty(), "   ");

        assertEquals(1, result.size());
        verify(ticketRepository).listarPorTenant("tienda1");
    }

    @Test
    void listar_conTexto_vendedorUsaBusquedaPorVendedor() throws SQLException {
        when(ticketRepository.buscarPorVendedor("v1", "juan", TicketConsultaServiceImpl.LIMITE_BUSQUEDA))
                .thenReturn(List.of());

        service.listar("tienda1", Optional.of("v1"), " juan ");

        verify(ticketRepository).buscarPorVendedor("v1", "juan", TicketConsultaServiceImpl.LIMITE_BUSQUEDA);
    }

    @Test
    void listar_conTexto_adminUsaBusquedaPorTenant() throws SQLException {
        when(ticketRepository.buscarPorTenant("tienda1", "folio", TicketConsultaServiceImpl.LIMITE_BUSQUEDA))
                .thenReturn(List.of());

        service.listar("tienda1", Optional.empty(), "folio");

        verify(ticketRepository).buscarPorTenant("tienda1", "folio", TicketConsultaServiceImpl.LIMITE_BUSQUEDA);
    }

    @Test
    void historialPorNombreCliente_nombreVacio_devuelveListaVacia() {
        assertTrue(service.historialPorNombreCliente("tienda1", "  ", 10).isEmpty());
    }

    @Test
    void historialPorNombreCliente_consultaRepositorio() throws SQLException {
        TicketVenta ticket = new TicketVenta();
        ticket.setFolio("TCK-2");
        when(ticketRepository.listarPorTenantYNombreCliente("tienda1", "Ana", 15))
                .thenReturn(List.of(ticket));

        List<TicketVenta> result = service.historialPorNombreCliente("tienda1", "Ana", 15);

        assertEquals(1, result.size());
        verify(ticketRepository).listarPorTenantYNombreCliente(eq("tienda1"), eq("Ana"), eq(15));
    }
}
