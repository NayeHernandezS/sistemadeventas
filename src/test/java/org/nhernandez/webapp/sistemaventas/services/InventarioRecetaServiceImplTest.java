package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.ItemCarro;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.Receta;
import org.nhernandez.webapp.sistemaventas.models.RecetaLinea;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.RecetaRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioRecetaServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private RecetaRepository recetaRepository;

    @InjectMocks
    private InventarioRecetaServiceImpl service;

    @Test
    void validarStockCarrito_platilloConReceta_validaInsumos() throws SQLException {
        Producto platillo = producto(10L, "Taco", 999);
        Producto jitomate = producto(20L, "Jitomate", 100);
        jitomate.setUnidadMedida("kg");

        Receta receta = new Receta();
        receta.setId(1L);
        RecetaLinea linea = new RecetaLinea();
        linea.setInsumoProductoId(20L);
        linea.setCantidad(new BigDecimal("0.2"));
        linea.setUnidad("kg");

        when(productoRepository.porIdPorOwner(10L, "tienda1")).thenReturn(platillo);
        when(recetaRepository.porProductoId("tienda1", 10L)).thenReturn(Optional.of(receta));
        when(recetaRepository.listarLineasPorReceta(1L)).thenReturn(List.of(linea));
        when(productoRepository.porIdPorOwner(20L, "tienda1")).thenReturn(jitomate);

        Producto ref = new Producto();
        ref.setId(10L);
        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> service.validarStockCarrito("tienda1", List.of(new ItemCarro(2, ref))));

        assertTrue(ex.getMessage().contains("Jitomate"));
    }

    @Test
    void descontarPorTicket_platilloConReceta_descuentaInsumo() throws SQLException {
        Producto platillo = producto(10L, "Taco", 999);
        Producto jitomate = producto(20L, "Jitomate", 5000);
        jitomate.setUnidadMedida("kg");

        Receta receta = new Receta();
        receta.setId(1L);
        RecetaLinea linea = new RecetaLinea();
        linea.setInsumoProductoId(20L);
        linea.setCantidad(new BigDecimal("0.15"));
        linea.setUnidad("kg");

        when(productoRepository.porIdPorOwner(10L, "tienda1")).thenReturn(platillo);
        when(recetaRepository.porProductoId("tienda1", 10L)).thenReturn(Optional.of(receta));
        when(recetaRepository.listarLineasPorReceta(1L)).thenReturn(List.of(linea));

        TicketItem item = new TicketItem();
        item.setProductoId(10L);
        item.setCantidad(2);
        TicketVenta ticket = new TicketVenta();
        ticket.setTenantOwner("tienda1");
        ticket.setItems(List.of(item));

        service.descontarPorTicket(ticket);

        verify(productoRepository).descontarExistencias(eq(20L), eq("tienda1"), eq(300));
    }

    private static Producto producto(long id, String nombre, int existencias) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setExistencias(existencias);
        return p;
    }
}
