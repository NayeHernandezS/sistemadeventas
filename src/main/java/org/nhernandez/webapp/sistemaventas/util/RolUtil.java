package org.nhernandez.webapp.sistemaventas.util;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.models.Usuario;

public final class RolUtil {

    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_VENDEDOR = "VENDEDOR";

    private RolUtil() {
    }

    public static boolean esAdmin(HttpServletRequest req) {
        Object rol = req.getSession().getAttribute("rol");
        return rol != null && ROL_ADMIN.equalsIgnoreCase(rol.toString());
    }

    public static boolean esAdmin(Usuario usuario) {
        return usuario != null && usuario.getRol() != null
                && ROL_ADMIN.equalsIgnoreCase(usuario.getRol());
    }
}
