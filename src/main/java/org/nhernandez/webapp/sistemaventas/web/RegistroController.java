package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    private final UsuarioService usuarioService;

    public RegistroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String mostrarFormulario() {
        return "registro";
    }

    @PostMapping
    public String registrar(HttpServletRequest req, Model model) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirmar = req.getParameter("confirmarPassword");
        String email = req.getParameter("email");

        Map<String, String> errores = new HashMap<>();

        if (username == null || username.isBlank()) {
            errores.put("username", "El usuario es requerido");
        } else if (username.length() < 3) {
            errores.put("username", "Minimo 3 caracteres");
        }

        if (password == null || password.isBlank()) {
            errores.put("password", "La contraseña es requerida");
        } else if (password.length() < 4) {
            errores.put("password", "Minimo 4 caracteres");
        }

        if (confirmar == null || !confirmar.equals(password)) {
            errores.put("confirmarPassword", "Las contraseñas no coinciden");
        }

        if (email == null || email.isBlank()) {
            errores.put("email", "El email es requerido");
        }

        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "registro";
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username.trim());
        usuario.setPassword(password);
        usuario.setEmail(email.trim());

        try {
            usuarioService.registrarCuentaAdmin(usuario);
            model.addAttribute("mensaje", "Cuenta de administrador creada. Ya puedes iniciar sesion y agregar vendedores.");
            return "login";
        } catch (ServiceJdbcException e) {
            errores.put("username", e.getMessage());
            model.addAttribute("errores", errores);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "registro";
        }
    }
}
