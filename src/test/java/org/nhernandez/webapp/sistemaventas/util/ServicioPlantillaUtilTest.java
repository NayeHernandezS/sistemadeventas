package org.nhernandez.webapp.sistemaventas.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioPlantillaUtilTest {

    @Test
    void belleza_incluyeServiciosDeSalon() {
        var sugerencias = ServicioPlantillaUtil.sugerenciasParaRubro("belleza");
        assertTrue(sugerencias.stream().anyMatch(s -> s.nombre().contains("Corte")));
        assertTrue(sugerencias.stream().anyMatch(s -> "Unas".equals(s.categoria())));
    }

    @Test
    void serviciosProfesionales_incluyeConsultoriaLegal() {
        var sugerencias = ServicioPlantillaUtil.sugerenciasParaRubro("servicios_profesionales");
        assertTrue(sugerencias.stream().anyMatch(s -> s.nombre().toLowerCase().contains("legal")));
    }

    @Test
    void tecnologia_incluyeReparaciones() {
        var sugerencias = ServicioPlantillaUtil.sugerenciasParaRubro("tecnologia");
        assertFalse(sugerencias.isEmpty());
        assertTrue(sugerencias.stream().anyMatch(s -> s.nombre().toLowerCase().contains("reparacion")));
    }

    @Test
    void todasCategorias_incluyeProductoYServicio() {
        var todas = CategoriaPlantillaUtil.todasCategoriasParaTipoNegocio("belleza");
        assertTrue(todas.contains("Cabello"));
        assertTrue(todas.contains("Unas"));
    }
}
