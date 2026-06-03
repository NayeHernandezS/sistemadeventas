package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.models.SuscripcionCorreoTipo;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.SuscripcionCorreoEnviadoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.SuscripcionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuscripcionVencimientoCorreoServiceTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private UsuarioReposository usuarioRepository;

    @Mock
    private RecuperacionCorreoService correoService;

    @Mock
    private SuscripcionCorreoEnviadoRepository correoEnviadoRepository;

    private SuscripcionVencimientoCorreoService service;

    @BeforeEach
    void setUp() {
        service = new SuscripcionVencimientoCorreoService(
                suscripcionRepository, usuarioRepository, correoService, correoEnviadoRepository);
        ReflectionTestUtils.setField(service, "appNombre", "Test App");
        ReflectionTestUtils.setField(service, "appBaseUrl", "https://app.test");
        ReflectionTestUtils.setField(service, "diasAvisoUmbral", 7);
    }

    @Test
    void procesarAvisos_sinSmtp_noEnvia() throws SQLException {
        when(correoService.correoHabilitado()).thenReturn(false);

        int n = service.procesarAvisosManual();

        assertEquals(0, n);
        verify(suscripcionRepository, never()).listarVigentesQueVencenEn(any(Integer.class));
    }

    @Test
    void procesarAvisos_omiteSiYaEnviado() throws SQLException {
        when(correoService.correoHabilitado()).thenReturn(true);
        Suscripcion s = suscripcion("admin1", LocalDateTime.now().plusDays(7));
        when(suscripcionRepository.listarVigentesQueVencenEn(7)).thenReturn(List.of(s));
        when(suscripcionRepository.listarVigentesQueVencenEn(3)).thenReturn(List.of());
        when(suscripcionRepository.listarVigentesQueVencenEn(1)).thenReturn(List.of());
        when(suscripcionRepository.listarVigentesQueVencenEn(0)).thenReturn(List.of());
        when(suscripcionRepository.listarConFechaFinEnDiaPasado(1)).thenReturn(List.of());
        when(correoEnviadoRepository.yaEnviado(eq("admin1"), eq(SuscripcionCorreoTipo.codigoAviso(7)), any()))
                .thenReturn(true);

        int n = service.procesarAvisosManual();

        assertEquals(0, n);
        verify(correoService, never()).enviarTexto(any(), any(), any());
    }

    @Test
    void procesarAvisos_enviaYRegistra() throws SQLException {
        when(correoService.correoHabilitado()).thenReturn(true);
        LocalDateTime fin = LocalDateTime.of(2026, 6, 10, 18, 0);
        Suscripcion s = suscripcion("admin1", fin);
        when(suscripcionRepository.listarVigentesQueVencenEn(7)).thenReturn(List.of(s));
        when(suscripcionRepository.listarVigentesQueVencenEn(3)).thenReturn(List.of());
        when(suscripcionRepository.listarVigentesQueVencenEn(1)).thenReturn(List.of());
        when(suscripcionRepository.listarVigentesQueVencenEn(0)).thenReturn(List.of());
        when(suscripcionRepository.listarConFechaFinEnDiaPasado(1)).thenReturn(List.of());
        when(correoEnviadoRepository.yaEnviado(eq("admin1"), eq(SuscripcionCorreoTipo.codigoAviso(7)), any()))
                .thenReturn(false);
        Usuario u = new Usuario();
        u.setUsername("admin1");
        u.setEmail("admin@test.com");
        when(usuarioRepository.porUsername("admin1")).thenReturn(u);

        int n = service.procesarAvisosManual();

        assertEquals(1, n);
        verify(correoService).enviarTexto(eq("admin@test.com"), any(), any());
        verify(correoEnviadoRepository).registrar(eq("admin1"), eq(SuscripcionCorreoTipo.codigoAviso(7)), any());
    }

    @Test
    void diasParaEnviar_respetaUmbral() {
        ReflectionTestUtils.setField(service, "diasAvisoUmbral", 3);
        List<Integer> dias = service.diasParaEnviar();
        assertFalse(dias.contains(7));
        assertTrue(dias.contains(3));
        assertTrue(dias.contains(0));
    }

    private static Suscripcion suscripcion(String user, LocalDateTime fin) {
        Suscripcion s = new Suscripcion();
        s.setUsername(user);
        s.setFechaFin(fin);
        s.setEnPeriodoPrueba(false);
        s.setEstado("ACTIVA");
        return s;
    }
}
