package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class SuscripcionController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LoginService loginService;
    private final SuscripcionService suscripcionService;

    public SuscripcionController(LoginService loginService, SuscripcionService suscripcionService) {
        this.loginService = loginService;
        this.suscripcionService = suscripcionService;
    }

    @GetMapping("/suscripcion")
    public String suscripcionGet(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            return "redirect:/";
        }
        cargarDatosSuscripcion(req, model);
        return "suscripcion";
    }

    @PostMapping("/suscripcion")
    public String suscripcionPost(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador de la cuenta gestiona la suscripcion.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        int meses;
        try {
            meses = Integer.parseInt(req.getParameter("meses"));
        } catch (NumberFormatException e) {
            meses = 0;
        }
        Map<String, String> errores = new HashMap<>();
        if (meses < 1 || meses > 24) {
            errores.put("meses", "Elige entre 1 y 24 meses");
        }
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            cargarDatosSuscripcion(req, model);
            return "suscripcion";
        }
        try {
            suscripcionService.solicitarPago(tenant, meses);
            model.addAttribute("mensajeExito",
                    "Solicitud registrada por " + meses + " mes(es). Confirma el pago en tu panel.");
        } catch (ServiceJdbcException e) {
            errores.put("general", e.getMessage());
            model.addAttribute("errores", errores);
        }
        cargarDatosSuscripcion(req, model);
        return "suscripcion";
    }

    @GetMapping("/admin/pagos")
    public String pagosGet(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo administrador de la cuenta");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        model.addAttribute("pagosPendientes", suscripcionService.pagosPendientesDelTenant(tenant));
        return "pagosAdmin";
    }

    @PostMapping("/admin/pagos")
    public String pagosPost(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo administrador de la cuenta");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long pagoId;
        try {
            pagoId = Long.parseLong(req.getParameter("pagoId"));
        } catch (NumberFormatException e) {
            pagoId = 0;
        }
        if (pagoId > 0) {
            suscripcionService.confirmarPago(pagoId, tenant);
            model.addAttribute("mensajeExito", "Pago confirmado. Suscripcion extendida.");
        }
        model.addAttribute("pagosPendientes", suscripcionService.pagosPendientesDelTenant(tenant));
        return "pagosAdmin";
    }

    private void cargarDatosSuscripcion(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        suscripcionService.consultar(tenant).ifPresent(s -> {
            model.addAttribute("suscripcion", s);
            model.addAttribute("fechaFinTexto", s.getFechaFin().format(FORMATO));
        });
        model.addAttribute("pagos", suscripcionService.pagosDelUsuario(tenant));
        model.addAttribute("precioMes", suscripcionService.precioPorMes());
        model.addAttribute("requierePago", "1".equals(req.getParameter("requierePago")));
    }
}
