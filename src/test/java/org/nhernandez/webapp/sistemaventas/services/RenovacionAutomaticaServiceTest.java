package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenovacionAutomaticaServiceTest {

    @Test
    void construirReferencia_formatoEsperado() {
        assertEquals("auto_tienda1__NEGOCIO",
                RenovacionAutomaticaService.construirReferencia("tienda1", "NEGOCIO"));
    }

    @Test
    void parsearReferencia_extraeTenantYPlan() {
        Optional<RenovacionAutomaticaService.AutoRef> ref =
                RenovacionAutomaticaService.parsearReferencia("auto_tienda1__PRO");

        assertTrue(ref.isPresent());
        assertEquals("tienda1", ref.get().username());
        assertEquals("PRO", ref.get().planCodigo());
    }

    @Test
    void parsearReferencia_rechazaReferenciaCheckout() {
        assertTrue(RenovacionAutomaticaService.parsearReferencia("pago_42").isEmpty());
    }

    @Test
    void parsearReferencia_rechazaFormatoInvalido() {
        assertTrue(RenovacionAutomaticaService.parsearReferencia("auto_solo").isEmpty());
    }
}
