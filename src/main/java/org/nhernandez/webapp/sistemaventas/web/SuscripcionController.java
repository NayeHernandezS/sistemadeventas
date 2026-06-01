package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoCheckoutService;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.nhernandez.webapp.sistemaventas.services.PlanLimiteService;
import org.nhernandez.webapp.sistemaventas.services.RenovacionAutomaticaService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionAvisoService;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SuscripcionController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SuscripcionService suscripcionService;
    private final PlanLimiteService planLimiteService;
    private final MercadoPagoCheckoutService mercadoPagoCheckoutService;
    private final SuscripcionAvisoService suscripcionAvisoService;
    private final RenovacionAutomaticaService renovacionAutomaticaService;
    private final UsuarioReposository usuarioRepository;

    public SuscripcionController(SuscripcionService suscripcionService,
                                 PlanLimiteService planLimiteService,
                                 MercadoPagoCheckoutService mercadoPagoCheckoutService,
                                 SuscripcionAvisoService suscripcionAvisoService,
                                 RenovacionAutomaticaService renovacionAutomaticaService,
                                 UsuarioReposository usuarioRepository) {
        this.suscripcionService = suscripcionService;
        this.planLimiteService = planLimiteService;
        this.mercadoPagoCheckoutService = mercadoPagoCheckoutService;
        this.suscripcionAvisoService = suscripcionAvisoService;
        this.renovacionAutomaticaService = renovacionAutomaticaService;
        this.usuarioRepository = usuarioRepository;
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
        String planCodigo = req.getParameter("planCodigo");
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
        if (PlanSuscripcion.porCodigo(planCodigo).isEmpty()) {
            errores.put("planCodigo", "Selecciona un plan valido");
        }
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            cargarDatosSuscripcion(req, model);
            return "suscripcion";
        }
        boolean pagoManual = "1".equals(req.getParameter("manual"));
        try {
            if (!pagoManual && mercadoPagoCheckoutService.habilitado()) {
                String checkoutUrl = mercadoPagoCheckoutService.iniciarCheckout(
                        tenant, meses, planCodigo.trim().toUpperCase(), baseUrlPublica(req));
                return "redirect:" + checkoutUrl;
            }
            suscripcionService.solicitarPago(tenant, meses, planCodigo.trim().toUpperCase());
            model.addAttribute("mensajeExito",
                    "Solicitud registrada por " + meses + " mes(es). La plataforma confirmara tu pago pronto.");
        } catch (ServiceJdbcException e) {
            errores.put("general", e.getMessage());
            model.addAttribute("errores", errores);
            String planIntentado = planCodigo.trim().toUpperCase();
            if (planLimiteService.evaluarPlanContratable(tenant, planIntentado).isContratable()) {
                model.addAttribute("planCodigoSeleccion", planIntentado);
            }
        }
        cargarDatosSuscripcion(req, model);
        return "suscripcion";
    }

    @GetMapping("/suscripcion/pago-exitoso")
    public String pagoExitoso(HttpServletRequest req, Model model) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            return "redirect:/";
        }
        String paymentId = req.getParameter("payment_id");
        if (paymentId == null) {
            paymentId = req.getParameter("collection_id");
        }
        String tenant = TenantUtil.getTenantOwner(req);
        if (paymentId != null && mercadoPagoCheckoutService.habilitado()) {
            try {
                mercadoPagoCheckoutService.procesarPagoPorId(Long.parseLong(paymentId.trim()));
                model.addAttribute("mensajeExito",
                        "Pago recibido. Tu suscripcion se activara en unos segundos si aun no aparece como vigente.");
            } catch (NumberFormatException ignored) {
                model.addAttribute("mensajeExito", "Gracias. Estamos confirmando tu pago.");
            }
        } else if (mercadoPagoCheckoutService.habilitado()
                && mercadoPagoCheckoutService.sincronizarPagosPendientesDelTenant(tenant) > 0) {
            model.addAttribute("mensajeExito", "Pago confirmado. Tu plan ya deberia estar activo.");
        } else {
            model.addAttribute("mensajeExito",
                    "Gracias. Si pagaste con Mercado Pago, recarga esta pagina o usa Volver al sitio en MP para activar el plan.");
        }
        cargarDatosSuscripcion(req, model);
        return "suscripcion";
    }

    @GetMapping("/suscripcion/pago-pendiente")
    public String pagoPendiente(HttpServletRequest req, Model model) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            return "redirect:/";
        }
        model.addAttribute("mensajeExito",
                "Tu pago esta pendiente (por ejemplo SPEI u OXXO). Cuando Mercado Pago lo confirme, se activara el plan.");
        cargarDatosSuscripcion(req, model);
        return "suscripcion";
    }

    @GetMapping("/suscripcion/pago-fallido")
    public String pagoFallido(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            return "redirect:/";
        }
        model.addAttribute("errores", Map.of("general",
                "El pago no se completo. Puedes intentar de nuevo o solicitar pago manual."));
        cargarDatosSuscripcion(req, model);
        return "suscripcion";
    }

    @PostMapping("/suscripcion/auto-renovar")
    public String activarAutoRenovacion(HttpServletRequest req, Model model, HttpServletResponse resp)
            throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        String planCodigo = req.getParameter("planCodigo");
        if (PlanSuscripcion.porCodigo(planCodigo).isEmpty()) {
            planCodigo = planLimiteService.planActivo(tenant).getCodigo();
        }
        try {
            String email = emailDelAdmin(tenant);
            String url = renovacionAutomaticaService.iniciarActivacion(
                    tenant, planCodigo.trim().toUpperCase(), baseUrlPublica(req), email);
            return "redirect:" + url;
        } catch (ServiceJdbcException e) {
            model.addAttribute("errores", Map.of("general", e.getMessage()));
            cargarDatosSuscripcion(req, model);
            return "suscripcion";
        }
    }

    @PostMapping("/suscripcion/auto-renovar/cancelar")
    public String cancelarAutoRenovacion(HttpServletRequest req, Model model, HttpServletResponse resp)
            throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        try {
            renovacionAutomaticaService.cancelar(tenant);
            model.addAttribute("mensajeExito", "Renovacion automatica cancelada.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("errores", Map.of("general", e.getMessage()));
        }
        cargarDatosSuscripcion(req, model);
        return "suscripcion";
    }

    @GetMapping("/suscripcion/auto-renovar-exito")
    public String autoRenovarExito(HttpServletRequest req, Model model) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            return "redirect:/";
        }
        String preapprovalId = req.getParameter("preapproval_id");
        if (preapprovalId == null) {
            preapprovalId = req.getParameter("collection_id");
        }
        if (preapprovalId != null && renovacionAutomaticaService.disponible()) {
            renovacionAutomaticaService.procesarPreapprovalPorId(preapprovalId);
        }
        model.addAttribute("mensajeExito",
                "Renovacion automatica configurada. Se cobrara mensualmente el plan seleccionado.");
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
        if (mercadoPagoCheckoutService.habilitado()
                && mercadoPagoCheckoutService.sincronizarPagosPendientesDelTenant(tenant) > 0) {
            model.addAttribute("mensajeExito", "Pago de Mercado Pago confirmado.");
        }
        model.addAttribute("pagosPendientes", suscripcionService.pagosPendientesDelTenant(tenant));
        model.addAttribute("mercadoPagoHabilitado", mercadoPagoCheckoutService.habilitado());
        return "pagosAdmin";
    }

    @PostMapping("/admin/pagos/cancelar")
    public String cancelarPagoPendiente(HttpServletRequest req, Model model, HttpServletResponse resp)
            throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long pagoId;
        try {
            pagoId = Long.parseLong(req.getParameter("pagoId"));
        } catch (NumberFormatException e) {
            model.addAttribute("mensajeError", "Pago no valido");
            model.addAttribute("pagosPendientes", suscripcionService.pagosPendientesDelTenant(tenant));
            model.addAttribute("mercadoPagoHabilitado", mercadoPagoCheckoutService.habilitado());
            return "pagosAdmin";
        }
        try {
            suscripcionService.cancelarPagoPendienteDelTenant(tenant, pagoId);
            model.addAttribute("mensajeExito",
                    "Solicitud cancelada. Ya puedes iniciar un nuevo pago en Suscripcion.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        model.addAttribute("pagosPendientes", suscripcionService.pagosPendientesDelTenant(tenant));
        model.addAttribute("mercadoPagoHabilitado", mercadoPagoCheckoutService.habilitado());
        return "pagosAdmin";
    }

    @PostMapping("/suscripcion/pago/cancelar")
    public String cancelarPagoDesdeSuscripcion(HttpServletRequest req, Model model, HttpServletResponse resp)
            throws IOException {
        if (!RolUtil.esAdmin(req)) {
            return "redirect:/";
        }
        String tenant = TenantUtil.getTenantOwner(req);
        try {
            long pagoId = Long.parseLong(req.getParameter("pagoId"));
            suscripcionService.cancelarPagoPendienteDelTenant(tenant, pagoId);
            model.addAttribute("mensajeExito",
                    "Solicitud cancelada. Ya puedes solicitar un nuevo pago.");
        } catch (NumberFormatException | ServiceJdbcException e) {
            model.addAttribute("errores", Map.of("general",
                    e instanceof ServiceJdbcException s ? s.getMessage() : "Pago no valido"));
        }
        cargarDatosSuscripcion(req, model);
        return "suscripcion";
    }

    private void cargarDatosSuscripcion(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        if (mercadoPagoCheckoutService.habilitado()
                && mercadoPagoCheckoutService.sincronizarPagosPendientesDelTenant(tenant) > 0
                && !model.containsAttribute("mensajeExito")) {
            model.addAttribute("mensajeExito", "Pago de Mercado Pago confirmado. Tu plan ya esta activo.");
        }
        PlanSuscripcion planActivo = planLimiteService.planActivo(tenant);
        suscripcionService.consultar(tenant).ifPresent(s -> {
            model.addAttribute("suscripcion", s);
            model.addAttribute("fechaFinTexto", s.getFechaFin().format(FORMATO));
            model.addAttribute("planActivoNombre", PlanSuscripcion.porCodigoODefault(s.getPlanCodigo()).getNombre());
        });
        model.addAttribute("pagos", suscripcionService.pagosDelUsuario(tenant));
        var pagosPendientes = suscripcionService.pagosPendientesDelTenant(tenant);
        model.addAttribute("pagosPendientes", pagosPendientes);
        if (mercadoPagoCheckoutService.habilitado()) {
            List<PagoSuscripcion> checkoutsAbandonados = pagosPendientes.stream()
                    .filter(mercadoPagoCheckoutService::checkoutSinPagoEnMercadoPago)
                    .toList();
            if (!checkoutsAbandonados.isEmpty()) {
                model.addAttribute("checkoutsAbandonadosMp", checkoutsAbandonados);
            }
        }
        model.addAttribute("planes", suscripcionService.planesDisponibles());
        model.addAttribute("planActivo", planActivo);
        model.addAttribute("vendedoresUsados", planLimiteService.contarVendedores(tenant));
        model.addAttribute("productosUsados", planLimiteService.contarProductos(tenant));
        model.addAttribute("planesContratibilidad", planLimiteService.evaluarPlanesContratables(tenant));
        if (!model.containsAttribute("planCodigoSeleccion")) {
            model.addAttribute("planCodigoSeleccion", planActivo.getCodigo());
        }
        model.addAttribute("requierePago", "1".equals(req.getParameter("requierePago")));
        model.addAttribute("mercadoPagoHabilitado", mercadoPagoCheckoutService.habilitado());
        model.addAttribute("renovacionAutomaticaDisponible", renovacionAutomaticaService.disponible());
        suscripcionAvisoService.evaluar(tenant).ifPresent(a -> model.addAttribute("avisoSuscripcion", a));
    }

    private String emailDelAdmin(String tenant) {
        try {
            Usuario u = usuarioRepository.porUsername(tenant);
            return u != null && u.getEmail() != null ? u.getEmail() : "";
        } catch (SQLException e) {
            return "";
        }
    }

    private static String baseUrlPublica(HttpServletRequest req) {
        StringBuffer url = req.getRequestURL();
        String uri = req.getRequestURI();
        String base = url.substring(0, url.length() - uri.length());
        String ctx = req.getContextPath();
        if (ctx != null && !ctx.isEmpty()) {
            base += ctx;
        }
        return base;
    }
}
