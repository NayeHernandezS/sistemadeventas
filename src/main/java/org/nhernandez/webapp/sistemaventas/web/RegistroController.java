package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
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
    private final SuscripcionService suscripcionService;

    public RegistroController(UsuarioService usuarioService, SuscripcionService suscripcionService) {
        this.usuarioService = usuarioService;
        this.suscripcionService = suscripcionService;
    }

    @GetMapping
    public String mostrarFormulario(Model model) {
        cargarCatalogos(model, null, null);
        return "registro";
    }

    @PostMapping
    public String registrar(HttpServletRequest req, Model model) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirmar = req.getParameter("confirmarPassword");
        String email = req.getParameter("email");
        String tipoNegocio = req.getParameter("tipoNegocio");
        String planCodigo = req.getParameter("planCodigo");

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
        if (!TipoNegocioUtil.esValido(tipoNegocio)) {
            errores.put("tipoNegocio", "Selecciona el tipo de negocio");
        }
        if (PlanSuscripcion.porCodigo(planCodigo).isEmpty()) {
            errores.put("planCodigo", "Selecciona un plan");
        }

        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            cargarCatalogos(model, tipoNegocio, planCodigo);
            return "registro";
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username.trim());
        usuario.setPassword(password);
        usuario.setEmail(email.trim());
        usuario.setTipoNegocio(tipoNegocio.trim().toLowerCase());

        try {
            usuarioService.registrarCuentaAdmin(usuario, planCodigo.trim().toUpperCase());
            model.addAttribute("mensaje", "Cuenta creada con 1 mes gratis. Inicia sesion para usar tu plan.");
            return "login";
        } catch (ServiceJdbcException e) {
            errores.put("username", e.getMessage());
            model.addAttribute("errores", errores);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            cargarCatalogos(model, tipoNegocio, planCodigo);
            return "registro";
        }
    }

    private void cargarCatalogos(Model model, String tipoNegocio, String planCodigo) {
        model.addAttribute("tiposNegocio", TipoNegocioUtil.opciones());
        model.addAttribute("planes", suscripcionService.planesDisponibles());
        model.addAttribute("tipoNegocio", tipoNegocio);
        model.addAttribute("planCodigo", planCodigo != null ? planCodigo : "EMPRENDEDOR");
    }
}
