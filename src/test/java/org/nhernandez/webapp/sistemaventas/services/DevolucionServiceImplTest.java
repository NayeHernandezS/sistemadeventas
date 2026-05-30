package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.DevolucionItem;
import org.nhernandez.webapp.sistemaventas.models.LineaDevolucionVista;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.DevolucionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevolucionServiceImplTest {

    @Mock
    private DevolucionRepository devolucionRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private DevolucionServiceImpl devolucionService;

    private TicketVenta ticket;

    @BeforeEach
    void setUp() {
        ticket = new TicketVenta();
        ticket.setId(10L);
        ticket.setFolio("TCK-001");
        ticket.setTenantOwner("admin1");
        ticket.setEstado("ACTIVO");

        TicketItem item = new TicketItem();
        item.setProductoId(5L);
        item.setNombreProducto("Teclado");
        item.setPrecioUnitario(100);
        item.setCantidad(3);
        item.setImporte(300);
        ticket.setItems(List.of(item));
    }

    @Test
    void lineasDisponibles_calculaCantidadRestante() throws SQLException {
        when(devolucionRepository.cantidadDevueltaDeProducto(10L, 5L)).thenReturn(1);

        List<LineaDevolucionVista> lineas = devolucionService.lineasDisponibles(ticket);

        assertEquals(1, lineas.size());
        assertEquals(2, lineas.get(0).getCantidadDisponible());
        assertEquals(1, lineas.get(0).getCantidadYaDevuelta());
    }

    @Test
    void obtenerTicketParaDevolucion_rechazaTicketDevueltoTotal() throws SQLException {
        ticket.setEstado("DEVUELTO_TOTAL");
        when(ticketRepository.porIdDeTenant(10L, "admin1")).thenReturn(ticket);

        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> devolucionService.obtenerTicketParaDevolucion(10L, "admin1"));

        assertTrue(ex.getMessage().contains("devuelto por completo"));
    }

    @Test
    void registrarDevolucion_rechazaCantidadMayorALaDisponible() throws SQLException {
        when(ticketRepository.porIdDeTenant(10L, "admin1")).thenReturn(ticket);
        when(devolucionRepository.cantidadDevueltaDeProducto(10L, 5L)).thenReturn(0);

        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> devolucionService.registrarDevolucion(
                        10L, "admin1", "vendedor1", Map.of(5L, 5), "error de cliente"));

        assertTrue(ex.getMessage().contains("Cantidad invalida"));
        verify(devolucionRepository, never()).guardar(any());
    }

    @Test
    void registrarDevolucion_reintegraExistenciasYActualizaTicket() throws SQLException {
        when(ticketRepository.porIdDeTenant(10L, "admin1")).thenReturn(ticket);
        when(devolucionRepository.cantidadDevueltaDeProducto(10L, 5L)).thenReturn(0, 2);

        var devolucion = devolucionService.registrarDevolucion(
                10L, "admin1", "vendedor1", Map.of(5L, 2), "producto defectuoso");

        assertEquals(200, devolucion.getTotalDevuelto());
        assertEquals(1, devolucion.getItems().size());
        DevolucionItem item = devolucion.getItems().get(0);
        assertEquals(2, item.getCantidad());

        verify(devolucionRepository).guardar(any());
        verify(productoRepository).agregarExistencias(5L, "admin1", 2);
        verify(ticketRepository).actualizarEstado(10L, "admin1", "DEVUELTO_PARCIAL");
    }
}
