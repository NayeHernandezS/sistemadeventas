package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nhernandez.webapp.sistemaventas.models.RecetaLinea;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.RecetaRestauranteService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RecetaRestauranteController {

    private final RecetaRestauranteService recetaService;
    private final UsuarioService usuarioService;
    private final LoginService loginService;

    public RecetaRestauranteController(RecetaRestauranteService recetaService,
                                       UsuarioService usuarioService,
                                       LoginService loginService) {
        this.recetaService = recetaService;
        this.usuarioService = usuarioService;
        this.loginService = loginService;
    }

    @GetMapping("/recetas")
    public String listar(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!puedeGestionarRecetas(req, resp)) {
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        model.addAttribute("platillos", recetaService.listarResumenPlatillos(tenant));
        loginService.getUsername(req).ifPresent(u -> model.addAttribute("username", u));
        return "recetas";
    }

    @GetMapping("/recetas/form")
    public String formulario(HttpServletRequest req,
                             Model model,
                             HttpServletResponse resp,
                             @RequestParam(value = "productoId", required = false) Long productoId) throws IOException {
        if (!puedeGestionarRecetas(req, resp)) {
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        if (productoId == null || productoId <= 0) {
            model.addAttribute("platillosDisponibles", recetaService.listarPlatillos(tenant));
            loginService.getUsername(req).ifPresent(u -> model.addAttribute("username", u));
            return "recetaElegirPlatillo";
        }
        var platillo = recetaService.platilloPorId(tenant, productoId);
        if (platillo.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Platillo no encontrado");
            return null;
        }
        model.addAttribute("platillo", platillo.get());
        model.addAttribute("lineas", recetaService.lineasConCosto(tenant, productoId));
        model.addAttribute("insumos", recetaService.listarInsumos(tenant));
        loginService.getUsername(req).ifPresent(u -> model.addAttribute("username", u));
        return "recetaForm";
    }

    @PostMapping("/recetas/guardar")
    public String guardar(HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws IOException {
        if (!puedeGestionarRecetas(req, resp)) {
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        Long productoId = parseLong(req.getParameter("productoId"));
        if (productoId == null || productoId <= 0) {
            session.setAttribute("mensajeError", "Selecciona un platillo valido");
            return "redirect:/recetas";
        }
        try {
            recetaService.guardarReceta(tenant, productoId, parseLineas(req));
            session.setAttribute("mensajeExito", "Receta guardada correctamente");
        } catch (IllegalArgumentException e) {
            session.setAttribute("mensajeError", e.getMessage());
            return "redirect:/recetas/form?productoId=" + productoId;
        } catch (ServiceJdbcException e) {
            session.setAttribute("mensajeError", "No se pudo guardar la receta");
            return "redirect:/recetas/form?productoId=" + productoId;
        }
        return "redirect:/recetas";
    }

    @GetMapping("/recetas/eliminar")
    public String eliminar(HttpServletRequest req,
                           HttpServletResponse resp,
                           HttpSession session,
                           @RequestParam("productoId") Long productoId) throws IOException {
        if (!puedeGestionarRecetas(req, resp)) {
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        try {
            recetaService.eliminarReceta(tenant, productoId);
            session.setAttribute("mensajeExito", "Receta eliminada");
        } catch (ServiceJdbcException e) {
            session.setAttribute("mensajeError", "No se pudo eliminar la receta");
        }
        return "redirect:/recetas";
    }

    private boolean puedeGestionarRecetas(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede gestionar recetas.");
            return false;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        boolean aplica = usuarioService.porUsername(tenant)
                .map(u -> recetaService.aplicaParaTenant(u.getTipoNegocio()))
                .orElse(false);
        if (!aplica) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Las recetas solo estan disponibles para negocios tipo restaurante.");
            return false;
        }
        return true;
    }

    private static List<RecetaLinea> parseLineas(HttpServletRequest req) {
        String[] insumoIds = req.getParameterValues("insumoId");
        String[] cantidades = req.getParameterValues("cantidad");
        String[] unidades = req.getParameterValues("unidad");
        if (insumoIds == null || cantidades == null) {
            return List.of();
        }
        List<RecetaLinea> lineas = new ArrayList<>();
        for (int i = 0; i < insumoIds.length; i++) {
            Long insumoId = parseLong(insumoIds[i]);
            if (insumoId == null || insumoId <= 0) {
                continue;
            }
            RecetaLinea linea = new RecetaLinea();
            linea.setInsumoProductoId(insumoId);
            linea.setCantidad(parseCantidad(i < cantidades.length ? cantidades[i] : null));
            linea.setUnidad(i < unidades.length && unidades[i] != null ? unidades[i] : "pza");
            lineas.add(linea);
        }
        return lineas;
    }

    private static Long parseLong(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parseCantidad(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
