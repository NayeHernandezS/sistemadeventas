package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.services.InventarioAlertaService;
import org.nhernandez.webapp.sistemaventas.services.PlanLimiteService;
import org.nhernandez.webapp.sistemaventas.services.ProductoService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final PlanLimiteService planLimiteService;
    private final ProductoService productoService;
    private final InventarioAlertaService inventarioAlertaService;

    public IndexController(PlanLimiteService planLimiteService,
                           @ProductoServicePrincipal ProductoService productoService,
                           InventarioAlertaService inventarioAlertaService) {
        this.planLimiteService = planLimiteService;
        this.productoService = productoService;
        this.inventarioAlertaService = inventarioAlertaService;
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

            var productos = productoService.listarPorOwner(tenant);
            int conAlerta = inventarioAlertaService.contarConAlerta(productos);
            if (conAlerta > 0) {
                model.addAttribute("stockMinimo", inventarioAlertaService.getStockMinimo());
                model.addAttribute("cantidadConAlerta", conAlerta);
                model.addAttribute("cantidadAgotados", inventarioAlertaService.contarAgotados(productos));
                model.addAttribute("cantidadStockBajo", inventarioAlertaService.contarStockBajo(productos));
            }
        }
        return "index";
    }

    @GetMapping("/index.jsp")
    public String indexLegacy() {
        return "redirect:/";
    }
}
