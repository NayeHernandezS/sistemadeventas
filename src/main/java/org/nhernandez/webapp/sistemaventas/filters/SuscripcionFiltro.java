package org.nhernandez.webapp.sistemaventas.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nhernandez.webapp.sistemaventas.config.JdbcConnectionHolder;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/**
 * Registrado solo desde {@link org.nhernandez.webapp.sistemaventas.config.FilterConfig}.
 * Requiere que {@link ConexionFilter} haya abierto la conexion JDBC en la misma peticion.
 */
public class SuscripcionFiltro implements Filter {

    private final LoginService loginService;
    private final SuscripcionService suscripcionService;

    public SuscripcionFiltro(LoginService loginService, SuscripcionService suscripcionService) {
        this.loginService = loginService;
        this.suscripcionService = suscripcionService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (loginService.getUsername(req).isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        // Sin conexion JDBC (p. ej. rutas publicas de ConexionFilter): no evaluar suscripcion aqui.
        if (JdbcConnectionHolder.get() == null) {
            chain.doFilter(request, response);
            return;
        }

        String path = normalizarPath(req);
        String tenantOwner = TenantUtil.getTenantOwner(req);
        if (tenantOwner == null || tenantOwner.isBlank()) {
            cerrarSesion(req);
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }

        if (suscripcionService.tieneAccesoActivo(tenantOwner)) {
            chain.doFilter(request, response);
            return;
        }

        if (rutaPermitidaSinPlan(path, req)) {
            chain.doFilter(request, response);
            return;
        }

        if (RolUtil.esAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/suscripcion?requierePago=1");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/?sinPlan=1");
    }

    private static String normalizarPath(HttpServletRequest req) {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.isEmpty()) {
            return "/";
        }
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static boolean rutaPermitidaSinPlan(String path, HttpServletRequest req) {
        if (path.startsWith("/suscripcion")) {
            return true;
        }
        if (RolUtil.esAdmin(req) && path.startsWith("/admin/pagos")) {
            return true;
        }
        return false;
    }

    private static void cerrarSesion(HttpServletRequest req) {
        SecurityContextHolder.clearContext();
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
