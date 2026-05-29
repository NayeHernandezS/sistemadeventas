package org.nhernandez.webapp.sistemaventas.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.nhernandez.webapp.sistemaventas.models.Usuario;

public final class TenantUtil {

    public static final String SESSION_TENANT = "tenantOwner";

    private TenantUtil() {
    }

    public static void inicializarSesion(HttpSession session, Usuario usuario) {
        session.setAttribute("username", usuario.getUsername());
        if (PlataformaUtil.esOperadorPlataforma(usuario)) {
            session.setAttribute("rol", RolUtil.ROL_SUPER_ADMIN);
        } else {
            session.setAttribute("rol", usuario.getRol());
        }
        session.setAttribute(SESSION_TENANT, resolverTenant(usuario));
    }

    public static String resolverTenant(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        if (PlataformaUtil.esOperadorPlataforma(usuario)) {
            return usuario.getUsername();
        }
        if (RolUtil.esAdmin(usuario)) {
            return usuario.getUsername();
        }
        return usuario.getAdminOwner();
    }

    public static String getTenantOwner(HttpServletRequest req) {
        Object tenant = req.getSession().getAttribute(SESSION_TENANT);
        return tenant != null ? tenant.toString() : null;
    }

    public static boolean perteneceAlTenant(Usuario usuario, String tenantOwner) {
        if (usuario == null || tenantOwner == null || tenantOwner.isBlank()) {
            return false;
        }
        if (tenantOwner.equals(usuario.getUsername()) && RolUtil.esAdmin(usuario)) {
            return true;
        }
        return tenantOwner.equals(usuario.getAdminOwner());
    }
}
