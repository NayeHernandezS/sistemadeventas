package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.FacturaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.nhernandez.webapp.sistemaventas.services.CfdiTimbradoService;
import org.nhernandez.webapp.sistemaventas.services.ClienteService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ReporteService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.util.FacturaDatosUtil;
import org.nhernandez.webapp.sistemaventas.util.FacturaPdfExporter;
import org.nhernandez.webapp.sistemaventas.util.ReporteCsvExporter;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
    private final CfdiTimbradoService cfdiTimbradoService;
    private final ClienteService clienteService;

    public TicketReporteController(TicketRepository ticketRepository,
                                   FacturaRepository facturaRepository,
                                   LoginService loginService,
                                   ReporteService reporteService,
                                   ReporteCsvExporter reporteCsvExporter,
                                   FacturaPdfExporter facturaPdfExporter,
                                   CfdiTimbradoService cfdiTimbradoService,
                                   ClienteService clienteService) {
        this.ticketRepository = ticketRepository;
        this.facturaRepository = facturaRepository;
        this.loginService = loginService;
        this.reporteService = reporteService;
        this.reporteCsvExporter = reporteCsvExporter;
        this.facturaPdfExporter = facturaPdfExporter;
        this.cfdiTimbradoService = cfdiTimbradoService;
        this.clienteService = clienteService;
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

    @GetMapping("/tickets/ver")
    public String verTicket(HttpServletRequest req, Model model, HttpServletResponse resp)
            throws IOException, SQLException {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesion para consultar tickets.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        if (tenant == null || tenant.isBlank()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesion.");
            return null;
        }

        TicketVenta ticket = resolverTicket(req, tenant, resp);
        if (ticket == null) {
            return null;
        }
        if (!RolUtil.esAdmin(req)
                && !usernameOpt.get().equalsIgnoreCase(ticket.getUsernameVendedor())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para ver este ticket.");
            return null;
        }

        Factura factura = facturaRepository.porTicketId(ticket.getId());
        model.addAttribute("ticket", ticket);
        model.addAttribute("factura", factura);
        model.addAttribute("tieneFactura", factura != null);

        Object mensaje = req.getSession().getAttribute("mensajeTicket");
        if (mensaje != null) {
            model.addAttribute("mensajeExito", mensaje.toString());
            req.getSession().removeAttribute("mensajeTicket");
        }
        return "ticket";
    }

    private TicketVenta resolverTicket(HttpServletRequest req, String tenant, HttpServletResponse resp)
            throws IOException, SQLException {
        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isBlank()) {
            try {
                long id = Long.parseLong(idParam.trim());
                TicketVenta ticket = ticketRepository.porIdDeTenant(id, tenant);
                if (ticket == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Ticket no encontrado.");
                }
                return ticket;
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id de ticket invalido.");
                return null;
            }
        }
        String folio = req.getParameter("folio");
        if (folio != null && !folio.isBlank()) {
            TicketVenta ticket = ticketRepository.porFolioDeTenant(folio.trim(), tenant);
            if (ticket == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Ticket no encontrado.");
            }
            return ticket;
        }
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Indique id o folio del ticket.");
        return null;
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
        model.addAttribute("cfdiTimbradoDisponible", cfdiTimbradoService.disponible());
        model.addAttribute("esAdmin", RolUtil.esAdmin(req));
        if (factura != null && factura.getClienteId() != null && factura.getClienteId() > 0) {
            String tenant = TenantUtil.getTenantOwner(req);
            clienteService.porId(tenant, factura.getClienteId())
                    .ifPresent(c -> model.addAttribute("clienteCatalogo", c));
        }
        Object avisoCfdi = req.getSession().getAttribute("mensajeCfdi");
        if (avisoCfdi != null) {
            model.addAttribute("mensajeCfdi", avisoCfdi.toString());
            req.getSession().removeAttribute("mensajeCfdi");
        }
        return "factura";
    }

    @PostMapping("/factura/reintentar-cfdi")
    public String reintentarCfdi(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede reintentar el timbrado CFDI.");
            return null;
        }
        if (!cfdiTimbradoService.disponible()) {
            req.getSession().setAttribute("mensajeCfdi",
                    "Timbrado CFDI no configurado en el servidor.");
            return redirectFactura(req, req.getParameter("folioTicket"));
        }

        Optional<TicketVenta> ticketOpt = cargarTicket(req, resp);
        if (ticketOpt.isEmpty()) {
            return null;
        }
        TicketVenta ticket = ticketOpt.get();
        Factura factura = facturaRepository.porTicketId(ticket.getId());
        if (factura == null) {
            req.getSession().setAttribute("mensajeCfdi", "Este ticket no tiene factura registrada.");
            return redirectFactura(req, ticket.getFolio());
        }

        String tenant = TenantUtil.getTenantOwner(req);
        aplicarCorreccionesReceptor(req, factura, tenant);

        try {
            String mensaje = cfdiTimbradoService.reintentarTimbrar(tenant, ticket, factura);
            req.getSession().setAttribute("mensajeCfdi", mensaje);
        } catch (ServiceJdbcException e) {
            req.getSession().setAttribute("mensajeCfdi", e.getMessage());
        }
        return redirectFactura(req, ticket.getFolio());
    }

    private void aplicarCorreccionesReceptor(HttpServletRequest req, Factura factura, String tenant)
            throws SQLException {
        String rfc = limpiarTexto(req.getParameter("rfc"));
        String razonSocial = limpiarTexto(req.getParameter("razonSocial"));
        String email = limpiarTexto(req.getParameter("emailFactura"));
        String direccion = limpiarTexto(req.getParameter("direccionFactura"));
        String usoCfdi = limpiarTexto(req.getParameter("usoCfdi"));
        String cp = limpiarTexto(req.getParameter("codigoPostalReceptor"));

        boolean cambio = false;
        if (!rfc.isBlank()) {
            String error = FacturaDatosUtil.validarRfcObligatorio(rfc);
            if (error != null) {
                throw new ServiceJdbcException(error, null);
            }
            factura.setRfc(FacturaDatosUtil.normalizarRfc(rfc));
            cambio = true;
        }
        if (!razonSocial.isBlank()) {
            factura.setRazonSocial(razonSocial);
            cambio = true;
        }
        if (!email.isBlank()) {
            factura.setEmail(email);
            cambio = true;
        }
        if (!direccion.isBlank()) {
            factura.setDireccion(direccion);
            cambio = true;
        }
        if (!usoCfdi.isBlank()) {
            factura.setUsoCfdi(usoCfdi.toUpperCase());
            cambio = true;
        }
        if (!cp.isBlank()) {
            if (!cp.matches("\\d{5}")) {
                throw new ServiceJdbcException("Codigo postal invalido (5 digitos).", null);
            }
            factura.setCodigoPostalReceptor(cp);
            cambio = true;
        }
        if (cambio) {
            facturaRepository.actualizarDatosReceptor(factura, tenant);
        }
    }

    private String redirectFactura(HttpServletRequest req, String folio) {
        String base = req.getContextPath() + "/factura?folioTicket=";
        if (folio == null || folio.isBlank()) {
            return "redirect:/tickets";
        }
        return "redirect:" + base + folio.trim();
    }

    private String limpiarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    @GetMapping("/factura/cfdi/pdf")
    public void facturaCfdiPdf(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        Optional<TicketVenta> ticketOpt = cargarTicket(req, resp);
        if (ticketOpt.isEmpty()) {
            return;
        }
        Factura factura = facturaRepository.porTicketId(ticketOpt.get().getId());
        if (factura == null || !factura.estaTimbrada()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No hay CFDI timbrado para este ticket.");
            return;
        }
        try {
            byte[] pdf = cfdiTimbradoService.descargarPdfTimbrado(factura);
            String nombre = "cfdi-" + (factura.getCfdiUuid() != null ? factura.getCfdiUuid() : factura.getFolioFactura()) + ".pdf";
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
            resp.setContentLength(pdf.length);
            resp.getOutputStream().write(pdf);
            resp.getOutputStream().flush();
        } catch (ServiceJdbcException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/factura/cfdi/xml")
    public void facturaCfdiXml(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        Optional<TicketVenta> ticketOpt = cargarTicket(req, resp);
        if (ticketOpt.isEmpty()) {
            return;
        }
        Factura factura = facturaRepository.porTicketId(ticketOpt.get().getId());
        if (factura == null || !factura.estaTimbrada()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No hay CFDI timbrado para este ticket.");
            return;
        }
        try {
            byte[] xml = cfdiTimbradoService.descargarXmlTimbrado(factura);
            String nombre = "cfdi-" + (factura.getCfdiUuid() != null ? factura.getCfdiUuid() : factura.getFolioFactura()) + ".xml";
            resp.setContentType("application/xml");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
            resp.setContentLength(xml.length);
            resp.getOutputStream().write(xml);
            resp.getOutputStream().flush();
        } catch (ServiceJdbcException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
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
