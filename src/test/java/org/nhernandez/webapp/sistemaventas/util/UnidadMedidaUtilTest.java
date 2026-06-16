package org.nhernandez.webapp.sistemaventas.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnidadMedidaUtilTest {

    @Test
    void aUnidadBase_convierteKgAGramos() {
        assertEquals(2500, UnidadMedidaUtil.aUnidadBase(new BigDecimal("2.5"), "kg"));
    }

    @Test
    void formatear_muestraKgLegible() {
        assertEquals("2.5 kg", UnidadMedidaUtil.formatear(2500, "kg"));
    }

    @Test
    void umbralAUnidadBase_respetaUnidadProducto() {
        assertEquals(5000, UnidadMedidaUtil.umbralAUnidadBase(5, "kg"));
        assertEquals(5, UnidadMedidaUtil.umbralAUnidadBase(5, "pza"));
    }

    @Test
    void desdeUnidadBase_redondeaKg() {
        assertTrue(UnidadMedidaUtil.desdeUnidadBase(1500, "kg").compareTo(new BigDecimal("1.5")) == 0);
    }
}
