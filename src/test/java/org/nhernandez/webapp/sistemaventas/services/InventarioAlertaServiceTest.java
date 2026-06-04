package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.PreferenciasTenant;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TipoItem;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioAlertaServiceTest {

    @Mock
    private PreferenciasTenantService preferenciasTenantService;

    private InventarioAlertaService inventarioAlertaService;

    @BeforeEach
    void setUp() {
        inventarioAlertaService = new InventarioAlertaService(preferenciasTenantService);
        ReflectionTestUtils.setField(inventarioAlertaService, "stockMinimoGlobal", 5);
    }

    @Test
    void getStockMinimo_usaPreferenciaDelTenant() {
        when(preferenciasTenantService.resolverStockMinimo("tienda1", 5)).thenReturn(10);

        assertEquals(10, inventarioAlertaService.getStockMinimo("tienda1"));
    }

    @Test
    void esStockBajo_respetaUmbralDelTenant() {
        when(preferenciasTenantService.resolverStockMinimo("tienda1", 5)).thenReturn(10);

        assertTrue(inventarioAlertaService.esStockBajo(producto(8), "tienda1"));
        assertFalse(inventarioAlertaService.esStockBajo(producto(11), "tienda1"));
    }

    @Test
    void esAgotado_cuandoExistenciasEsCero() {
        assertTrue(inventarioAlertaService.esAgotado(producto(0)));
        assertFalse(inventarioAlertaService.esAgotado(producto(1)));
    }

    @Test
    void esAgotado_ignoraServicios() {
        Producto servicio = producto(0);
        servicio.setTipoItem(TipoItem.SERVICIO);
        assertFalse(inventarioAlertaService.esAgotado(servicio));
    }

    private Producto producto(int existencias) {
        Producto p = new Producto();
        p.setExistencias(existencias);
        return p;
    }
}
