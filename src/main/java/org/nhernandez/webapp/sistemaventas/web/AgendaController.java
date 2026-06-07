package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.models.CitaServicio;
import org.nhernandez.webapp.sistemaventas.models.EstadoCita;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.services.CitaServicioService;
import org.nhernandez.webapp.sistemaventas.services.ClienteService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FECHA_HORA_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final CitaServicioService citaServicioService;
    private final ClienteService clienteService;
    private final LoginService loginService;
    private final UsuarioService usuarioService;

    public AgendaController(CitaServicioService citaServicioService,
                            ClienteService clienteService,
                            LoginService loginService,
                            UsuarioService usuarioService) {
        this.citaServicioService = citaServicioService;
        this.clienteService = clienteService;
        this.loginService = loginService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String agenda(@RequestParam(value = "fecha", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         HttpServletRequest req,
                         Model model) {
        String bloqueo = verificarAccesoAgenda(req);
        if (bloqueo != null) {
            return bloqueo;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        model.addAttribute("fecha", dia);
        model.addAttribute("fechaAnterior", dia.minusDays(1));
        model.addAttribute("fechaSiguiente", dia.plusDays(1));
        model.addAttribute("citas", citaServicioService.listarDelDia(tenant, dia));
        model.addAttribute("esHoy", dia.equals(LocalDate.now()));
        mensajeSesion(req, model);
        return "agenda";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(value = "fecha", required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                        HttpServletRequest req,
                        Model model) {
        String bloqueo = verificarAccesoAgenda(req);
        if (bloqueo != null) {
            return bloqueo;
        }
        return formulario(req, model, fecha, new CitaServicio());
    }

    @GetMapping("/editar")
    public String editar(@RequestParam("id") long id, HttpServletRequest req, Model model) {
        String bloqueo = verificarAccesoAgenda(req);
        if (bloqueo != null) {
            return bloqueo;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        return citaServicioService.porId(tenant, id)
                .map(cita -> {
                    if (!cita.isEditable()) {
                        req.getSession().setAttribute("mensajeError", "Esta cita ya no se puede editar.");
                        return "redirect:/agenda?fecha=" + cita.getFechaHora().toLocalDate();
                    }
                    return formulario(req, model, cita.getFechaHora().toLocalDate(), cita);
                })
                .orElse("redirect:/agenda");
    }

    @PostMapping("/guardar")
    public String guardar(HttpServletRequest req, Model model) {
        String bloqueo = verificarAccesoAgenda(req);
        if (bloqueo != null) {
            return bloqueo;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        String username = loginService.getUsername(req).orElse("");

        CitaServicio cita = new CitaServicio();
        cita.setId(parseLong(req.getParameter("id"), 0L));
        cita.setTenantOwner(tenant);
        cita.setProductoId(parseLong(req.getParameter("productoId"), 0L));
        cita.setClienteId(parseLong(req.getParameter("clienteId"), 0L));
        cita.setFechaHora(parseFechaHora(req.getParameter("fechaHora")));
        cita.setDuracionMinutos(parseInt(req.getParameter("duracionMinutos"), 30));
        cita.setNotas(limpiar(req.getParameter("notas")));
        EstadoCita.porCodigo(req.getParameter("estado")).ifPresent(cita::setEstado);

        boolean esEdicion = cita.getId() != null && cita.getId() > 0;
        Map<String, String> errores = citaServicioService.validar(cita, esEdicion);
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("cita", cita);
            prepararFormulario(tenant, model, cita.getFechaHora() != null
                    ? cita.getFechaHora().toLocalDate()
                    : LocalDate.now());
            return "agendaForm";
        }

        try {
            CitaServicio guardada = citaServicioService.guardar(tenant, username, cita);
            req.getSession().setAttribute("mensajeExito", esEdicion
                    ? "Cita actualizada correctamente."
                    : "Cita agendada correctamente.");
            return "redirect:/agenda?fecha=" + guardada.getFechaHora().toLocalDate();
        } catch (ServiceJdbcException e) {
            model.addAttribute("errores", Map.of("general", e.getMessage()));
            model.addAttribute("cita", cita);
            prepararFormulario(tenant, model, cita.getFechaHora() != null
                    ? cita.getFechaHora().toLocalDate()
                    : LocalDate.now());
            return "agendaForm";
        }
    }

    @PostMapping("/confirmar")
    public String confirmar(@RequestParam("id") long id,
                            @RequestParam(value = "fecha", required = false) String fecha,
                            HttpServletRequest req) {
        String bloqueo = verificarAccesoAgenda(req);
        if (bloqueo != null) {
            return bloqueo;
        }
        return accionEstado(id, fecha, req, () -> citaServicioService.confirmar(TenantUtil.getTenantOwner(req), id),
                "Cita confirmada.");
    }

    @PostMapping("/cancelar")
    public String cancelar(@RequestParam("id") long id,
                           @RequestParam(value = "fecha", required = false) String fecha,
                           HttpServletRequest req) {
        String bloqueo = verificarAccesoAgenda(req);
        if (bloqueo != null) {
            return bloqueo;
        }
        return accionEstado(id, fecha, req, () -> citaServicioService.cancelar(TenantUtil.getTenantOwner(req), id),
                "Cita cancelada.");
    }

    @PostMapping("/completar")
    public String completar(@RequestParam("id") long id,
                            @RequestParam(value = "fecha", required = false) String fecha,
                            HttpServletRequest req) {
        String bloqueo = verificarAccesoAgenda(req);
        if (bloqueo != null) {
            return bloqueo;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        try {
            citaServicioService.marcarCompletada(tenant, id);
            return citaServicioService.porId(tenant, id)
                    .map(c -> "redirect:/carro/agregar?id=" + c.getProductoId())
                    .orElse(redirectAgenda(fecha));
        } catch (ServiceJdbcException e) {
            req.getSession().setAttribute("mensajeError", e.getMessage());
            return redirectAgenda(fecha);
        }
    }

    private String verificarAccesoAgenda(HttpServletRequest req) {
        String tenant = TenantUtil.getTenantOwner(req);
        boolean permitido = usuarioService.porUsername(tenant)
                .map(u -> TipoNegocioUtil.tieneOpcionServicios(u.getTipoNegocio()))
                .orElse(false);
        if (permitido) {
            return null;
        }
        req.getSession().setAttribute("mensajeError",
                "La agenda de servicios no esta disponible para el tipo de negocio de tu cuenta.");
        return "redirect:/inicio";
    }

    private String formulario(HttpServletRequest req, Model model, LocalDate fechaRef, CitaServicio cita) {
        String tenant = TenantUtil.getTenantOwner(req);
        if (cita.getFechaHora() == null) {
            LocalDate base = fechaRef != null ? fechaRef : LocalDate.now();
            cita.setFechaHora(base.atTime(9, 0));
        }
        if (cita.getDuracionMinutos() <= 0) {
            cita.setDuracionMinutos(30);
        }
        model.addAttribute("cita", cita);
        prepararFormulario(tenant, model, fechaRef != null ? fechaRef : cita.getFechaHora().toLocalDate());
        return "agendaForm";
    }

    private void prepararFormulario(String tenant, Model model, LocalDate fecha) {
        List<Producto> servicios = citaServicioService.listarServicios(tenant);
        model.addAttribute("servicios", servicios);
        model.addAttribute("clientes", clienteService.listarActivos(tenant));
        model.addAttribute("estadosCita", EstadoCita.values());
        model.addAttribute("fechaAgenda", fecha != null ? fecha : LocalDate.now());
        if (servicios.isEmpty()) {
            model.addAttribute("sinServicios", true);
        }
    }

    private String accionEstado(long id, String fecha, HttpServletRequest req, Runnable accion, String mensajeOk) {
        try {
            accion.run();
            req.getSession().setAttribute("mensajeExito", mensajeOk);
        } catch (ServiceJdbcException e) {
            req.getSession().setAttribute("mensajeError", e.getMessage());
        }
        return redirectAgenda(fecha);
    }

    private static String redirectAgenda(String fecha) {
        if (fecha != null && !fecha.isBlank()) {
            return "redirect:/agenda?fecha=" + fecha.trim();
        }
        return "redirect:/agenda";
    }

    private static void mensajeSesion(HttpServletRequest req, Model model) {
        Object ok = req.getSession().getAttribute("mensajeExito");
        if (ok != null) {
            model.addAttribute("mensajeExito", ok);
            req.getSession().removeAttribute("mensajeExito");
        }
        Object err = req.getSession().getAttribute("mensajeError");
        if (err != null) {
            model.addAttribute("mensajeError", err);
            req.getSession().removeAttribute("mensajeError");
        }
    }

    private static LocalDateTime parseFechaHora(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor.trim(), FECHA_HORA_LOCAL);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String limpiar(String value) {
        return value != null ? value.trim() : null;
    }
}
