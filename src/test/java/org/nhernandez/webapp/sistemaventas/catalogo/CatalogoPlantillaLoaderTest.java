package org.nhernandez.webapp.sistemaventas.catalogo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogoPlantillaLoaderTest {

    @Test
    void cargar_abarrotesIncluyeMarcasPrincipales() {
        var catalogo = CatalogoPlantillaLoader.cargar("abarrotes");
        assertTrue(catalogo.isPresent());
        assertEquals("abarrotes", catalogo.get().rubro());
        assertEquals(36, catalogo.get().productos().size());
        String nombres = catalogo.get().productos().stream()
                .map(p -> p.nombre().toLowerCase())
                .reduce("", (a, b) -> a + " " + b);
        assertTrue(nombres.contains("coca-cola"));
        assertTrue(nombres.contains("sabritas"));
        assertTrue(nombres.contains("takis"));
        assertTrue(nombres.contains("bimbo"));
        assertTrue(nombres.contains("marinela"));
    }

    @Test
    void cargar_rubroInvalidoUsaOtro() {
        var catalogo = CatalogoPlantillaLoader.cargar("no-existe");
        assertTrue(catalogo.isPresent());
        assertEquals("otro", catalogo.get().rubro());
        assertEquals(30, catalogo.get().productos().size());
    }

    @Test
    void cargar_todosLosRubrosDefinidos() {
        assertEquals(36, CatalogoPlantillaLoader.cargar("abarrotes").orElseThrow().productos().size());
        for (String rubro : new String[]{
                "ferreteria", "ropa", "tecnologia", "papeleria",
                "farmacia", "restaurante", "belleza", "regalo", "otro"
        }) {
            var catalogo = CatalogoPlantillaLoader.cargar(rubro);
            assertTrue(catalogo.isPresent(), rubro);
            assertEquals(30, catalogo.get().productos().size(), rubro);
            assertFalse(catalogo.get().productos().stream()
                    .anyMatch(p -> p.sku() == null || p.sku().length() > 10), rubro);
        }
    }
}
