package org.nhernandez.webapp.sistemaventas.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping({"/login", "/login.html"})
    public String mostrarLogin(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", mensajeError(error));
        }
        return "login";
    }

    private String mensajeError(String codigo) {
        return switch (codigo) {
            case "tenant" -> "Tu cuenta no tiene un administrador asignado. Contacta al dueño del negocio.";
            case "db" -> "No se pudo conectar a la base de datos. Revisa MySQL y el archivo .env.";
            case "session" -> "La sesion expiro o es invalida. Inicia sesion de nuevo.";
            default -> "Usuario o contraseña incorrectos.";
        };
    }
}
