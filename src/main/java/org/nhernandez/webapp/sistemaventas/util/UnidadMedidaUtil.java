package org.nhernandez.webapp.sistemaventas.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Set;

public final class UnidadMedidaUtil {

    public static final Set<String> UNIDADES = Set.of("pza", "kg", "g", "l", "ml");

    private UnidadMedidaUtil() {
    }

    public static String normalizar(String unidad) {
        if (unidad == null || unidad.isBlank()) {
            return "pza";
        }
        String u = unidad.trim().toLowerCase(Locale.ROOT);
        return UNIDADES.contains(u) ? u : "pza";
    }

    /** Convierte una cantidad en la unidad indicada a unidad base interna (g, ml o pza). */
    public static int aUnidadBase(BigDecimal cantidad, String unidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return switch (normalizar(unidad)) {
            case "kg" -> cantidad.multiply(BigDecimal.valueOf(1000))
                    .setScale(0, RoundingMode.HALF_UP).intValue();
            case "g" -> cantidad.setScale(0, RoundingMode.HALF_UP).intValue();
            case "l" -> cantidad.multiply(BigDecimal.valueOf(1000))
                    .setScale(0, RoundingMode.HALF_UP).intValue();
            case "ml" -> cantidad.setScale(0, RoundingMode.HALF_UP).intValue();
            default -> cantidad.setScale(0, RoundingMode.HALF_UP).intValue();
        };
    }

    public static int umbralAUnidadBase(int umbral, String unidadProducto) {
        if (umbral <= 0) {
            return 0;
        }
        return aUnidadBase(BigDecimal.valueOf(umbral), unidadProducto);
    }

    public static BigDecimal desdeUnidadBase(int base, String unidad) {
        if (base < 0) {
            base = 0;
        }
        return switch (normalizar(unidad)) {
            case "kg" -> BigDecimal.valueOf(base).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            case "g" -> BigDecimal.valueOf(base);
            case "l" -> BigDecimal.valueOf(base).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            case "ml" -> BigDecimal.valueOf(base);
            default -> BigDecimal.valueOf(base);
        };
    }

    public static String formatear(int base, String unidad) {
        BigDecimal valor = desdeUnidadBase(base, unidad);
        return valor.stripTrailingZeros().toPlainString() + " " + normalizar(unidad);
    }

    public static boolean admiteDecimales(String unidad) {
        String u = normalizar(unidad);
        return "kg".equals(u) || "l".equals(u);
    }
}
