package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorreoProduccionServiceTest {

    @Mock
    private RecuperacionCorreoService correoService;

    private CorreoProduccionService service;

    @BeforeEach
    void setUp() {
        service = new CorreoProduccionService(correoService);
        ReflectionTestUtils.setField(service, "envioTimeoutSegundos", 12);
    }

    @Test
    void evaluar_sinSmtp_marcaAdvertencias() {
        ReflectionTestUtils.setField(service, "smtpHost", "");
        ReflectionTestUtils.setField(service, "mailFrom", "");
        ReflectionTestUtils.setField(service, "appBaseUrl", "");

        var estado = service.evaluar();

        assertFalse(estado.isSmtpConfigurado());
        assertFalse(estado.isListoProduccion());
        assertTrue(estado.getAdvertencias().stream().anyMatch(a -> a.contains("SMTP_HOST")));
    }

    @Test
    void evaluar_smtpCompleto_listo() {
        ReflectionTestUtils.setField(service, "smtpHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(service, "smtpUser", "a@test.com");
        ReflectionTestUtils.setField(service, "smtpPassword", "secret");
        ReflectionTestUtils.setField(service, "mailFrom", "a@test.com");
        ReflectionTestUtils.setField(service, "appBaseUrl", "https://app.test");
        when(correoService.correoHabilitado()).thenReturn(true);

        var estado = service.evaluar();

        assertTrue(estado.isListoProduccion());
        assertTrue(estado.getAdvertencias().isEmpty());
    }

    @Test
    void enviarCorreoPrueba_propagaErrorSmtp() {
        when(correoService.enviarPrueba("bad@test.com"))
                .thenReturn(Optional.of(ResultadoEnvioCorreo.error("autenticacion rechazada")));

        String msg = service.enviarCorreoPrueba("bad@test.com");

        assertEquals("autenticacion rechazada", msg);
    }

    @Test
    void enviarCorreoPrueba_rechazaEmailInvalido() {
        String msg = service.enviarCorreoPrueba("no-es-correo");

        assertEquals("Correo de destino no valido.", msg);
    }
}
