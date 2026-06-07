package org.nhernandez.webapp.sistemaventas.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Evita 405 cuando el formulario hace POST a /login en lugar de /login/process.
 */
public class LoginPostRedirectFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest req) || !(response instanceof HttpServletResponse resp)) {
            chain.doFilter(request, response);
            return;
        }
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.isEmpty()) {
            path = "/";
        }
        if ("/login".equals(path) && "POST".equalsIgnoreCase(req.getMethod())) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }
        chain.doFilter(request, response);
    }
}
