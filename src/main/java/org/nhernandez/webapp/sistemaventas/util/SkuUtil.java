package org.nhernandez.webapp.sistemaventas.util;

public final class SkuUtil {

    /** Longitud maxima para SKU / codigo EAN-13. */
    public static final int LONGITUD_MAXIMA = 13;

    private SkuUtil() {
    }

    public static String normalizar(String sku) {
        if (sku == null) {
            return null;
        }
        String limpio = sku.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    public static boolean longitudValida(String sku) {
        String limpio = normalizar(sku);
        return limpio != null && limpio.length() <= LONGITUD_MAXIMA;
    }
}
