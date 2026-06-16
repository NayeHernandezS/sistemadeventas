package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class UsuarioController {

    private final UsuarioService service;
    private final LoginService auth;

    public UsuarioController(UsuarioService service, LoginService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping({"/usuarios", "/usuarios.html"})
    public String listar(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador de la cuenta puede gestionar usuarios.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        model.addAttribute("usuarios", service.listarVendedoresDelTenant(tenant));
        model.addAttribute("formatoAcceso", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        model.addAttribute("username", auth.getUsername(req));
        Object title = req.getAttribute("title");
        model.addAttribute("title", (title != null ? title : "") + ": Listado de usuarios");
        return "users";
    }

    @GetMapping("/usuarios/form")
    public String formularioGet(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador de la cuenta puede gestionar usuarios.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long id = parseLong(req.getParameter("id"), 0L);
        Usuario usuario = new Usuario();
        usuario.setRol(RolUtil.ROL_VENDEDOR);
        if (id > 0) {
            service.porIdDeTenant(id, tenant).ifPresent(u -> model.addAttribute("usuario", u));
        }
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", usuario);
        }
        return "formUs";
    }

    @PostMapping("/usuarios/form")
    public String formularioPost(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador de la cuenta puede gestionar usuarios.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long id = parseLong(req.getParameter("id"), 0L);
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");

        Map<String, String> errores = new HashMap<>();
        if (username == null || username.isBlank()) {
            errores.put("username", "el username es requerido!");
        }
        if (id == 0 && (password == null || password.isBlank())) {
            errores.put("password", "el password es requerido!");
        }
        if (email == null || email.isBlank()) {
            errores.put("email", "el email es requerido!");
        }

        Usuario usuario = new Usuario();
        if (id > 0) {
            Optional<Usuario> o = service.porIdDeTenant(id, tenant);
            if (o.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Usuario no pertenece a tu cuenta.");
                return null;
            }
            usuario = o.get();
        }

        usuario.setEmail(email);
        usuario.setUsername(username.trim());
        if (password != null && !password.isBlank()) {
            usuario.setPassword(password);
        }

        if (errores.isEmpty()) {
            try {
                service.guardarVendedor(usuario, tenant);
                return "redirect:/usuarios";
            } catch (ServiceJdbcException e) {
                errores.put("username", e.getMessage());
            }
        }
        model.addAttribute("errores", errores);
        model.addAttribute("usuario", usuario);
        return "formUs";
    }

    @GetMapping("/usuarios/eliminar")
    public void eliminar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador de la cuenta puede eliminar usuarios.");
            return;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long id = parseLong(req.getParameter("id"), 0L);
        if (id > 0) {
            try {
                service.eliminarDeTenant(id, tenant);
                resp.sendRedirect(req.getContextPath() + "/usuarios");
            } catch (ServiceJdbcException e) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
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
