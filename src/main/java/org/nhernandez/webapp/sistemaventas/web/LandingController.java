package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.SoporteConfigUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    private final LoginService loginService;

    @Value("${soporte.email:soporte@fusiondigital.com}")
    private String soporteEmail;

    @Value("${suscripcion.meses.gratis:1}")
    private int mesesGratis;

    public LandingController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/")
    public String landing(HttpServletRequest req, Model model) {
        if (loginService.getUsername(req).isPresent()) {
            if (RolUtil.esSuperAdmin(req)) {
                return "redirect:/plataforma";
            }
            return "redirect:/inicio";
        }
        model.addAttribute("planes", PlanSuscripcion.todos());
        model.addAttribute("soporteEmail", soporteEmail);
        model.addAttribute("soporteWhatsapp", SoporteConfigUtil.whatsapp());
        model.addAttribute("soporteWhatsappUrl", SoporteConfigUtil.whatsappEnlace());
        model.addAttribute("mesesGratis", mesesGratis);
        return "landing";
    }
}
