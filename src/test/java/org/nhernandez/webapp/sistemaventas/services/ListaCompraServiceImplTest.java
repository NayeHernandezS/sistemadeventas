package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.ListaCompraHoy;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.ProductoCompraSugerida;
import org.nhernandez.webapp.sistemaventas.models.ProductoVentaRanking;
import org.nhernandez.webapp.sistemaventas.models.TipoItem;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListaCompraServiceImplTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private InventarioAlertaService inventarioAlertaService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ListaCompraServiceImpl listaCompraService;

    @Test
    void generar_ordenaAgotadosPrimeroYCantidadSugerida() throws SQLException {
        Producto agotado = producto(1L, "Refresco", 0, 12);
        Producto bajo = producto(2L, "Galletas", 3, 8);

        when(usuarioService.porUsername("tienda1")).thenReturn(java.util.Optional.empty());

        when(inventarioAlertaService.getStockMinimo("tienda1")).thenReturn(5);
        when(productoService.listarPorOwner("tienda1")).thenReturn(List.of(bajo, agotado));
        when(inventarioAlertaService.requiereAlerta(agotado, 5)).thenReturn(true);
        when(inventarioAlertaService.requiereAlerta(bajo, 5)).thenReturn(true);
        when(inventarioAlertaService.esAgotado(agotado)).thenReturn(true);
        when(inventarioAlertaService.esAgotado(bajo)).thenReturn(false);
        when(ticketRepository.topProductosVendidosPorTenant(eq("tienda1"), any(), any(), eq(50)))
                .thenReturn(List.of(ranking(2L, 15)));

        ListaCompraHoy lista = listaCompraService.generar("tienda1");

        assertEquals(2, lista.getTotalProductos());
        assertEquals(2, lista.getProductos().size());
        ProductoCompraSugerida primero = lista.getProductos().get(0);
        assertEquals("Refresco", primero.getNombre());
        assertEquals("AGOTADO", primero.getNivelAlerta());
        assertEquals(10, primero.getCantidadSugerida());

        ProductoCompraSugerida segundo = lista.getProductos().get(1);
        assertEquals("Galletas", segundo.getNombre());
        assertEquals(7, segundo.getCantidadSugerida());
        assertEquals(15, segundo.getUnidadesVendidas7d());
        assertEquals(56, segundo.getCostoEstimadoReposicion());
    }

    @Test
    void generar_limiteVista_mantieneTotalCompleto() throws SQLException {
        Producto p1 = producto(1L, "A", 0, 10);
        Producto p2 = producto(2L, "B", 1, 10);

        when(usuarioService.porUsername("tienda1")).thenReturn(java.util.Optional.empty());
        when(inventarioAlertaService.getStockMinimo("tienda1")).thenReturn(5);
        when(productoService.listarPorOwner("tienda1")).thenReturn(List.of(p1, p2));
        when(inventarioAlertaService.requiereAlerta(p1, 5)).thenReturn(true);
        when(inventarioAlertaService.requiereAlerta(p2, 5)).thenReturn(true);
        when(inventarioAlertaService.esAgotado(p1)).thenReturn(true);
        when(inventarioAlertaService.esAgotado(p2)).thenReturn(false);
        when(ticketRepository.topProductosVendidosPorTenant(eq("tienda1"), any(), any(), eq(50)))
                .thenReturn(List.of());

        ListaCompraHoy lista = listaCompraService.generar("tienda1", 1);

        assertEquals(2, lista.getTotalProductos());
        assertEquals(1, lista.getProductos().size());
        assertTrue(lista.getProductos().get(0).isAgotado());
    }

    private Producto producto(Long id, String nombre, int existencias, int precioCompra) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setExistencias(existencias);
        p.setPrecioCompra(precioCompra);
        p.setTipoItem(TipoItem.PRODUCTO);
        Categoria cat = new Categoria();
        cat.setNombre("Abarrotes");
        p.setCategoria(cat);
        return p;
    }

    private ProductoVentaRanking ranking(Long id, int unidades) {
        ProductoVentaRanking r = new ProductoVentaRanking();
        r.setProductoId(id);
        r.setUnidadesVendidas(unidades);
        return r;
    }
}
