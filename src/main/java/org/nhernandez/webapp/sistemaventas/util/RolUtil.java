package org.nhernandez.webapp.sistemaventas.util;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class RolUtil {

    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_VENDEDOR = "VENDEDOR";
    public static final String AUTHORITY_ADMIN = "ROLE_ADMIN";

    private RolUtil() {
    }

    public static boolean esAdmin(HttpServletRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch(AUTHORITY_ADMIN::equals)) {
            return true;
        }
        Object rol = req.getSession().getAttribute("rol");
        return rol != null && ROL_ADMIN.equalsIgnoreCase(rol.toString());
    }

    public static boolean esAdmin(Usuario usuario) {
        return usuario != null && usuario.getRol() != null
                && ROL_ADMIN.equalsIgnoreCase(usuario.getRol());
    }
}
