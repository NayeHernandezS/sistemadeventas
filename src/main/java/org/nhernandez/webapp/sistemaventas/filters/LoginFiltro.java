package org.nhernandez.webapp.sistemaventas.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.services.LoginService;

import java.io.IOException;
import java.util.Optional;

/**
 * Registrado solo desde {@link org.nhernandez.webapp.sistemaventas.config.FilterConfig}.
 */
public class LoginFiltro implements Filter {

    private final LoginService service;

    public LoginFiltro(LoginService service) {
        this.service = service;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Optional<String> username = service.getUsername((HttpServletRequest) request);
        if (username.isPresent()) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendRedirect(
                    ((HttpServletRequest) request).getContextPath() + "/login");
        }
    }
}
