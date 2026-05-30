package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.FacturaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ReporteService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Controller
public class TicketReporteController {

    private final TicketRepository ticketRepository;
    private final FacturaRepository facturaRepository;
    private final LoginService loginService;
    private final ReporteService reporteService;

    public TicketReporteController(TicketRepository ticketRepository,
                                   FacturaRepository facturaRepository,
                                   LoginService loginService,
                                   ReporteService reporteService) {
        this.ticketRepository = ticketRepository;
        this.facturaRepository = facturaRepository;
        this.loginService = loginService;
        this.reporteService = reporteService;
    }

    @GetMapping("/tickets")
    public String tickets(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException, SQLException {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión para consultar tickets.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        List<TicketVenta> tickets = RolUtil.esAdmin(req)
                ? ticketRepository.listarPorTenant(tenant)
                : ticketRepository.listarPorVendedor(usernameOpt.get());
        model.addAttribute("tickets", tickets);
        return "tickets";
    }

    @GetMapping("/factura")
    public String factura(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException, SQLException {
        String folioTicket = req.getParameter("folioTicket");
        if (folioTicket == null || folioTicket.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Indique el folio del ticket.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        if (tenant == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesion.");
            return null;
        }
        TicketVenta ticket = ticketRepository.porFolioDeTenant(folioTicket.trim(), tenant);
        if (ticket == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Ticket no encontrado.");
            return null;
        }
        Factura factura = facturaRepository.porTicketId(ticket.getId());
        model.addAttribute("ticket", ticket);
        model.addAttribute("factura", factura);
        return "factura";
    }

    @GetMapping("/reportes")
    public String reportes(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión para consultar reportes.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        ReporteVentas reporte = reporteService.generar(
                tenant,
                usernameOpt.get(),
                RolUtil.esAdmin(req),
                limpiarTexto(req.getParameter("vendedor")),
                parseFecha(req.getParameter("fechaInicio")),
                parseFecha(req.getParameter("fechaFin"))
        );
        model.addAttribute("reporte", reporte);
        return "reportes";
    }

    private String limpiarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private LocalDate parseFecha(String fechaTexto) {
        if (fechaTexto == null || fechaTexto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(fechaTexto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
