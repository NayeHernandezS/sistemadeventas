package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;
    private final LoginService loginService;

    public PerfilController(UsuarioService usuarioService, LoginService loginService) {
        this.usuarioService = usuarioService;
        this.loginService = loginService;
    }

    @GetMapping
    public String perfil(HttpServletRequest req, Model model) {
        loginService.getUsername(req).ifPresent(u -> model.addAttribute("username", u));
        return "perfil";
    }

    @PostMapping("/password")
    public String cambiarPassword(HttpServletRequest req, Model model) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        String passwordActual = req.getParameter("passwordActual");
        String passwordNueva = req.getParameter("passwordNueva");
        String passwordConfirmacion = req.getParameter("passwordConfirmacion");

        Map<String, String> errores = new HashMap<>();
        if (passwordActual == null || passwordActual.isBlank()) {
            errores.put("passwordActual", "Indica tu contraseña actual");
        }
        if (passwordNueva == null || passwordNueva.isBlank()) {
            errores.put("passwordNueva", "Indica la nueva contraseña");
        }
        if (passwordConfirmacion == null || !passwordConfirmacion.equals(passwordNueva)) {
            errores.put("passwordConfirmacion", "Las contraseñas nuevas no coinciden");
        }
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("username", usernameOpt.get());
            return "perfil";
        }

        try {
            usuarioService.cambiarPassword(usernameOpt.get(), passwordActual, passwordNueva);
            model.addAttribute("mensajeExito", "Contraseña actualizada correctamente.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        model.addAttribute("username", usernameOpt.get());
        return "perfil";
    }
}
