package org.nhernandez.webapp.sistemaventas.util;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.models.Usuario;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Identifica al operador de la plataforma (creadora del SaaS).
 * Puede ser por rol SUPER_ADMIN en BD o por lista en application.properties.
 */
public final class PlataformaUtil {

    private static final String PROP_SUPERADMINS = "plataforma.superadmins";

    private PlataformaUtil() {
    }

    public static boolean esOperadorPlataforma(Usuario usuario) {
        if (usuario == null || usuario.getUsername() == null) {
            return false;
        }
        if (usuario.getRol() != null
                && RolUtil.ROL_SUPER_ADMIN.equalsIgnoreCase(usuario.getRol().trim())) {
            return true;
        }
        return usuariosConfigurados().contains(usuario.getUsername().trim().toLowerCase());
    }

    public static boolean esOperadorPlataforma(HttpServletRequest req) {
        Object rol = req.getSession().getAttribute("rol");
        if (rol != null && RolUtil.ROL_SUPER_ADMIN.equalsIgnoreCase(rol.toString())) {
            return true;
        }
        Object username = req.getSession().getAttribute("username");
        if (username != null) {
            return usuariosConfigurados().contains(username.toString().trim().toLowerCase());
        }
        return false;
    }

    private static Set<String> usuariosConfigurados() {
        String raw = loadConfig().getProperty(PROP_SUPERADMINS, "").trim();
        if (raw.isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream in = PlataformaUtil.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
        }
        return properties;
    }
}
