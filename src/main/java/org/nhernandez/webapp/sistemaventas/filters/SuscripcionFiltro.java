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
 * <p>
 * Usuarios autenticados sin suscripcion activa solo pueden acceder a rutas de renovacion,
 * soporte y cierre de sesion; el resto del sistema queda bloqueado.
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

        if (esRutaExenta(req)) {
            chain.doFilter(request, response);
            return;
        }

        if (loginService.getUsername(req).isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        if (RolUtil.esSuperAdmin(req)) {
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
            if ("/inicio".equals(path) && RolUtil.esAdmin(req)) {
                String query = suscripcionService.consultar(tenantOwner).isEmpty()
                        ? "eligePlan=1"
                        : "requierePago=1";
                resp.sendRedirect(req.getContextPath() + "/suscripcion?" + query);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        if (RolUtil.esAdmin(req)) {
            String query = suscripcionService.consultar(tenantOwner).isEmpty()
                    ? "eligePlan=1"
                    : "requierePago=1";
            resp.sendRedirect(req.getContextPath() + "/suscripcion?" + query);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/inicio?sinPlan=1");
    }

    static String normalizarPath(HttpServletRequest req) {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.isEmpty()) {
            return "/";
        }
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Rutas que no requieren evaluacion de suscripcion (publicas o estaticas).
     */
    static boolean esRutaExenta(HttpServletRequest req) {
        String path = normalizarPath(req);
        String method = req.getMethod();
        if (path.equals("/login") || path.equals("/login.html") || path.equals("/login/process")) {
            return true;
        }
        if (path.equals("/registro") || path.startsWith("/registro/")) {
            return "GET".equalsIgnoreCase(method);
        }
        if (path.equals("/")) {
            return "GET".equalsIgnoreCase(method);
        }
        if (path.startsWith("/recuperar")) {
            return true;
        }
        if (path.equals("/logout") || path.equals("/error") || path.equals("/acceso-denegado")) {
            return true;
        }
        return path.startsWith("/css/")
                || path.startsWith("/img/")
                || esRecursoEstatico(path);
    }

    private static boolean esRecursoEstatico(String path) {
        return path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".svg")
                || path.endsWith(".png")
                || path.endsWith(".webmanifest")
                || path.equals("/manifest.webmanifest")
                || path.equals("/sw.js");
    }

    /**
     * Rutas accesibles con suscripcion vencida (renovacion, soporte, aviso sin plan).
     * Ventas, inventario, reportes y demas modulos requieren plan activo.
     * Perfil permite gestionar cuenta (email, contraseña) aunque el plan este vencido.
     */
    static boolean rutaPermitidaSinPlan(String path, HttpServletRequest req) {
        if (path.equals("/inicio") || path.equals("/index.jsp")) {
            return true;
        }
        if (path.equals("/perfil") || path.startsWith("/perfil/")) {
            return true;
        }
        if (path.startsWith("/suscripcion")) {
            return true;
        }
        if (path.startsWith("/onboarding")) {
            return true;
        }
        if (path.startsWith("/soporte")) {
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
