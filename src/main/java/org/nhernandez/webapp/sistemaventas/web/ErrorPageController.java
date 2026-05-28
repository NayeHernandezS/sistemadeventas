package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/acceso-denegado")
    public String accesoDenegado(HttpServletRequest request) {
        request.setAttribute("status", 403);
        request.setAttribute("mensaje", "No tienes permisos para realizar esta accion.");
        return "error";
    }
}
