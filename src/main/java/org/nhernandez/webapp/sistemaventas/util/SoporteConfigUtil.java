package org.nhernandez.webapp.sistemaventas.util;

import org.nhernandez.webapp.sistemaventas.config.AppEnvironmentHolder;

public final class SoporteConfigUtil {

    private SoporteConfigUtil() {
    }

    public static String email() {
        return prop("soporte.email", "soporte@fusiondigital.com");
    }

    public static String whatsapp() {
        return prop("soporte.whatsapp", "");
    }

    public static String horario() {
        return prop("soporte.horario", "Lunes a viernes, 9:00 - 18:00");
    }

    private static String prop(String key, String defaultValue) {
        String v = AppEnvironmentHolder.getProperty(key, defaultValue);
        return v != null ? v.trim() : defaultValue;
    }
}
