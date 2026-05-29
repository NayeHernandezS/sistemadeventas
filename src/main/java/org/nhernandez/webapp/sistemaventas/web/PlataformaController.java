package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.services.PlataformaService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.SoporteService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/plataforma")
public class PlataformaController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PlataformaService plataformaService;
    private final SoporteService soporteService;

    public PlataformaController(PlataformaService plataformaService, SoporteService soporteService) {
        this.plataformaService = plataformaService;
        this.soporteService = soporteService;
    }

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("totalClientes", plataformaService.listarClientes().size());
        model.addAttribute("pagosPendientes", plataformaService.pagosPendientesGlobales().size());
        model.addAttribute("soporteAbiertas", soporteService.listarAbiertasPlataforma().size());
        return "plataforma/inicio";
    }

    @GetMapping("/soporte")
    public String soportePlataforma(Model model) {
        model.addAttribute("solicitudes", soporteService.listarAbiertasPlataforma());
        return "plataforma/soporte";
    }

    @PostMapping("/soporte/atender")
    public String atenderSoporte(@RequestParam("id") long id, Model model) {
        soporteService.marcarAtendida(id);
        model.addAttribute("mensajeExito", "Solicitud marcada como atendida.");
        model.addAttribute("solicitudes", soporteService.listarAbiertasPlataforma());
        return "plataforma/soporte";
    }

    @GetMapping("/clientes")
    public String clientes(Model model) {
        model.addAttribute("clientes", plataformaService.listarClientes());
        model.addAttribute("formatoFecha", FORMATO);
        return "plataforma/clientes";
    }

    @GetMapping("/pagos")
    public String pagos(Model model) {
        model.addAttribute("pagosPendientes", plataformaService.pagosPendientesGlobales());
        return "plataforma/pagos";
    }

    @PostMapping("/pagos/confirmar")
    public String confirmarPago(@RequestParam("pagoId") long pagoId, Model model) {
        try {
            plataformaService.confirmarPago(pagoId);
            model.addAttribute("mensajeExito", "Pago confirmado y suscripcion extendida.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        model.addAttribute("pagosPendientes", plataformaService.pagosPendientesGlobales());
        return "plataforma/pagos";
    }

    @PostMapping("/clientes/extender")
    public String extenderSuscripcion(HttpServletRequest req,
                                      HttpServletResponse resp,
                                      Model model) throws IOException {
        if (!RolUtil.esSuperAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        String username = req.getParameter("username");
        int meses;
        try {
            meses = Integer.parseInt(req.getParameter("meses"));
        } catch (NumberFormatException e) {
            meses = 0;
        }
        Map<String, String> errores = new HashMap<>();
        if (username == null || username.isBlank()) {
            errores.put("username", "Cuenta requerida");
        }
        if (meses < 1 || meses > 24) {
            errores.put("meses", "Elige entre 1 y 24 meses");
        }
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("clientes", plataformaService.listarClientes());
            model.addAttribute("formatoFecha", FORMATO);
            return "plataforma/clientes";
        }
        try {
            plataformaService.extenderMeses(username.trim(), meses);
            model.addAttribute("mensajeExito",
                    "Suscripcion de " + username + " extendida " + meses + " mes(es).");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        model.addAttribute("clientes", plataformaService.listarClientes());
        model.addAttribute("formatoFecha", FORMATO);
        return "plataforma/clientes";
    }
}
