package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.services.PlanLimiteService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final PlanLimiteService planLimiteService;

    public IndexController(PlanLimiteService planLimiteService) {
        this.planLimiteService = planLimiteService;
    }

    @GetMapping("/")
    public String index(HttpServletRequest req, Model model) {
        if (RolUtil.esSuperAdmin(req)) {
            return "redirect:/plataforma";
        }
        if (RolUtil.esAdmin(req)) {
            String tenant = TenantUtil.getTenantOwner(req);
            var plan = planLimiteService.planActivo(tenant);
            model.addAttribute("planNombre", plan.getNombre());
            model.addAttribute("vendedoresUsados", planLimiteService.contarVendedores(tenant));
            model.addAttribute("vendedoresMax", plan.getMaxVendedores());
            model.addAttribute("productosUsados", planLimiteService.contarProductos(tenant));
            model.addAttribute("productosMax", plan.getMaxProductos());
        }
        return "index";
    }

    @GetMapping("/index.jsp")
    public String indexLegacy() {
        return "redirect:/";
    }
}
