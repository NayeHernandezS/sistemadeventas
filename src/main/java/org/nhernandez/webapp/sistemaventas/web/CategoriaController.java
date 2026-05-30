package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.services.CategoriaService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class CategoriaController {

    private final CategoriaService service;
    private final LoginService auth;

    public CategoriaController(CategoriaService service, LoginService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping("/categorias")
    public String listar(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede gestionar categorias.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        model.addAttribute("categorias", service.listarPorOwner(tenant));
        model.addAttribute("username", auth.getUsername(req));
        return "categorias";
    }

    @GetMapping("/categorias/form")
    public String formularioGet(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede gestionar categorias.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long id = parseLong(req.getParameter("id"), 0L);
        if (id > 0) {
            service.porIdPorOwner(id, tenant).ifPresent(c -> model.addAttribute("categoria", c));
        }
        if (!model.containsAttribute("categoria")) {
            model.addAttribute("categoria", new Categoria());
        }
        return "formCategoria";
    }

    @PostMapping("/categorias/form")
    public String formularioPost(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede gestionar categorias.");
            return null;
        }

        String tenant = TenantUtil.getTenantOwner(req);
        long id = parseLong(req.getParameter("id"), 0L);
        String nombre = req.getParameter("nombre");

        Map<String, String> errores = new HashMap<>();
        if (nombre == null || nombre.isBlank()) {
            errores.put("nombre", "El nombre es requerido");
        } else if (nombre.trim().length() > 100) {
            errores.put("nombre", "El nombre no puede superar 100 caracteres");
        }

        Categoria categoria = new Categoria();
        categoria.setOwnerUsername(tenant);
        if (id > 0) {
            Optional<Categoria> existente = service.porIdPorOwner(id, tenant);
            if (existente.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Categoria no pertenece a tu cuenta.");
                return null;
            }
            categoria.setId(id);
        }
        categoria.setNombre(nombre != null ? nombre.trim() : "");

        if (errores.isEmpty()) {
            try {
                service.guardar(categoria);
                return "redirect:/categorias";
            } catch (ServiceJdbcException e) {
                errores.put("nombre", e.getMessage());
            }
        }
        model.addAttribute("errores", errores);
        model.addAttribute("categoria", categoria);
        return "formCategoria";
    }

    @GetMapping("/categorias/eliminar")
    public void eliminar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede eliminar categorias.");
            return;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long id = parseLong(req.getParameter("id"), 0L);
        if (id > 0) {
            try {
                service.eliminarPorOwner(id, tenant);
                resp.sendRedirect(req.getContextPath() + "/categorias");
            } catch (ServiceJdbcException e) {
                req.getSession().setAttribute("mensajeError", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/categorias");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Error el id es null, se debe enviar como parametro en la url!");
        }
    }

    private long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
