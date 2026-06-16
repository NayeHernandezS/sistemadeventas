package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.services.ActividadVendedorService;
import org.nhernandez.webapp.sistemaventas.services.CfdiTimbradoService;
import org.nhernandez.webapp.sistemaventas.services.DatosFiscalesNegocioService;
import org.nhernandez.webapp.sistemaventas.services.InventarioAlertaService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.PlanLimiteService;
import org.nhernandez.webapp.sistemaventas.services.PreferenciasTenantService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.nhernandez.webapp.sistemaventas.services.TenantLogoService;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioService usuarioService;
    private final LoginService loginService;
    private final SuscripcionService suscripcionService;
    private final PlanLimiteService planLimiteService;
    private final DatosFiscalesNegocioService datosFiscalesNegocioService;
    private final ActividadVendedorService actividadVendedorService;
    private final PreferenciasTenantService preferenciasTenantService;
    private final InventarioAlertaService inventarioAlertaService;
    private final TenantLogoService tenantLogoService;
    private final CfdiTimbradoService cfdiTimbradoService;

    public PerfilController(UsuarioService usuarioService,
                            LoginService loginService,
                            SuscripcionService suscripcionService,
                            PlanLimiteService planLimiteService,
                            DatosFiscalesNegocioService datosFiscalesNegocioService,
                            ActividadVendedorService actividadVendedorService,
                            PreferenciasTenantService preferenciasTenantService,
                            InventarioAlertaService inventarioAlertaService,
                            TenantLogoService tenantLogoService,
                            CfdiTimbradoService cfdiTimbradoService) {
        this.usuarioService = usuarioService;
        this.loginService = loginService;
        this.suscripcionService = suscripcionService;
        this.planLimiteService = planLimiteService;
        this.datosFiscalesNegocioService = datosFiscalesNegocioService;
        this.actividadVendedorService = actividadVendedorService;
        this.preferenciasTenantService = preferenciasTenantService;
        this.inventarioAlertaService = inventarioAlertaService;
        this.tenantLogoService = tenantLogoService;
        this.cfdiTimbradoService = cfdiTimbradoService;
    }

    @GetMapping
    public String perfil(HttpServletRequest req, Model model) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        cargarPerfil(req, model, usernameOpt.get());
        return "perfil";
    }

    @PostMapping("/logo")
    public String subirLogo(HttpServletRequest req, Model model,
                              @RequestParam("logo") MultipartFile logo) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        if (!RolUtil.esAdmin(req)) {
            model.addAttribute("mensajeError", "Solo el administrador puede cambiar el logo del negocio.");
            cargarPerfil(req, model, usernameOpt.get());
            return "perfil";
        }
        String tenant = TenantUtil.getTenantOwner(req);
        try {
            tenantLogoService.guardarLogo(tenant, logo);
            model.addAttribute("mensajeExito",
                    "Logo actualizado. Todos los vendedores de tu cuenta veran la misma marca.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        cargarPerfil(req, model, usernameOpt.get());
        return "perfil";
    }

    @PostMapping("/logo/eliminar")
    public String eliminarLogo(HttpServletRequest req, Model model) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        if (!RolUtil.esAdmin(req)) {
            model.addAttribute("mensajeError", "Solo el administrador puede quitar el logo del negocio.");
            cargarPerfil(req, model, usernameOpt.get());
            return "perfil";
        }
        String tenant = TenantUtil.getTenantOwner(req);
        try {
            tenantLogoService.eliminarLogo(tenant);
            model.addAttribute("mensajeExito", "Logo eliminado.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        cargarPerfil(req, model, usernameOpt.get());
        return "perfil";
    }

    @PostMapping("/email")
    public String actualizarEmail(HttpServletRequest req, Model model) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        String email = req.getParameter("email");
        Map<String, String> errores = new HashMap<>();
        if (email == null || email.isBlank()) {
            errores.put("email", "El email es requerido");
        }
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            cargarPerfil(req, model, usernameOpt.get());
            return "perfil";
        }
        try {
            usuarioService.actualizarEmail(usernameOpt.get(), email);
            model.addAttribute("mensajeExito", "Email actualizado correctamente.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        cargarPerfil(req, model, usernameOpt.get());
        return "perfil";
    }

    @PostMapping("/preferencias")
    public String guardarPreferencias(HttpServletRequest req, Model model) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        if (!RolUtil.esAdmin(req)) {
            model.addAttribute("mensajeError", "Solo el administrador puede editar las preferencias del negocio.");
            cargarPerfil(req, model, usernameOpt.get());
            return "perfil";
        }
        String tenant = TenantUtil.getTenantOwner(req);
        Integer stockMinimo = null;
        String param = req.getParameter("stockMinimo");
        if (param != null && !param.isBlank()) {
            try {
                stockMinimo = Integer.parseInt(param.trim());
            } catch (NumberFormatException e) {
                model.addAttribute("errores", Map.of("stockMinimo", "Indica un numero valido"));
                cargarPerfil(req, model, usernameOpt.get());
                return "perfil";
            }
        }
        try {
            preferenciasTenantService.guardarStockMinimo(tenant, stockMinimo);
            model.addAttribute("mensajeExito", "Preferencias del negocio actualizadas.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        cargarPerfil(req, model, usernameOpt.get());
        return "perfil";
    }

    @PostMapping("/tipo-negocio")
    public String actualizarTipoNegocio(HttpServletRequest req, Model model) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        if (!RolUtil.esAdmin(req)) {
            model.addAttribute("mensajeError", "Solo el administrador puede cambiar el tipo de negocio.");
            cargarPerfil(req, model, usernameOpt.get());
            return "perfil";
        }
        String tipoNegocio = req.getParameter("tipoNegocio");
        Map<String, String> errores = new HashMap<>();
        if (!TipoNegocioUtil.esValido(tipoNegocio)) {
            errores.put("tipoNegocio", "Selecciona un tipo de negocio valido");
        }
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            cargarPerfil(req, model, usernameOpt.get());
            return "perfil";
        }
        try {
            usuarioService.actualizarTipoNegocio(usernameOpt.get(), tipoNegocio);
            model.addAttribute("mensajeExito", "Tipo de negocio actualizado.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        cargarPerfil(req, model, usernameOpt.get());
        return "perfil";
    }

    @PostMapping("/datos-fiscales")
    public String guardarDatosFiscales(HttpServletRequest req, Model model) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        if (!RolUtil.esAdmin(req)) {
            model.addAttribute("mensajeError", "Solo el administrador puede editar los datos fiscales del negocio.");
            cargarPerfil(req, model, usernameOpt.get());
            return "perfil";
        }
        String tenant = TenantUtil.getTenantOwner(req);
        DatosFiscalesNegocio datos = new DatosFiscalesNegocio();
        datos.setRfc(req.getParameter("rfcDefault"));
        datos.setRazonSocial(req.getParameter("razonSocialDefault"));
        datos.setEmail(req.getParameter("emailFiscalDefault"));
        datos.setDireccion(req.getParameter("direccionDefault"));
        datos.setUsoCfdi(req.getParameter("usoCfdiDefault"));
        datos.setCodigoPostal(req.getParameter("codigoPostalEmisor"));
        datos.setRegimenFiscal(req.getParameter("regimenFiscalEmisor"));
        try {
            datosFiscalesNegocioService.guardar(tenant, datos);
            model.addAttribute("mensajeExito", "Datos fiscales por defecto guardados.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        cargarPerfil(req, model, usernameOpt.get());
        return "perfil";
    }

    @PostMapping("/facturama-cfdi")
    public String guardarFacturamaCfdi(HttpServletRequest req, Model model) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        if (!RolUtil.esAdmin(req)) {
            model.addAttribute("mensajeError", "Solo el administrador puede configurar Facturama.");
            cargarPerfil(req, model, usernameOpt.get());
            return "perfil";
        }
        String tenant = TenantUtil.getTenantOwner(req);
        boolean sandbox = req.getParameter("facturamaSandbox") != null;
        boolean habilitado = req.getParameter("cfdiHabilitado") != null;
        try {
            datosFiscalesNegocioService.guardarConfiguracionFacturama(
                    tenant,
                    req.getParameter("facturamaUsername"),
                    req.getParameter("facturamaPassword"),
                    sandbox,
                    habilitado);
            model.addAttribute("mensajeExito", "Conexion Facturama guardada.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        cargarPerfil(req, model, usernameOpt.get());
        return "perfil";
    }

    @PostMapping("/password")
    public String cambiarPassword(HttpServletRequest req, Model model) {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            return "redirect:/login";
        }
        String passwordActual = req.getParameter("passwordActual");
        String passwordNueva = req.getParameter("passwordNueva");
        String passwordConfirmacion = req.getParameter("passwordConfirmacion");

        Map<String, String> errores = new HashMap<>();
        if (passwordActual == null || passwordActual.isBlank()) {
            errores.put("passwordActual", "Indica tu contraseña actual");
        }
        if (passwordNueva == null || passwordNueva.isBlank()) {
            errores.put("passwordNueva", "Indica la nueva contraseña");
        }
        if (passwordConfirmacion == null || !passwordConfirmacion.equals(passwordNueva)) {
            errores.put("passwordConfirmacion", "Las contraseñas nuevas no coinciden");
        }
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            cargarPerfil(req, model, usernameOpt.get());
            return "perfil";
        }

        try {
            usuarioService.cambiarPassword(usernameOpt.get(), passwordActual, passwordNueva);
            model.addAttribute("mensajeExito", "Contraseña actualizada correctamente.");
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
        }
        cargarPerfil(req, model, usernameOpt.get());
        return "perfil";
    }

    private void cargarPerfil(HttpServletRequest req, Model model, String username) {
        usuarioService.porUsername(username).ifPresent(u -> {
            model.addAttribute("usuario", u);
            model.addAttribute("username", u.getUsername());
            model.addAttribute("email", u.getEmail());
            model.addAttribute("rolEtiqueta", etiquetaRol(u));
            model.addAttribute("tipoNegocio", u.getTipoNegocio());
            model.addAttribute("tipoNegocioEtiqueta", TipoNegocioUtil.etiqueta(u.getTipoNegocio()));
            if (u.getAdminOwner() != null && !u.getAdminOwner().isBlank()) {
                model.addAttribute("adminOwner", u.getAdminOwner());
            }
        });

        model.addAttribute("tiposNegocio", TipoNegocioUtil.opciones());
        model.addAttribute("esAdmin", RolUtil.esAdmin(req));

        String tenant = TenantUtil.getTenantOwner(req);
        if (tenant != null && !tenant.isBlank()) {
            model.addAttribute("tenantOwner", tenant);
            model.addAttribute("tenantTieneLogo", tenantLogoService.tieneLogo(tenant));
            if (tenantLogoService.tieneLogo(tenant)) {
                model.addAttribute("tenantLogoUrl", tenantLogoService.urlLogo(req.getContextPath()));
            }
            cargarUsoPlan(tenant, model);
            if (RolUtil.esAdmin(req)) {
                cargarResumenSuscripcion(tenant, model);
                datosFiscalesNegocioService.consultar(tenant).ifPresent(d -> model.addAttribute("datosFiscales", d));
                model.addAttribute("cfdiTimbradoDisponible", cfdiTimbradoService.disponible(tenant));
                model.addAttribute("cfdiFacturamaPropio", cfdiTimbradoService.usaCredencialesTenant(tenant));
                cargarPreferencias(tenant, model);
            } else {
                usuarioService.porUsername(tenant).ifPresent(admin ->
                        model.addAttribute("tipoNegocioEtiqueta", TipoNegocioUtil.etiqueta(admin.getTipoNegocio())));
                cargarActividadVendedor(username, model);
            }
        }
    }

    private void cargarActividadVendedor(String username, Model model) {
        model.addAttribute("resumenMes", actividadVendedorService.resumenMesActual(username));
        model.addAttribute("mesActividad", actividadVendedorService.etiquetaMesActual());
        model.addAttribute("ticketsRecientes", actividadVendedorService.ticketsRecientes(username));
        model.addAttribute("formatoTicket", actividadVendedorService.formatoTicket());
    }

    private void cargarPreferencias(String tenant, Model model) {
        preferenciasTenantService.consultar(tenant).ifPresent(p -> {
            model.addAttribute("preferencias", p);
            if (p.getStockMinimo() != null) {
                model.addAttribute("stockMinimoTenant", p.getStockMinimo());
            }
        });
        model.addAttribute("stockMinimoGlobal", inventarioAlertaService.getStockMinimo());
        model.addAttribute("stockMinimoEfectivo", inventarioAlertaService.getStockMinimo(tenant));
    }

    private void cargarUsoPlan(String tenant, Model model) {
        PlanSuscripcion plan = planLimiteService.planActivo(tenant);
        int vendedoresUsados = planLimiteService.contarVendedores(tenant);
        int productosUsados = planLimiteService.contarProductos(tenant);
        model.addAttribute("planActivo", plan);
        model.addAttribute("vendedoresUsados", vendedoresUsados);
        model.addAttribute("productosUsados", productosUsados);
        model.addAttribute("porcentajeVendedores",
                porcentajeUso(vendedoresUsados, plan.getMaxVendedores()));
        model.addAttribute("porcentajeProductos",
                porcentajeUso(productosUsados, plan.getMaxProductos()));

        suscripcionService.consultar(tenant).ifPresent(s ->
                model.addAttribute("planNombre", PlanSuscripcion.porCodigoODefault(s.getPlanCodigo()).getNombre()));
        if (!model.containsAttribute("planNombre")) {
            model.addAttribute("planNombre", plan.getNombre());
        }
    }

    private static int porcentajeUso(int usados, int maximo) {
        if (maximo <= 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round(usados * 100.0 / maximo));
    }

    private static String etiquetaRol(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null) {
            return "Usuario";
        }
        return switch (usuario.getRol().trim().toUpperCase()) {
            case "ADMIN" -> "Administrador";
            case "VENDEDOR" -> "Vendedor";
            case "SUPER_ADMIN" -> "Operador plataforma";
            default -> usuario.getRol();
        };
    }

    private void cargarResumenSuscripcion(String tenant, Model model) {
        suscripcionService.consultar(tenant).ifPresentOrElse(s -> {
            model.addAttribute("suscripcion", s);
            model.addAttribute("fechaFinTexto", s.getFechaFin().format(FORMATO));
            model.addAttribute("planNombre", PlanSuscripcion.porCodigoODefault(s.getPlanCodigo()).getNombre());
            model.addAttribute("suscripcionVigente", s.estaVigente());
        }, () -> model.addAttribute("sinSuscripcion", true));
    }
}
