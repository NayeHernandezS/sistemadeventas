package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.repositories.PagoSuscripcionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.SuscripcionRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuscripcionServiceImplTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private PagoSuscripcionRepository pagoRepository;

    @InjectMocks
    private SuscripcionServiceImpl suscripcionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(suscripcionService, "mesesGratis", 1);
    }

    @Test
    void calcularMonto_multiplicaPrecioDelPlanPorMeses() {
        BigDecimal monto = suscripcionService.calcularMonto("EMPRENDEDOR", 2);

        assertEquals(new BigDecimal("298.00"), monto);
    }

    @Test
    void tieneAccesoActivo_trueCuandoFechaFinEsFutura() {
        Suscripcion vigente = new Suscripcion();
        vigente.setFechaFin(LocalDateTime.now().plusDays(5));
        when(suscripcionRepository.porUsername("admin1")).thenReturn(vigente);

        assertTrue(suscripcionService.tieneAccesoActivo("admin1"));
    }

    @Test
    void tieneAccesoActivo_falseCuandoSuscripcionVencida() {
        Suscripcion vencida = new Suscripcion();
        vencida.setFechaFin(LocalDateTime.now().minusDays(1));
        when(suscripcionRepository.porUsername("admin1")).thenReturn(vencida);

        assertFalse(suscripcionService.tieneAccesoActivo("admin1"));
    }

    @Test
    void solicitarPago_rechazaMesesInvalidos() {
        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> suscripcionService.solicitarPago("admin1", 0, "PRO"));

        assertTrue(ex.getMessage().contains("entre 1 y 24"));
        verify(pagoRepository, never()).guardar(any());
    }

    @Test
    void solicitarPago_rechazaSiYaHayPagoPendiente() throws SQLException {
        PagoSuscripcion pendiente = new PagoSuscripcion();
        pendiente.setEstado("PENDIENTE");
        when(pagoRepository.listarPorUsername("admin1")).thenReturn(List.of(pendiente));

        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> suscripcionService.solicitarPago("admin1", 3, "NEGOCIO"));

        assertTrue(ex.getMessage().contains("pago pendiente"));
        verify(pagoRepository, never()).guardar(any());
    }

    @Test
    void solicitarPago_registraPagoConMontoCalculado() throws SQLException {
        when(pagoRepository.listarPorUsername("admin1")).thenReturn(List.of());

        suscripcionService.solicitarPago("admin1", 2, "PRO");

        verify(pagoRepository).guardar(any(PagoSuscripcion.class));
    }

    @Test
    void confirmarPagoPlataforma_extiendeSuscripcion() throws SQLException {
        PagoSuscripcion pago = new PagoSuscripcion();
        pago.setId(7L);
        pago.setUsername("admin1");
        pago.setMeses(3);
        pago.setPlanCodigo("NEGOCIO");
        pago.setEstado("PENDIENTE");

        Suscripcion actual = new Suscripcion();
        actual.setUsername("admin1");
        actual.setFechaFin(LocalDateTime.now().plusDays(10));
        actual.setPlanCodigo("EMPRENDEDOR");

        when(pagoRepository.porId(7L)).thenReturn(pago);
        when(suscripcionRepository.porUsername("admin1")).thenReturn(actual);

        suscripcionService.confirmarPagoPlataforma(7L);

        verify(pagoRepository).confirmar(7L);
        verify(suscripcionRepository).extenderVigencia(eq("admin1"), any(LocalDateTime.class), eq(false));
        verify(suscripcionRepository).actualizarPlan("admin1", "NEGOCIO", false);
    }

    @Test
    void confirmarPagoPlataforma_rechazaPagoNoPendiente() throws SQLException {
        PagoSuscripcion pago = new PagoSuscripcion();
        pago.setEstado("CONFIRMADO");
        when(pagoRepository.porId(anyLong())).thenReturn(pago);

        assertThrows(ServiceJdbcException.class,
                () -> suscripcionService.confirmarPagoPlataforma(99L));
    }
}
