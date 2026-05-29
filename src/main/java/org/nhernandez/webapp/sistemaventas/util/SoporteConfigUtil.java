package org.nhernandez.webapp.sistemaventas.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class SoporteConfigUtil {

    private SoporteConfigUtil() {
    }

    public static String email() {
        return prop("soporte.email", "soporte@misistema.com");
    }

    public static String whatsapp() {
        return prop("soporte.whatsapp", "");
    }

    public static String horario() {
        return prop("soporte.horario", "Lunes a viernes, 9:00 - 18:00");
    }

    private static String prop(String key, String defaultValue) {
        String v = loadConfig().getProperty(key, defaultValue);
        return v != null ? v.trim() : defaultValue;
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream in = SoporteConfigUtil.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
        }
        return properties;
    }
}
