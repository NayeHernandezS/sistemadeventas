package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventarioAlertaServiceTest {

    private InventarioAlertaService inventarioAlertaService;

    @BeforeEach
    void setUp() {
        inventarioAlertaService = new InventarioAlertaService();
        ReflectionTestUtils.setField(inventarioAlertaService, "stockMinimo", 5);
    }

    @Test
    void esAgotado_cuandoExistenciasEsCero() {
        assertTrue(inventarioAlertaService.esAgotado(producto(0)));
        assertFalse(inventarioAlertaService.esAgotado(producto(1)));
    }

    @Test
    void esStockBajo_cuandoExistenciasEnUmbral() {
        assertTrue(inventarioAlertaService.esStockBajo(producto(5)));
        assertTrue(inventarioAlertaService.esStockBajo(producto(3)));
        assertFalse(inventarioAlertaService.esStockBajo(producto(6)));
        assertFalse(inventarioAlertaService.esStockBajo(producto(0)));
    }

    @Test
    void contarConAlerta_sumaAgotadosYBajos() {
        List<Producto> productos = List.of(
                producto(0),
                producto(2),
                producto(10)
        );

        assertEquals(2, inventarioAlertaService.contarConAlerta(productos));
        assertEquals(1, inventarioAlertaService.contarAgotados(productos));
        assertEquals(1, inventarioAlertaService.contarStockBajo(productos));
    }

    private Producto producto(int existencias) {
        Producto p = new Producto();
        p.setExistencias(existencias);
        return p;
    }
}
