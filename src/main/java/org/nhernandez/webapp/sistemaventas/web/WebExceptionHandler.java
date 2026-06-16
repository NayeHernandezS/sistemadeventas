package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(ServiceJdbcException.class)
    public Object manejarServiceJdbc(ServiceJdbcException ex, HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String path = req.getRequestURI();
        boolean esApi = (path != null && path.contains("/api/"))
                || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));

        if (esApi) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "mensaje", mensajeSeguro(ex)));
        }

        req.getSession().setAttribute("mensajeError", mensajeSeguro(ex));
        return "redirect:/inicio";
    }

    private static String mensajeSeguro(ServiceJdbcException ex) {
        String msg = ex.getMessage();
        return msg != null && !msg.isBlank() ? msg : "Error al procesar la solicitud.";
    }
}
