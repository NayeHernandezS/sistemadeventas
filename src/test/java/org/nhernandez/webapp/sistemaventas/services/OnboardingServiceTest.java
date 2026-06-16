package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.models.TipoItem;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private PreferenciasTenantService preferenciasTenantService;

    @Mock
    private PlanLimiteService planLimiteService;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private ProductoService productoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private VentaService ventaService;

    private OnboardingService service;

    @BeforeEach
    void setUp() {
        service = new OnboardingService(
                preferenciasTenantService, planLimiteService, categoriaService,
                productoService, usuarioService, ticketRepository, ventaService);
    }

    @Test
    void requiereOnboarding_falseSiCompletado() {
        when(preferenciasTenantService.onboardingCompletado("t1")).thenReturn(true);
        assertFalse(service.requiereOnboarding("t1"));
    }

    @Test
    void requiereOnboarding_trueConCatalogoImportadoSiNoCompletado() {
        when(preferenciasTenantService.onboardingCompletado("t1")).thenReturn(false);
        assertTrue(service.requiereOnboarding("t1"));
    }

    @Test
    void requiereOnboarding_trueSinProductosNiFlag() {
        when(preferenciasTenantService.onboardingCompletado("t1")).thenReturn(false);
        assertTrue(service.requiereOnboarding("t1"));
    }

    @Test
    void estadoActivacion_marcaPrimeraVenta() throws SQLException {
        when(planLimiteService.contarProductos("t1")).thenReturn(3);
        when(preferenciasTenantService.onboardingCompletado("t1")).thenReturn(true);
        when(ticketRepository.contarActivosPorTenant("t1")).thenReturn(1);

        var estado = service.estadoActivacion("t1");

        assertTrue(estado.isCatalogoListo());
        assertTrue(estado.isPrimeraVentaRegistrada());
        assertTrue(estado.isActivacionCompleta());
    }

    @Test
    void registrarVentaPractica_creaTicketDeUnProducto() {
        Producto producto = new Producto();
        producto.setId(5L);
        producto.setNombre("Refresco");
        producto.setPrecio(18);
        producto.setTipoItem(TipoItem.PRODUCTO);
        producto.setExistencias(10);

        TicketVenta guardado = new TicketVenta();
        guardado.setId(99L);
        guardado.setFolio("TCK-TEST");

        when(productoService.porIdPorOwner(5L, "t1")).thenReturn(Optional.of(producto));
        when(ventaService.registrarVenta(any(TicketVenta.class), isNull())).thenReturn(guardado);

        TicketVenta ticket = service.registrarVentaPractica("t1", "admin", 5L);

        assertEquals(99L, ticket.getId());
        verify(ventaService).registrarVenta(any(TicketVenta.class), isNull());
    }

    @Test
    void validarPrimerProducto_rechazaPrecioCero() {
        var errores = service.validarPrimerProducto("Agua", "A1", "0", "5", 1L);
        assertTrue(errores.containsKey("precio"));
    }

    @Test
    void validarPrimerItem_servicioNoRequiereSku() {
        var errores = service.validarPrimerItem(
                TipoItem.SERVICIO.name(), "Consultoria legal", "", "500", "0", 1L);
        assertFalse(errores.containsKey("sku"));
        assertFalse(errores.containsKey("existencias"));
    }

    @Test
    void validarPrimerItem_productoRequiereSku() {
        var errores = service.validarPrimerItem(
                TipoItem.PRODUCTO.name(), "Agua", "", "15", "10", 1L);
        assertTrue(errores.containsKey("sku"));
    }
}
