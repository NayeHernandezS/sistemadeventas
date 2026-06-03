package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TipoMovimientoInventario;
import org.nhernandez.webapp.sistemaventas.repositories.MovimientoInventarioRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioMovimientoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @Mock
    private Connection conn;

    @InjectMocks
    private InventarioMovimientoServiceImpl service;

    @Test
    void aplicarMovimiento_entradaSumaExistencias() throws SQLException {
        Producto producto = productoConStock(10);
        when(productoRepository.porIdPorOwner(1L, "tienda1")).thenReturn(producto);
        when(conn.getAutoCommit()).thenReturn(true);

        service.aplicarMovimiento("tienda1", "admin1", 1L, TipoMovimientoInventario.ENTRADA, 5, "Compra");

        verify(movimientoRepository).insertar(any());
        verify(productoRepository).agregarExistencias(1L, "tienda1", 5);
        verify(conn).commit();
    }

    @Test
    void aplicarMovimiento_salidaRechazaStockInsuficiente() throws SQLException {
        when(productoRepository.porIdPorOwner(1L, "tienda1")).thenReturn(productoConStock(2));

        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> service.aplicarMovimiento("tienda1", "admin1", 1L, TipoMovimientoInventario.SALIDA, 5, null));

        assertEquals(true, ex.getMessage().contains("Stock insuficiente"));
        verify(movimientoRepository, never()).insertar(any());
    }

    @Test
    void aplicarMovimiento_ajusteFijaExistencias() throws SQLException {
        when(productoRepository.porIdPorOwner(1L, "tienda1")).thenReturn(productoConStock(10));
        when(conn.getAutoCommit()).thenReturn(true);

        service.aplicarMovimiento("tienda1", "admin1", 1L, TipoMovimientoInventario.AJUSTE, 7, "Conteo");

        verify(productoRepository).actualizarExistencias(1L, "tienda1", 7);
        verify(movimientoRepository).insertar(any());
    }

    @Test
    void aplicarMovimiento_rechazaSinCambio() throws SQLException {
        when(productoRepository.porIdPorOwner(1L, "tienda1")).thenReturn(productoConStock(5));

        assertThrows(ServiceJdbcException.class,
                () -> service.aplicarMovimiento("tienda1", "admin1", 1L, TipoMovimientoInventario.AJUSTE, 5, null));
    }

    private static Producto productoConStock(int existencias) {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Producto");
        p.setExistencias(existencias);
        return p;
    }
}
