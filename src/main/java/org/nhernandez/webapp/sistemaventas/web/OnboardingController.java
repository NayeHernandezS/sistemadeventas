package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.TipoItem;
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

import java.io.IOException;
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
    public String articulo(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        cargarContexto(tenant, model);
        model.addAttribute("paso", 2);
        aplicarValoresFormulario(model, null);
        return "onboarding";
    }

    @PostMapping("/producto")
    public String guardarArticulo(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        String tipoItemStr = req.getParameter("tipo_item");
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

        Map<String, String> errores = onboardingService.validarPrimerItem(
                tipoItemStr, nombre, sku, precio, existencias, categoriaId);
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            aplicarValoresFormulario(model, Map.of(
                    "tipoItem", tipoItemStr,
                    "nombre", nombre,
                    "sku", sku,
                    "precio", precio,
                    "existencias", existencias,
                    "categoriaId", categoriaId
            ));
            cargarContexto(tenant, model);
            model.addAttribute("paso", 2);
            return "onboarding";
        }

        try {
            TipoItem tipoItem = TipoItem.porCodigo(tipoItemStr).orElse(TipoItem.PRODUCTO);
            int precioInt = Integer.parseInt(precio.trim());
            int existenciasInt = tipoItem == TipoItem.SERVICIO
                    ? 0
                    : Integer.parseInt(existencias.trim());
            onboardingService.guardarPrimerItem(
                    tenant, tipoItem, nombre, sku, precioInt, existenciasInt, categoriaId);
            return "redirect:/onboarding/facturacion";
        } catch (ServiceJdbcException e) {
            model.addAttribute("errores", Map.of("general", e.getMessage()));
            aplicarValoresFormulario(model, Map.of(
                    "tipoItem", tipoItemStr,
                    "nombre", nombre,
                    "sku", sku,
                    "precio", precio,
                    "existencias", existencias,
                    "categoriaId", categoriaId
            ));
            cargarContexto(tenant, model);
            model.addAttribute("paso", 2);
            return "onboarding";
        }
    }

    @GetMapping("/facturacion")
    public String facturacion(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        cargarContexto(tenant, model);
        model.addAttribute("paso", 3);
        return "onboarding";
    }

    @GetMapping("/listo")
    public String listo(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        cargarContexto(tenant, model);
        model.addAttribute("paso", 4);
        return "onboarding";
    }

    @PostMapping("/completar")
    public String completar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String tenant = requiereAdminConOnboarding(req, resp);
        if (tenant == null) {
            return null;
        }
        onboardingService.completar(tenant);
        return "redirect:/inicio?onboarding=ok";
    }

    @PostMapping("/omitir")
    public String omitir(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        try {
            onboardingService.completar(tenant);
        } catch (ServiceJdbcException e) {
            req.getSession().setAttribute("mensajeError",
                    "No se pudo completar la configuracion inicial. Intenta de nuevo o agrega un producto.");
            return "redirect:/inicio";
        }
        return "redirect:/inicio";
    }

    private String requiereAdminConOnboarding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/inicio");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        if (!onboardingService.requiereOnboarding(tenant)) {
            resp.sendRedirect(req.getContextPath() + "/inicio");
            return null;
        }
        return tenant;
    }

    private void cargarContexto(String tenant, Model model) {
        Usuario admin = onboardingService.datosAdmin(tenant).orElse(null);
        String tipoNegocio = "otro";
        if (admin != null && admin.getTipoNegocio() != null && !admin.getTipoNegocio().isBlank()) {
            tipoNegocio = admin.getTipoNegocio();
            model.addAttribute("tipoNegocioEtiqueta", onboardingService.etiquetaTipoNegocio(tipoNegocio));
            onboardingService.asegurarCategoriasPlantilla(tenant, tipoNegocio);
        } else {
            model.addAttribute("tipoNegocioEtiqueta", "General");
        }
        model.addAttribute("categorias", onboardingService.categorias(tenant));
        model.addAttribute("tenant", tenant);
    }

    private void aplicarValoresFormulario(Model model, Map<String, Object> valores) {
        String tipoNegocio = (String) model.getAttribute("tipoNegocio");
        String tipoDefecto = tipoNegocio != null
                ? onboardingService.tipoItemPorDefecto(tipoNegocio).name()
                : TipoItem.PRODUCTO.name();
        if (valores == null) {
            model.addAttribute("tipoItem", tipoDefecto);
            model.addAttribute("existencias", 10);
            return;
        }
        model.addAttribute("tipoItem", valores.getOrDefault("tipoItem", tipoDefecto));
        model.addAttribute("nombre", valores.get("nombre"));
        model.addAttribute("sku", valores.get("sku"));
        model.addAttribute("precio", valores.get("precio"));
        model.addAttribute("existencias", valores.getOrDefault("existencias", "10"));
        model.addAttribute("categoriaId", valores.get("categoriaId"));
    }
}
