package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;
import java.util.Optional;

@Controller
public class LoginController {

    private final LoginService auth;
    private final UsuarioService usuarioService;

    public LoginController(LoginService auth, UsuarioService usuarioService) {
        this.auth = auth;
        this.usuarioService = usuarioService;
    }

    @GetMapping({"/login", "/login.html"})
    public String mostrarLogin(HttpServletRequest req) {
        if (auth.getUsername(req).isPresent()) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String iniciarSesion(HttpServletRequest req, Model model) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            Optional<Usuario> usuarioOptional = usuarioService.login(username, password);
            if (usuarioOptional.isPresent()) {
                Usuario usuario = usuarioOptional.get();
                String tenant = TenantUtil.resolverTenant(usuario);
                if (tenant == null || tenant.isBlank()) {
                    model.addAttribute("error",
                            "Tu cuenta no tiene un administrador asignado. Contacta al dueño del negocio.");
                    return "login";
                }
                TenantUtil.inicializarSesion(req.getSession(), usuario);
                return "redirect:/";
            }
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
            return "login";
        } catch (ServiceJdbcException e) {
            model.addAttribute("error",
                    "Error al consultar la base de datos: " + mensajeClaro(e));
            return "login";
        } catch (IllegalStateException e) {
            model.addAttribute("error",
                    "Error de conexion interna. Reinicia la aplicacion e intenta de nuevo.");
            return "login";
        }
    }

    private String mensajeClaro(ServiceJdbcException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "revisa MySQL y el archivo .env";
        if (msg.contains("admin_owner")) {
            return "Falta ejecutar migracion_tenant.sql en la base java_curso.";
        }
        if (msg.contains("Access denied") || msg.contains("Communications link")) {
            return "No se pudo conectar a MySQL. Revisa .env (DB_USER, DB_PASSWORD) y que el servicio este activo.";
        }
        return msg;
    }
}
