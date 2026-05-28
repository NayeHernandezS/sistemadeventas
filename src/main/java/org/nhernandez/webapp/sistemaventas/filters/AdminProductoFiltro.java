package org.nhernandez.webapp.sistemaventas.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;

import java.io.IOException;
import java.util.Optional;

/**
 * Registrado solo desde {@link org.nhernandez.webapp.sistemaventas.config.FilterConfig}.
 */
public class AdminProductoFiltro implements Filter {

    private final LoginService auth;

    public AdminProductoFiltro(LoginService auth) {
        this.auth = auth;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        Optional<String> username = auth.getUsername(req);
        if (username.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede crear, editar o eliminar productos.");
            return;
        }
        chain.doFilter(request, response);
    }
}
