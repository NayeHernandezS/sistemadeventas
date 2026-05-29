package org.nhernandez.webapp.sistemaventas.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.nhernandez.webapp.sistemaventas.util.PlataformaUtil;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TenantAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final SuscripcionService suscripcionService;

    public TenantAuthenticationSuccessHandler(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        Usuario usuario = principal.getUsuario();
        String tenant = TenantUtil.resolverTenant(usuario);
        if (tenant == null || tenant.isBlank()) {
            SecurityContextHolder.clearContext();
            request.getSession().invalidate();
            getRedirectStrategy().sendRedirect(request, response,
                    request.getContextPath() + "/login?error=tenant");
            return;
        }

        TenantUtil.inicializarSesion(request.getSession(), usuario);

        if (PlataformaUtil.esOperadorPlataforma(usuario)) {
            getRedirectStrategy().sendRedirect(request, response, request.getContextPath() + "/plataforma");
            return;
        }

        if (!suscripcionService.tieneAccesoActivo(tenant)) {
            String destino = RolUtil.esAdmin(usuario)
                    ? "/suscripcion?requierePago=1"
                    : "/?sinPlan=1";
            getRedirectStrategy().sendRedirect(request, response, request.getContextPath() + destino);
            return;
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
