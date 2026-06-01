package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuscripcionAvisoServiceTest {

    @Mock
    private SuscripcionService suscripcionService;

    @InjectMocks
    private SuscripcionAvisoService avisoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(avisoService, "diasAviso", 7);
    }

    @Test
    void evaluar_generaAvisoCuandoFaltanTresDias() {
        Suscripcion s = new Suscripcion();
        s.setFechaFin(LocalDateTime.now().plusDays(3));
        s.setEnPeriodoPrueba(false);
        when(suscripcionService.consultar("tienda1")).thenReturn(Optional.of(s));

        var aviso = avisoService.evaluar("tienda1");

        assertTrue(aviso.isPresent());
        assertEquals(3, aviso.get().getDiasRestantes());
        assertEquals("warning", aviso.get().getNivel());
    }

    @Test
    void evaluar_vacioCuandoFaltanMasDeSieteDias() {
        Suscripcion s = new Suscripcion();
        s.setFechaFin(LocalDate.now().plusDays(15).atStartOfDay());
        when(suscripcionService.consultar("tienda1")).thenReturn(Optional.of(s));

        assertTrue(avisoService.evaluar("tienda1").isEmpty());
    }

    @Test
    void evaluar_nivelCriticoCuandoVenceHoy() {
        Suscripcion s = new Suscripcion();
        s.setFechaFin(LocalDateTime.now().plusHours(5));
        s.setEnPeriodoPrueba(true);
        when(suscripcionService.consultar("tienda1")).thenReturn(Optional.of(s));

        var aviso = avisoService.evaluar("tienda1");

        assertTrue(aviso.isPresent());
        assertEquals("danger", aviso.get().getNivel());
        assertTrue(aviso.get().getMensaje().contains("periodo de prueba"));
    }
}
