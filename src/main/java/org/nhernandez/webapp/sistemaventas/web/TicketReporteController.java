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
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.util.FacturaPdfExporter;
import org.nhernandez.webapp.sistemaventas.util.ReporteCsvExporter;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Controller
public class TicketReporteController {

    private static final DateTimeFormatter NOMBRE_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TicketRepository ticketRepository;
    private final FacturaRepository facturaRepository;
    private final LoginService loginService;
    private final ReporteService reporteService;
    private final ReporteCsvExporter reporteCsvExporter;
    private final FacturaPdfExporter facturaPdfExporter;

    public TicketReporteController(TicketRepository ticketRepository,
                                   FacturaRepository facturaRepository,
                                   LoginService loginService,
                                   ReporteService reporteService,
                                   ReporteCsvExporter reporteCsvExporter,
                                   FacturaPdfExporter facturaPdfExporter) {
        this.ticketRepository = ticketRepository;
        this.facturaRepository = facturaRepository;
        this.loginService = loginService;
        this.reporteService = reporteService;
        this.reporteCsvExporter = reporteCsvExporter;
        this.facturaPdfExporter = facturaPdfExporter;
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
        Optional<TicketVenta> ticketOpt = cargarTicket(req, resp);
        if (ticketOpt.isEmpty()) {
            return null;
        }
        TicketVenta ticket = ticketOpt.get();
        Factura factura = facturaRepository.porTicketId(ticket.getId());
        model.addAttribute("ticket", ticket);
        model.addAttribute("factura", factura);
        return "factura";
    }

    @GetMapping("/factura/pdf")
    public void facturaPdf(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        Optional<TicketVenta> ticketOpt = cargarTicket(req, resp);
        if (ticketOpt.isEmpty()) {
            return;
        }
        TicketVenta ticket = ticketOpt.get();
        Factura factura = facturaRepository.porTicketId(ticket.getId());
        if (factura == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Este ticket no tiene factura registrada.");
            return;
        }
        try {
            byte[] pdf = facturaPdfExporter.exportar(factura, ticket);
            String nombre = "factura-" + factura.getFolioFactura() + ".pdf";
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
            resp.setContentLength(pdf.length);
            resp.getOutputStream().write(pdf);
            resp.getOutputStream().flush();
        } catch (ServiceJdbcException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/reportes")
    public String reportes(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        Optional<ReporteVentas> reporteOpt = generarReporteAutenticado(req, resp);
        if (reporteOpt.isEmpty()) {
            return null;
        }
        model.addAttribute("reporte", reporteOpt.get());
        return "reportes";
    }

    @GetMapping("/reportes/export")
    public void exportarReportes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<ReporteVentas> reporteOpt = generarReporteAutenticado(req, resp);
        if (reporteOpt.isEmpty()) {
            return;
        }
        byte[] csv = reporteCsvExporter.exportar(reporteOpt.get());
        String nombre = "reporte-ventas-" + LocalDate.now().format(NOMBRE_ARCHIVO) + ".csv";
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
        resp.setContentLength(csv.length);
        resp.getOutputStream().write(csv);
        resp.getOutputStream().flush();
    }

    private Optional<TicketVenta> cargarTicket(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {
        String folioTicket = req.getParameter("folioTicket");
        if (folioTicket == null || folioTicket.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Indique el folio del ticket.");
            return Optional.empty();
        }
        String tenant = TenantUtil.getTenantOwner(req);
        if (tenant == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesion.");
            return Optional.empty();
        }
        TicketVenta ticket = ticketRepository.porFolioDeTenant(folioTicket.trim(), tenant);
        if (ticket == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Ticket no encontrado.");
            return Optional.empty();
        }
        return Optional.of(ticket);
    }

    private Optional<ReporteVentas> generarReporteAutenticado(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión para consultar reportes.");
            return Optional.empty();
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
        return Optional.of(reporte);
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
