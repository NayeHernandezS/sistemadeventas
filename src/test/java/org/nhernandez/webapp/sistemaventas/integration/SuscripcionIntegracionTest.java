package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class SuscripcionIntegracionTest extends IntegrationTestBase {

    @Autowired
    private SuscripcionService suscripcionService;

    @Test
    void suspenderCuenta_bloqueaAccesoAunConFechaVigente() throws Exception {
        withJdbcRollback(() -> {
            assertTrue(suscripcionService.tieneAccesoActivo("tienda1"));

            suscripcionService.suspenderCuenta("tienda1");

            assertFalse(suscripcionService.tieneAccesoActivo("tienda1"));
            Suscripcion s = suscripcionService.consultar("tienda1").orElseThrow();
            assertEquals("SUSPENDIDA", s.getEstado());
            assertTrue(s.getFechaFin().isAfter(LocalDateTime.now()));
        });
    }

    @Test
    void reactivarCuenta_restauraAcceso() throws Exception {
        withJdbcRollback(() -> {
            suscripcionService.suspenderCuenta("tienda1");
            assertFalse(suscripcionService.tieneAccesoActivo("tienda1"));

            suscripcionService.reactivarCuenta("tienda1");

            assertTrue(suscripcionService.tieneAccesoActivo("tienda1"));
            assertEquals("ACTIVA", suscripcionService.consultar("tienda1").orElseThrow().getEstado());
        });
    }

    @Test
    void extenderSuscripcionMeses_prolongaFechaFin() throws Exception {
        withJdbcRollback(() -> {
            LocalDateTime antes = suscripcionService.consultar("tienda1").orElseThrow().getFechaFin();

            suscripcionService.extenderSuscripcionMeses("tienda1", 2);

            LocalDateTime despues = suscripcionService.consultar("tienda1").orElseThrow().getFechaFin();
            assertTrue(despues.isAfter(antes));
        });
    }

    @Test
    void cambiarPlanPlataforma_rechazaDowngradePorExcesoDeVendedores() throws Exception {
        withJdbcRollback(() -> {
            ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                    () -> suscripcionService.cambiarPlanPlataforma("negocio_test", "EMPRENDEDOR"));

            assertTrue(ex.getMessage().contains("vendedores"));
            assertEquals("NEGOCIO", suscripcionService.consultar("negocio_test").orElseThrow().getPlanCodigo());
        });
    }

    @Test
    void cambiarPlanPlataforma_permitePlanCompatible() throws Exception {
        withJdbcRollback(() -> {
            suscripcionService.cambiarPlanPlataforma("negocio_test", "PRO");

            assertEquals("PRO", suscripcionService.consultar("negocio_test").orElseThrow().getPlanCodigo());
        });
    }

    @Test
    void confirmarPagoPlataforma_extiendeSuscripcionYConfirmaPago() throws Exception {
        withJdbcRollback(() -> {
            LocalDateTime antes = suscripcionService.consultar("tienda1").orElseThrow().getFechaFin();
            suscripcionService.solicitarPago("tienda1", 1, "NEGOCIO");
            var pagos = suscripcionService.pagosPendientesDelTenant("tienda1");
            assertEquals(1, pagos.size());

            suscripcionService.confirmarPagoPlataforma(pagos.getFirst().getId());

            assertEquals("NEGOCIO", suscripcionService.consultar("tienda1").orElseThrow().getPlanCodigo());
            LocalDateTime despues = suscripcionService.consultar("tienda1").orElseThrow().getFechaFin();
            assertTrue(despues.isAfter(antes));
            assertTrue(suscripcionService.pagosDelUsuario("tienda1").stream()
                    .anyMatch(p -> "CONFIRMADO".equals(p.getEstado())));
        });
    }
}
