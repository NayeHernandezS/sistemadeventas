package org.nhernandez.webapp.sistemaventas.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoNegocioUtilTest {

    @Test
    void tieneOpcionServicios_rubrosConAgenda() {
        assertTrue(TipoNegocioUtil.tieneOpcionServicios("belleza"));
        assertTrue(TipoNegocioUtil.tieneOpcionServicios("servicios_profesionales"));
        assertTrue(TipoNegocioUtil.tieneOpcionServicios("otro"));
    }

    @Test
    void tieneOpcionServicios_sinAgenda() {
        assertFalse(TipoNegocioUtil.tieneOpcionServicios("ferreteria"));
        assertFalse(TipoNegocioUtil.tieneOpcionServicios("abarrotes"));
        assertFalse(TipoNegocioUtil.tieneOpcionServicios("farmacia"));
        assertFalse(TipoNegocioUtil.tieneOpcionServicios("ropa"));
        assertFalse(TipoNegocioUtil.tieneOpcionServicios("papeleria"));
    }
}
