package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private OnboardingService service;

    @BeforeEach
    void setUp() {
        service = new OnboardingService(
                preferenciasTenantService, planLimiteService, categoriaService,
                productoService, usuarioService);
    }

    @Test
    void requiereOnboarding_falseSiCompletado() {
        when(preferenciasTenantService.onboardingCompletado("t1")).thenReturn(true);
        assertFalse(service.requiereOnboarding("t1"));
    }

    @Test
    void requiereOnboarding_falseSiYaTieneProductos() {
        when(preferenciasTenantService.onboardingCompletado("t1")).thenReturn(false);
        when(planLimiteService.contarProductos("t1")).thenReturn(2);
        assertFalse(service.requiereOnboarding("t1"));
        verify(preferenciasTenantService).marcarOnboardingCompletado("t1");
    }

    @Test
    void requiereOnboarding_trueSinProductosNiFlag() {
        when(preferenciasTenantService.onboardingCompletado("t1")).thenReturn(false);
        when(planLimiteService.contarProductos("t1")).thenReturn(0);
        assertTrue(service.requiereOnboarding("t1"));
    }

    @Test
    void validarPrimerProducto_rechazaPrecioCero() {
        var errores = service.validarPrimerProducto("Agua", "A1", "0", "5", 1L);
        assertTrue(errores.containsKey("precio"));
    }
}
