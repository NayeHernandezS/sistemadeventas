package org.nhernandez.webapp.sistemaventas.util;

import org.nhernandez.webapp.sistemaventas.models.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Set;

public final class RecetaRestauranteUtil {

    private static final Set<String> CATEGORIAS_INSUMO = Set.of("insumos", "desechables");
    private static final Set<String> CATEGORIAS_PLATILLO = Set.of("comida", "bebidas", "postres");

    private RecetaRestauranteUtil() {
    }

    public static boolean esCategoriaInsumo(String nombreCategoria) {
        return nombreCategoria != null
                && CATEGORIAS_INSUMO.contains(nombreCategoria.trim().toLowerCase(Locale.ROOT));
    }

    public static boolean esCategoriaPlatillo(String nombreCategoria) {
        return nombreCategoria != null
                && CATEGORIAS_PLATILLO.contains(nombreCategoria.trim().toLowerCase(Locale.ROOT));
    }

    public static boolean esInsumo(Producto producto) {
        return producto != null
                && producto.esProducto()
                && producto.getCategoria() != null
                && esCategoriaInsumo(producto.getCategoria().getNombre());
    }

    public static boolean esPlatillo(Producto producto) {
        return producto != null
                && producto.esProducto()
                && producto.getCategoria() != null
                && esCategoriaPlatillo(producto.getCategoria().getNombre());
    }

    /** Costo unitario del insumo: precio de compra; si no hay, precio de venta del insumo. */
    public static int costoUnitarioInsumo(Producto insumo) {
        if (insumo == null) {
            return 0;
        }
        if (insumo.getPrecioCompra() > 0) {
            return insumo.getPrecioCompra();
        }
        return Math.max(0, insumo.getPrecio());
    }

    public static int calcularCostoLinea(Producto insumo, BigDecimal cantidad) {
        if (insumo == null || cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal costo = cantidad.multiply(BigDecimal.valueOf(costoUnitarioInsumo(insumo)));
        return costo.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public static int calcularMargenPesos(int precioVenta, int costoReceta) {
        if (precioVenta <= 0 || costoReceta <= 0) {
            return 0;
        }
        return precioVenta - costoReceta;
    }

    public static int calcularMargenPorcentaje(int precioVenta, int costoReceta) {
        if (costoReceta <= 0 || precioVenta <= 0) {
            return 0;
        }
        return Math.round((calcularMargenPesos(precioVenta, costoReceta) * 100f) / costoReceta);
    }
}
