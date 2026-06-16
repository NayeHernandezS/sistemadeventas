package org.nhernandez.webapp.sistemaventas.util;

import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.Producto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecetaRestauranteUtilTest {

    @Test
    void categoriasPlatilloEInsumo() {
        assertTrue(RecetaRestauranteUtil.esCategoriaPlatillo("Comida"));
        assertTrue(RecetaRestauranteUtil.esCategoriaInsumo("Insumos"));
        assertFalse(RecetaRestauranteUtil.esCategoriaPlatillo("Insumos"));
    }

    @Test
    void calcularCostoLinea_usaPrecioCompra() {
        Producto insumo = producto("Insumos", 0, 10);
        int costo = RecetaRestauranteUtil.calcularCostoLinea(insumo, new BigDecimal("2.5"));
        assertEquals(25, costo);
    }

    @Test
    void calcularMargen() {
        assertEquals(15, RecetaRestauranteUtil.calcularMargenPesos(50, 35));
        assertEquals(43, RecetaRestauranteUtil.calcularMargenPorcentaje(50, 35));
    }

    @Test
    void esRestaurante_enTipoNegocioUtil() {
        assertTrue(TipoNegocioUtil.esRestaurante("restaurante"));
        assertFalse(TipoNegocioUtil.esRestaurante("abarrotes"));
    }

    private static Producto producto(String categoria, int precio, int precioCompra) {
        Producto p = new Producto();
        Categoria cat = new Categoria();
        cat.setNombre(categoria);
        p.setCategoria(cat);
        p.setPrecio(precio);
        p.setPrecioCompra(precioCompra);
        return p;
    }
}
