package org.nhernandez.webapp.sistemaventas.web;

import org.nhernandez.webapp.sistemaventas.security.UsuarioPrincipal;
import org.nhernandez.webapp.sistemaventas.util.PlataformaUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Punto de entrada de la PWA: abre panel si ya hay sesion o remember-me.
 */
@Controller
public class AppLaunchController {

    @GetMapping("/app")
    public String lanzar(Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
            if (PlataformaUtil.esOperadorPlataforma(principal.getUsuario())) {
                return "redirect:/plataforma";
            }
            return "redirect:/inicio";
        }
        return "redirect:/login";
    }
}
