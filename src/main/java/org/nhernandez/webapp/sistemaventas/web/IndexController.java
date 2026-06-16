package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.services.InventarioAlertaService;
import org.nhernandez.webapp.sistemaventas.services.OnboardingService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.PanelNegocioService;
import org.nhernandez.webapp.sistemaventas.services.PlanLimiteService;
import org.nhernandez.webapp.sistemaventas.services.ProductoService;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionAvisoService;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final PlanLimiteService planLimiteService;
    private final ProductoService productoService;
    private final InventarioAlertaService inventarioAlertaService;
    private final SuscripcionAvisoService suscripcionAvisoService;
    private final PanelNegocioService panelNegocioService;
    private final LoginService loginService;
    private final OnboardingService onboardingService;
    private final UsuarioService usuarioService;

    public IndexController(PlanLimiteService planLimiteService,
                           @ProductoServicePrincipal ProductoService productoService,
                           InventarioAlertaService inventarioAlertaService,
                           SuscripcionAvisoService suscripcionAvisoService,
                           PanelNegocioService panelNegocioService,
                           LoginService loginService,
                           OnboardingService onboardingService,
                           UsuarioService usuarioService) {
        this.planLimiteService = planLimiteService;
        this.productoService = productoService;
        this.inventarioAlertaService = inventarioAlertaService;
        this.suscripcionAvisoService = suscripcionAvisoService;
        this.panelNegocioService = panelNegocioService;
        this.loginService = loginService;
        this.onboardingService = onboardingService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/inicio")
    public String index(HttpServletRequest req, Model model) {
        if (RolUtil.esSuperAdmin(req)) {
            return "redirect:/plataforma";
        }
        agregarVisibilidadAgenda(req, model);
        agregarVisibilidadRecetas(req, model);
        if (RolUtil.esAdmin(req)) {
            String tenant = TenantUtil.getTenantOwner(req);
            if (onboardingService.requiereOnboarding(tenant)) {
                return "redirect:/onboarding";
            }
            if ("ok".equals(req.getParameter("onboarding"))) {
                model.addAttribute("mensajeExito",
                        "Configuracion inicial completada. Cuando quieras facturar, completa los datos fiscales en Mi perfil.");
            }
            var plan = planLimiteService.planActivo(tenant);
            model.addAttribute("planNombre", plan.getNombre());
            model.addAttribute("vendedoresUsados", planLimiteService.contarVendedores(tenant));
            model.addAttribute("vendedoresMax", plan.getMaxVendedores());
            model.addAttribute("productosUsados", planLimiteService.contarProductos(tenant));
            model.addAttribute("productosMax", plan.getMaxProductos());
            suscripcionAvisoService.evaluar(tenant).ifPresent(a -> model.addAttribute("avisoSuscripcion", a));

            var productos = productoService.listarPorOwner(tenant);
            int conAlerta = inventarioAlertaService.contarConAlerta(productos, tenant);
            if (conAlerta > 0) {
                model.addAttribute("stockMinimo", inventarioAlertaService.getStockMinimo(tenant));
                model.addAttribute("cantidadConAlerta", conAlerta);
                model.addAttribute("cantidadAgotados", inventarioAlertaService.contarAgotados(productos));
                model.addAttribute("cantidadStockBajo", inventarioAlertaService.contarStockBajo(productos, tenant));
            }

            loginService.getUsername(req).ifPresent(user ->
                    model.addAttribute("panelNegocio", panelNegocioService.resumenParaAdmin(tenant, user)));
        }
        return "index";
    }

    private void agregarVisibilidadAgenda(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        if (tenant == null || tenant.isBlank()) {
            model.addAttribute("mostrarAgendaServicios", false);
            return;
        }
        boolean mostrar = usuarioService.porUsername(tenant)
                .map(u -> TipoNegocioUtil.tieneOpcionServicios(u.getTipoNegocio()))
                .orElse(false);
        model.addAttribute("mostrarAgendaServicios", mostrar);
    }

    private void agregarVisibilidadRecetas(HttpServletRequest req, Model model) {
        boolean mostrar = false;
        if (RolUtil.esAdmin(req)) {
            String tenant = TenantUtil.getTenantOwner(req);
            if (tenant != null && !tenant.isBlank()) {
                mostrar = usuarioService.porUsername(tenant)
                        .map(u -> TipoNegocioUtil.esRestaurante(u.getTipoNegocio()))
                        .orElse(false);
            }
        }
        model.addAttribute("mostrarRecetasRestaurante", mostrar);
    }

    @GetMapping("/index.jsp")
    public String indexLegacy() {
        return "redirect:/inicio";
    }
}
