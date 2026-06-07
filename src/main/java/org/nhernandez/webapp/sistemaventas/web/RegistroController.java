package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.config.JdbcConnectionHolder;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.services.RegistroLegalService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
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
    private final RegistroLegalService registroLegalService;

    public RegistroController(UsuarioService usuarioService,
                              RegistroLegalService registroLegalService) {
        this.usuarioService = usuarioService;
        this.registroLegalService = registroLegalService;
    }

    @GetMapping
    public String mostrarFormulario(Model model) {
        cargarCatalogos(model, null);
        model.addAttribute("versionLegal", registroLegalService.versionVigente());
        return "registro";
    }

    @PostMapping
    public String registrar(HttpServletRequest req, Model model) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirmar = req.getParameter("confirmarPassword");
        String email = req.getParameter("email");
        String tipoNegocio = req.getParameter("tipoNegocio");
        String aceptaTerminos = req.getParameter("aceptaTerminos");
        String aceptaPrivacidad = req.getParameter("aceptaPrivacidad");

        Map<String, String> errores = new HashMap<>();
        errores.putAll(registroLegalService.validarAceptacion(aceptaTerminos, aceptaPrivacidad));

        if (!TipoNegocioUtil.esValido(tipoNegocio)) {
            errores.put("tipoNegocio", "Selecciona el tipo de negocio");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username != null ? username.trim() : null);
        usuario.setPassword(password);
        usuario.setEmail(email != null ? email.trim() : null);
        usuario.setTipoNegocio(tipoNegocio != null ? tipoNegocio.trim().toLowerCase() : null);

        errores.putAll(usuarioService.validarRegistroCuentaAdmin(usuario, confirmar));

        if (!errores.isEmpty()) {
            return mostrarRegistroConErrores(model, errores, username, email, tipoNegocio,
                    aceptaTerminos, aceptaPrivacidad);
        }

        try {
            usuarioService.registrarCuentaAdmin(
                    usuario,
                    registroLegalService.momentoAceptacion(),
                    registroLegalService.versionVigente());
            model.addAttribute("mensaje",
                    "Cuenta creada. Inicia sesion y elige tu plan (1 mes gratis) en el panel de administrador.");
            return "login";
        } catch (ServiceJdbcException e) {
            JdbcConnectionHolder.rollbackSilencioso();
            asignarErrorServicio(errores, e);
            return mostrarRegistroConErrores(model, errores, username, email, tipoNegocio,
                    aceptaTerminos, aceptaPrivacidad);
        }
    }

    private void asignarErrorServicio(Map<String, String> errores, ServiceJdbcException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "No se pudo crear la cuenta";
        String msgLower = msg.toLowerCase();
        if (msgLower.contains("usuario") || msgLower.contains("username")) {
            errores.put("username", msg);
        } else if (msgLower.contains("email")) {
            errores.put("email", msg);
        } else if (msgLower.contains("contrase") || msgLower.contains("password")) {
            errores.put("password", msg);
        } else {
            errores.put("general", msg);
        }
    }

    private String mostrarRegistroConErrores(Model model, Map<String, String> errores,
                                             String username, String email, String tipoNegocio,
                                             String aceptaTerminos, String aceptaPrivacidad) {
        model.addAttribute("errores", errores);
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("aceptaTerminos", aceptaTerminos);
        model.addAttribute("aceptaPrivacidad", aceptaPrivacidad);
        cargarCatalogos(model, tipoNegocio);
        model.addAttribute("versionLegal", registroLegalService.versionVigente());
        return "registro";
    }

    private void cargarCatalogos(Model model, String tipoNegocio) {
        model.addAttribute("tiposNegocio", TipoNegocioUtil.opciones());
        model.addAttribute("tipoNegocio", tipoNegocio);
        if (!model.containsAttribute("versionLegal")) {
            model.addAttribute("versionLegal", registroLegalService.versionVigente());
        }
    }
}
