package org.nhernandez.webapp.sistemaventas.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;

import java.io.IOException;

/**
 * Registrado solo desde {@link org.nhernandez.webapp.sistemaventas.config.FilterConfig}.
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
        String username = loginService.getUsername(req).orElse(null);
        if (username == null) {
            chain.doFilter(request, response);
            return;
        }

        String tenantOwner = TenantUtil.getTenantOwner(req);
        if (tenantOwner == null || tenantOwner.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (suscripcionService.tieneAccesoActivo(tenantOwner)) {
            chain.doFilter(request, response);
            return;
        }

        if (RolUtil.esAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/suscripcion?requierePago=1");
        } else {
            resp.sendRedirect(req.getContextPath() + "/?sinPlan=1");
        }
    }
}
