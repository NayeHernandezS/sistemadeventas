package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.services.OnboardingService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping
    public String inicio(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        cargarContexto(tenant, model);
        model.addAttribute("paso", 1);
        return "onboarding";
    }

    @GetMapping("/producto")
    public String producto(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        cargarContexto(tenant, model);
        model.addAttribute("paso", 2);
        model.addAttribute("fechaHoy", LocalDate.now());
        return "onboarding";
    }

    @PostMapping("/producto")
    public String guardarProducto(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        String nombre = req.getParameter("nombre");
        String sku = req.getParameter("sku");
        String precio = req.getParameter("precio");
        String existencias = req.getParameter("existencias");
        long categoriaId;
        try {
            categoriaId = Long.parseLong(req.getParameter("categoria"));
        } catch (NumberFormatException e) {
            categoriaId = 0L;
        }

        Map<String, String> errores = onboardingService.validarPrimerProducto(
                nombre, sku, precio, existencias, categoriaId);
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("nombre", nombre);
            model.addAttribute("sku", sku);
            model.addAttribute("precio", precio);
            model.addAttribute("existencias", existencias);
            model.addAttribute("categoriaId", categoriaId);
            cargarContexto(tenant, model);
            model.addAttribute("paso", 2);
            model.addAttribute("fechaHoy", LocalDate.now());
            return "onboarding";
        }

        try {
            int precioInt = Integer.parseInt(precio.trim());
            int existenciasInt = Integer.parseInt(existencias.trim());
            onboardingService.guardarPrimerProducto(tenant, nombre, sku, precioInt, existenciasInt, categoriaId);
            return "redirect:/onboarding/listo";
        } catch (ServiceJdbcException e) {
            model.addAttribute("errores", Map.of("general", e.getMessage()));
            cargarContexto(tenant, model);
            model.addAttribute("paso", 2);
            model.addAttribute("fechaHoy", LocalDate.now());
            return "onboarding";
        }
    }

    @GetMapping("/listo")
    public String listo(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        cargarContexto(tenant, model);
        model.addAttribute("paso", 3);
        return "onboarding";
    }

    @PostMapping("/completar")
    public String completar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        onboardingService.completar(tenant);
        return "redirect:/?onboarding=ok";
    }

    @PostMapping("/omitir")
    public String omitir(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        onboardingService.completar(tenant);
        return "redirect:/";
    }

    private String requiereAdminConOnboarding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        if (!onboardingService.requiereOnboarding(tenant)) {
            resp.sendRedirect(req.getContextPath() + "/");
            return null;
        }
        return tenant;
    }

    private void cargarContexto(String tenant, Model model) {
        Usuario admin = onboardingService.datosAdmin(tenant).orElse(null);
        if (admin != null) {
            model.addAttribute("tipoNegocioEtiqueta",
                    onboardingService.etiquetaTipoNegocio(admin.getTipoNegocio()));
            onboardingService.asegurarCategoriasPlantilla(tenant, admin.getTipoNegocio());
        }
        model.addAttribute("categorias", onboardingService.categorias(tenant));
        model.addAttribute("tenant", tenant);
    }
}
