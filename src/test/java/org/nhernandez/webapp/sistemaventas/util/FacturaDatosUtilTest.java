package org.nhernandez.webapp.sistemaventas.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacturaDatosUtilTest {

    @Test
    void esRfcValido_aceptaPersonaMoral() {
        assertTrue(FacturaDatosUtil.esRfcValido("XAXX010101000"));
    }

    @Test
    void esRfcValido_rechazaCorto() {
        assertFalse(FacturaDatosUtil.esRfcValido("ABC123"));
    }

    @Test
    void validarRfcObligatorio_retornaNullSiEsValido() {
        assertNull(FacturaDatosUtil.validarRfcObligatorio("XAXX010101000"));
    }
}
