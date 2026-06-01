package org.nhernandez.webapp.sistemaventas.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.services.PlanLimiteService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.config.JdbcConnectionHolder;
import org.nhernandez.webapp.sistemaventas.support.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class PlanLimiteIntegracionTest extends IntegrationTestBase {

    @Autowired
    private PlanLimiteService planLimiteService;

    @Test
    void evaluarPlanesContratables_bloqueaEmprendedorPorVendedores() throws Exception {
        withJdbcRollback(() -> {
            var evaluaciones = planLimiteService.evaluarPlanesContratables("negocio_test");

            assertFalse(evaluaciones.get("EMPRENDEDOR").isContratable());
            assertTrue(evaluaciones.get("NEGOCIO").isContratable());
            assertTrue(evaluaciones.get("PRO").isContratable());
            assertTrue(evaluaciones.get("EMPRENDEDOR").getMotivos().getFirst().contains("vendedores"));
        });
    }

    @Test
    void evaluarPlanesContratables_bloqueaPorExcesoDeProductos() throws Exception {
        withJdbcRollback(() -> {
            try (var stmt = JdbcConnectionHolder.require().createStatement()) {
                stmt.execute("""
                        INSERT INTO productos (nombre, precio, existencias, sku, categoria_id, fecha_registro, owner_username)
                        SELECT 'Prod ' || x, 10, 1, CONCAT('PX', x), 2, CURRENT_DATE, 'negocio_test'
                        FROM SYSTEM_RANGE(1, 160)
                        """);
            }

            var evaluacion = planLimiteService.evaluarPlanContratable("negocio_test", "EMPRENDEDOR");

            assertFalse(evaluacion.isContratable());
            assertTrue(evaluacion.getMotivos().stream().anyMatch(m -> m.contains("productos")));
        });
    }

    @Test
    void validarPlanContratable_lanzaExcepcionConMensajeClaro() throws Exception {
        withJdbcRollback(() -> {
            ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                    () -> planLimiteService.validarPlanContratable("negocio_test", "EMPRENDEDOR"));

            assertTrue(ex.getMessage().contains("vendedores"));
        });
    }
}
