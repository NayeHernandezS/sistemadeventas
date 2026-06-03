package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroLegalServiceTest {

    private final RegistroLegalService service = new RegistroLegalService();

    @Test
    void validarAceptacion_rechazaSiFaltaAlguno() {
        var errores = service.validarAceptacion(null, "1");
        assertTrue(errores.containsKey("aceptaTerminos"));
        assertFalse(errores.containsKey("aceptaPrivacidad"));
    }

    @Test
    void validarAceptacion_okSiAmbosMarcados() {
        var errores = service.validarAceptacion("1", "on");
        assertTrue(errores.isEmpty());
    }
}
