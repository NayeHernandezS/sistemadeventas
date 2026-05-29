package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Devolucion;
import org.nhernandez.webapp.sistemaventas.models.LineaDevolucionVista;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.services.DevolucionService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/devoluciones")
public class DevolucionController {

    private final DevolucionService devolucionService;
    private final TicketRepository ticketRepository;
    private final LoginService loginService;

    public DevolucionController(DevolucionService devolucionService,
                                TicketRepository ticketRepository,
                                LoginService loginService) {
        this.devolucionService = devolucionService;
        this.ticketRepository = ticketRepository;
        this.loginService = loginService;
    }

    @GetMapping
    public String listar(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!autenticado(req, resp)) {
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        model.addAttribute("devoluciones", devolucionService.listarPorTenant(tenant));
        return "devoluciones";
    }

    @GetMapping("/nueva")
    public String nueva(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException, SQLException {
        if (!autenticado(req, resp)) {
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        Long ticketId = parseLong(req.getParameter("ticketId"));
        String folio = req.getParameter("folio");

        TicketVenta ticket = null;
        if (ticketId != null && ticketId > 0) {
            ticket = ticketRepository.porIdDeTenant(ticketId, tenant);
        } else if (folio != null && !folio.isBlank()) {
            ticket = ticketRepository.porFolioDeTenant(folio.trim(), tenant);
        }

        if (ticket == null) {
            model.addAttribute("mensajeError", "Indica un ticket valido (folio o id).");
            model.addAttribute("devoluciones", devolucionService.listarPorTenant(tenant));
            return "devoluciones";
        }

        try {
            ticket = devolucionService.obtenerTicketParaDevolucion(ticket.getId(), tenant);
            List<LineaDevolucionVista> lineas = devolucionService.lineasDisponibles(ticket);
            if (lineas.isEmpty()) {
                model.addAttribute("mensajeError", "Este ticket no tiene productos pendientes de devolver.");
                return "devoluciones";
            }
            model.addAttribute("ticket", ticket);
            model.addAttribute("lineas", lineas);
            return "devolucionForm";
        } catch (ServiceJdbcException e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("devoluciones", devolucionService.listarPorTenant(tenant));
            return "devoluciones";
        }
    }

    @PostMapping("/registrar")
    public String registrar(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!autenticado(req, resp)) {
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        String username = loginService.getUsername(req).orElse("");
        Long ticketId = parseLong(req.getParameter("ticketId"));
        String motivo = req.getParameter("motivo");

        Map<Long, Integer> cantidades = new HashMap<>();
        req.getParameterMap().forEach((name, values) -> {
            if (name.startsWith("cant_") && values != null && values.length > 0) {
                try {
                    long productoId = Long.parseLong(name.substring(5));
                    int cant = Integer.parseInt(values[0]);
                    if (cant > 0) {
                        cantidades.put(productoId, cant);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        });

        if (ticketId == null || ticketId <= 0) {
            model.addAttribute("mensajeError", "Ticket no valido");
            model.addAttribute("devoluciones", devolucionService.listarPorTenant(tenant));
            return "devoluciones";
        }

        try {
            Devolucion devolucion = devolucionService.registrarDevolucion(
                    ticketId, tenant, username, cantidades, motivo);
            model.addAttribute("mensajeExito",
                    "Devolucion " + devolucion.getFolio() + " registrada. Inventario actualizado.");
            model.addAttribute("devoluciones", devolucionService.listarPorTenant(tenant));
            return "devoluciones";
        } catch (ServiceJdbcException e) {
            try {
                TicketVenta ticket = devolucionService.obtenerTicketParaDevolucion(ticketId, tenant);
                model.addAttribute("ticket", ticket);
                model.addAttribute("lineas", devolucionService.lineasDisponibles(ticket));
                model.addAttribute("mensajeError", e.getMessage());
                model.addAttribute("motivo", motivo);
                return "devolucionForm";
            } catch (ServiceJdbcException ex) {
                model.addAttribute("mensajeError", ex.getMessage());
                model.addAttribute("devoluciones", devolucionService.listarPorTenant(tenant));
                return "devoluciones";
            }
        }
    }

    private boolean autenticado(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<String> user = loginService.getUsername(req);
        if (user.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesion");
            return false;
        }
        return true;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
