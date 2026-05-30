package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.services.RecuperacionPasswordService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/recuperar")
public class RecuperacionController {

    private final RecuperacionPasswordService recuperacionPasswordService;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    public RecuperacionController(RecuperacionPasswordService recuperacionPasswordService) {
        this.recuperacionPasswordService = recuperacionPasswordService;
    }

    @GetMapping
    public String formularioSolicitud() {
        return "recuperar";
    }

    @PostMapping
    public String solicitar(@RequestParam("email") String email, HttpServletRequest req, Model model) {
        if (email == null || email.isBlank()) {
            model.addAttribute("error", "Indica el correo de tu cuenta.");
            model.addAttribute("email", email);
            return "recuperar";
        }
        Optional<String> enlaceDemo = recuperacionPasswordService.solicitarPorEmail(email, baseUrl(req));
        model.addAttribute("mensajeExito",
                "Si el correo esta registrado, recibiras un enlace para restablecer tu contraseña.");
        enlaceDemo.ifPresent(link -> model.addAttribute("enlaceDemo", link));
        return "recuperar";
    }

    @GetMapping("/restablecer")
    public String formularioRestablecer(@RequestParam(value = "token", required = false) String token, Model model) {
        if (token == null || token.isBlank() || !recuperacionPasswordService.tokenValido(token)) {
            model.addAttribute("error", "El enlace no es valido o ha expirado. Solicita uno nuevo.");
            return "recuperar";
        }
        model.addAttribute("token", token.trim());
        return "restablecer";
    }

    @PostMapping("/restablecer")
    public String restablecer(@RequestParam("token") String token,
                              @RequestParam("passwordNueva") String passwordNueva,
                              @RequestParam("passwordConfirmacion") String passwordConfirmacion,
                              Model model,
                              RedirectAttributes redirect) {
        Map<String, String> errores = new HashMap<>();
        if (passwordNueva == null || passwordNueva.isBlank()) {
            errores.put("passwordNueva", "Indica la nueva contraseña");
        }
        if (passwordConfirmacion == null || !passwordConfirmacion.equals(passwordNueva)) {
            errores.put("passwordConfirmacion", "Las contraseñas no coinciden");
        }
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("token", token);
            return "restablecer";
        }
        try {
            recuperacionPasswordService.restablecerConToken(token, passwordNueva);
            redirect.addFlashAttribute("mensaje", "Contraseña actualizada. Ya puedes iniciar sesion.");
            return "redirect:/login";
        } catch (ServiceJdbcException e) {
            model.addAttribute("error", e.getMessage());
            if (recuperacionPasswordService.tokenValido(token)) {
                model.addAttribute("token", token);
                return "restablecer";
            }
            return "recuperar";
        }
    }

    private String baseUrl(HttpServletRequest req) {
        if (appBaseUrl != null && !appBaseUrl.isBlank()) {
            return appBaseUrl.replaceAll("/+$", "");
        }
        String scheme = req.getScheme();
        String server = req.getServerName();
        int port = req.getServerPort();
        String ctx = req.getContextPath() != null ? req.getContextPath() : "";
        StringBuilder url = new StringBuilder(scheme).append("://").append(server);
        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
            url.append(':').append(port);
        }
        url.append(ctx);
        return url.toString();
    }
}
