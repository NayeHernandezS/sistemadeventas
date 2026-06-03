package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.models.SuscripcionCorreoTipo;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SuscripcionVencimientoCorreoPlantillaTest {

    @Test
    void construir_aviso7_incluyeFechaYEnlace() {
        var correo = SuscripcionVencimientoCorreoPlantilla.construir(
                "Mi SaaS", "https://app.test", "tienda1",
                LocalDateTime.of(2026, 6, 10, 12, 0),
                false, SuscripcionCorreoTipo.codigoAviso(7));

        assertTrue(correo.asunto().contains("7 dia"));
        assertTrue(correo.cuerpo().contains("https://app.test/suscripcion"));
        assertTrue(correo.cuerpo().contains("10/06/2026"));
    }

    @Test
    void construir_vencido_indicaAccesoLimitado() {
        var correo = SuscripcionVencimientoCorreoPlantilla.construir(
                "Mi SaaS", "https://app.test", "tienda1",
                LocalDateTime.of(2026, 6, 1, 0, 0),
                true, SuscripcionCorreoTipo.VENCIDO);

        assertTrue(correo.asunto().contains("ha vencido"));
        assertTrue(correo.cuerpo().contains("periodo de prueba"));
        assertTrue(correo.cuerpo().contains("acceso al sistema esta limitado"));
    }
}
