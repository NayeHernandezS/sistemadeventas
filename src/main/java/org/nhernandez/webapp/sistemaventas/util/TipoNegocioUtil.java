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
}
