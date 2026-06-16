package org.nhernandez.webapp.sistemaventas.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TipoNegocioUtil {

    private static final Map<String, String> TIPOS = new LinkedHashMap<>();

    static {
        TIPOS.put("abarrotes", "Abarrotes / miscelanea");
        TIPOS.put("ferreteria", "Ferreteria");
        TIPOS.put("ropa", "Ropa y calzado");
        TIPOS.put("tecnologia", "Tecnologia");
        TIPOS.put("papeleria", "Papeleria");
        TIPOS.put("farmacia", "Farmacia");
        TIPOS.put("restaurante", "Restaurante / cafeteria");
        TIPOS.put("belleza", "Belleza y cuidado personal");
        TIPOS.put("regalo", "Regalos y detalles");
        TIPOS.put("servicios_profesionales", "Servicios profesionales / consultoria");
        TIPOS.put("otro", "Otro");
    }

    private TipoNegocioUtil() {
    }

    public static Map<String, String> opciones() {
        return TIPOS;
    }

    public static Set<String> codigosValidos() {
        return TIPOS.keySet();
    }

    public static String etiqueta(String codigo) {
        if (codigo == null) {
            return "";
        }
        return TIPOS.getOrDefault(codigo.trim().toLowerCase(Locale.ROOT), codigo);
    }

    public static boolean esValido(String codigo) {
        return codigo != null && TIPOS.containsKey(codigo.trim().toLowerCase(Locale.ROOT));
    }

    /** Rubros que al registrarse no cargan catalogo JSON de mercancia. */
    public static boolean importaCatalogoProductos(String codigo) {
        return !"servicios_profesionales".equals(normalizar(codigo));
    }

    /** Onboarding sugiere servicio como tipo por defecto. */
    public static boolean predominanServicios(String codigo) {
        String rubro = normalizar(codigo);
        return "servicios_profesionales".equals(rubro);
    }

    /**
     * Rubros con plantillas de servicio en catalogo (belleza, consultoria, etc.).
     * La agenda de citas solo aplica a estos negocios.
     */
    public static boolean tieneOpcionServicios(String codigo) {
        return switch (normalizar(codigo)) {
            case "belleza", "tecnologia", "restaurante", "regalo",
                 "servicios_profesionales", "otro" -> true;
            default -> false;
        };
    }

    /** Rubro con modulo de recetas, costos y margen por platillo. */
    public static boolean esRestaurante(String codigo) {
        return "restaurante".equals(normalizar(codigo));
    }

    private static String normalizar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return "otro";
        }
        return codigo.trim().toLowerCase(Locale.ROOT);
    }
}
