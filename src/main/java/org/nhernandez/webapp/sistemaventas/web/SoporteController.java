package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.SoporteService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.SoporteConfigUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@Controller
@RequestMapping("/soporte")
public class SoporteController {

    private final SoporteService soporteService;
    private final LoginService loginService;
    private final UsuarioReposository usuarioRepository;

    public SoporteController(SoporteService soporteService,
                             LoginService loginService,
                             UsuarioReposository usuarioRepository) {
        this.soporteService = soporteService;
        this.loginService = loginService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String soporte(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!requiereAdmin(req, resp)) {
            return null;
        }
        cargarVista(req, model, null);
        return "soporte";
    }

    @PostMapping
    public String enviar(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!requiereAdmin(req, resp)) {
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        String username = loginService.getUsername(req).orElse("");
        String asunto = req.getParameter("asunto");
        String mensaje = req.getParameter("mensaje");
        String email = req.getParameter("email");

        try {
            soporteService.enviarSolicitud(tenant, username, email, asunto, mensaje);
            model.addAttribute("mensajeExito",
                    "Solicitud enviada. Te responderemos pronto por el correo indicado.");
            cargarVista(req, model, email);
            model.addAttribute("historial", soporteService.historialTenant(tenant));
            return "soporte";
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
            cargarVista(req, model, email);
            model.addAttribute("asunto", asunto);
            model.addAttribute("mensaje", mensaje);
            return "soporte";
        }
    }

    private void cargarVista(HttpServletRequest req, Model model, String emailForm) {
        String tenant = TenantUtil.getTenantOwner(req);
        model.addAttribute("soporteEmail", SoporteConfigUtil.email());
        model.addAttribute("soporteWhatsapp", SoporteConfigUtil.whatsapp());
        model.addAttribute("soporteWhatsappUrl", SoporteConfigUtil.whatsappEnlace());
        model.addAttribute("soporteHorario", SoporteConfigUtil.horario());
        model.addAttribute("historial", soporteService.historialTenant(tenant));
        if (emailForm != null) {
            model.addAttribute("email", emailForm);
        } else {
            model.addAttribute("email", emailDelUsuario(req));
        }
    }

    private String emailDelUsuario(HttpServletRequest req) {
        try {
            String user = loginService.getUsername(req).orElse(null);
            if (user == null) {
                return "";
            }
            Usuario u = usuarioRepository.porUsername(user);
            return u != null && u.getEmail() != null ? u.getEmail() : "";
        } catch (SQLException e) {
            return "";
        }
    }

    private boolean requiereAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<String> user = loginService.getUsername(req);
        if (user.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador de la cuenta puede acceder a soporte.");
            return false;
        }
        return true;
    }
}
