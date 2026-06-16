package org.nhernandez.webapp.sistemaventas.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.nhernandez.webapp.sistemaventas.security.UsuarioPrincipal;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/**
 * Tras remember-me o sesion parcial, repone atributos de tenant en la sesion HTTP.
 */
public class SessionTenantSyncFilter implements Filter {

    private final UsuarioService usuarioService;

    public SessionTenantSyncFilter(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req) {
            sincronizarSesion(req);
        }
        chain.doFilter(request, response);
    }

    private void sincronizarSesion(HttpServletRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return;
        }
        if (!(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
            return;
        }
        HttpSession session = req.getSession(true);
        if (session.getAttribute(TenantUtil.SESSION_TENANT) != null) {
            return;
        }
        TenantUtil.inicializarSesion(session, principal.getUsuario());
        usuarioService.registrarUltimoAcceso(principal.getUsername());
    }
}
