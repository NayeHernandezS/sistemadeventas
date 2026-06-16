package org.nhernandez.webapp.sistemaventas.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    public static String whatsappMensaje() {
        return prop("soporte.whatsapp-mensaje",
                "Hola, me interesa FUSION DIGITAL (sistema de ventas). ¿Podrían darme más información?");
    }

    /** Enlace wa.me listo para botones; vacio si no hay numero configurado. */
    public static String whatsappEnlace() {
        String digits = whatsappDigitos();
        if (digits.isEmpty()) {
            return "";
        }
        String url = "https://wa.me/" + digits;
        String mensaje = whatsappMensaje();
        if (!mensaje.isBlank()) {
            String encoded = URLEncoder.encode(mensaje.trim(), StandardCharsets.UTF_8).replace("+", "%20");
            url += "?text=" + encoded;
        }
        return url;
    }

    public static String whatsappDigitos() {
        String raw = whatsapp();
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.replaceAll("\\D", "");
    }

    public static String horario() {
        return prop("soporte.horario", "Lunes a viernes, 9:00 - 18:00");
    }

    private static String prop(String key, String defaultValue) {
        String v = AppEnvironmentHolder.getProperty(key, defaultValue);
        return v != null ? v.trim() : defaultValue;
    }
}
