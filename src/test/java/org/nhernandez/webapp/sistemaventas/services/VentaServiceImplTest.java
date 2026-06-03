package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.FacturaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;

import java.sql.SQLException;
import java.util.List;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private CfdiTimbradoService cfdiTimbradoService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Agua");
        producto.setExistencias(10);
        producto.setPrecio(15);
    }

    @Test
    void validarStock_rechazaCuandoNoHayExistencias() throws SQLException {
        producto.setExistencias(2);
        when(productoRepository.porIdPorOwner(1L, "tienda1")).thenReturn(producto);

        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> ventaService.validarStock("tienda1", 1L, 5));

        assertEquals(true, ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void registrarVenta_guardaTicketYFactura() throws SQLException {
        when(productoRepository.porIdPorOwner(1L, "tienda1")).thenReturn(producto);
        doAnswer(inv -> {
            TicketVenta t = inv.getArgument(0);
            t.setId(99L);
            return null;
        }).when(ticketRepository).guardar(any(TicketVenta.class));

        TicketVenta ticket = ticketConItem(1L, 2);
        Factura factura = new Factura();
        factura.setFolioFactura("FAC-001");

        TicketVenta resultado = ventaService.registrarVenta(ticket, factura);

        assertEquals(99L, resultado.getId());
        verify(ticketRepository).guardar(ticket);
        verify(facturaRepository).guardar(factura);
        verify(cfdiTimbradoService).intentarTimbrar("tienda1", ticket, factura);
    }

    @Test
    void registrarVenta_facturaConClienteId() throws SQLException {
        when(productoRepository.porIdPorOwner(1L, "tienda1")).thenReturn(producto);
        doAnswer(inv -> {
            TicketVenta t = inv.getArgument(0);
            t.setId(88L);
            return null;
        }).when(ticketRepository).guardar(any(TicketVenta.class));

        Factura factura = new Factura();
        factura.setFolioFactura("FAC-CLI");
        factura.setClienteId(7L);

        ventaService.registrarVenta(ticketConItem(1L, 1), factura);

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).guardar(captor.capture());
        assertEquals(7L, captor.getValue().getClienteId());
        assertNotNull(captor.getValue().getTicketId());
    }

    @Test
    void registrarVenta_sinFacturaNoLlamaFacturaRepository() throws SQLException {
        when(productoRepository.porIdPorOwner(1L, "tienda1")).thenReturn(producto);
        doAnswer(inv -> {
            TicketVenta t = inv.getArgument(0);
            t.setId(50L);
            return null;
        }).when(ticketRepository).guardar(any(TicketVenta.class));

        ventaService.registrarVenta(ticketConItem(1L, 1), null);

        verify(facturaRepository, never()).guardar(any());
    }

    private TicketVenta ticketConItem(long productoId, int cantidad) {
        TicketItem item = new TicketItem();
        item.setProductoId(productoId);
        item.setNombreProducto("Agua");
        item.setPrecioUnitario(15);
        item.setCantidad(cantidad);
        item.setImporte(15 * cantidad);

        TicketVenta ticket = new TicketVenta();
        ticket.setTenantOwner("tienda1");
        ticket.setTotal(15 * cantidad);
        ticket.setItems(List.of(item));
        return ticket;
    }
}
