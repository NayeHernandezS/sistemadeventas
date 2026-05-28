package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.FacturaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Controller
public class TicketReporteController {

    private final TicketRepository ticketRepository;
    private final FacturaRepository facturaRepository;
    private final LoginService loginService;

    public TicketReporteController(TicketRepository ticketRepository,
                                   FacturaRepository facturaRepository,
                                   LoginService loginService) {
        this.ticketRepository = ticketRepository;
        this.facturaRepository = facturaRepository;
        this.loginService = loginService;
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
    public String reportes(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException, SQLException {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión para consultar reportes.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        List<TicketVenta> tickets = RolUtil.esAdmin(req)
                ? ticketRepository.listarPorTenant(tenant)
                : ticketRepository.listarPorVendedor(usernameOpt.get());

        String vendedor = limpiarTexto(req.getParameter("vendedor"));
        LocalDate fechaInicio = parseFecha(req.getParameter("fechaInicio"));
        LocalDate fechaFin = parseFecha(req.getParameter("fechaFin"));

        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            LocalDate temporal = fechaInicio;
            fechaInicio = fechaFin;
            fechaFin = temporal;
        }

        final LocalDate fechaInicioFiltro = fechaInicio;
        final LocalDate fechaFinFiltro = fechaFin;

        List<TicketVenta> ticketsFiltrados = tickets.stream()
                .filter(t -> coincideVendedor(t, vendedor))
                .filter(t -> coincideRango(t, fechaInicioFiltro, fechaFinFiltro))
                .collect(Collectors.toList());

        Set<String> vendedores = tickets.stream()
                .map(TicketVenta::getUsernameVendedor)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));

        LocalDate hoy = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int semanaActual = hoy.get(weekFields.weekOfWeekBasedYear());
        int anioActual = hoy.getYear();
        int mesActual = hoy.getMonthValue();

        List<TicketVenta> ticketsDia = ticketsFiltrados.stream()
                .filter(t -> t.getFechaVenta() != null && t.getFechaVenta().toLocalDate().equals(hoy))
                .collect(Collectors.toList());

        List<TicketVenta> ticketsSemana = ticketsFiltrados.stream()
                .filter(t -> {
                    if (t.getFechaVenta() == null) {
                        return false;
                    }
                    LocalDate fecha = t.getFechaVenta().toLocalDate();
                    int semanaTicket = fecha.get(weekFields.weekOfWeekBasedYear());
                    return fecha.getYear() == anioActual && semanaTicket == semanaActual;
                })
                .collect(Collectors.toList());

        List<TicketVenta> ticketsMes = ticketsFiltrados.stream()
                .filter(t -> t.getFechaVenta() != null
                        && t.getFechaVenta().getYear() == anioActual
                        && t.getFechaVenta().getMonthValue() == mesActual)
                .collect(Collectors.toList());

        model.addAttribute("ticketsDia", ticketsDia);
        model.addAttribute("ticketsSemana", ticketsSemana);
        model.addAttribute("ticketsMes", ticketsMes);
        model.addAttribute("ticketsFiltrados", ticketsFiltrados);
        model.addAttribute("totalDia", calcularTotal(ticketsDia));
        model.addAttribute("totalSemana", calcularTotal(ticketsSemana));
        model.addAttribute("totalMes", calcularTotal(ticketsMes));
        model.addAttribute("totalFiltrado", calcularTotal(ticketsFiltrados));
        model.addAttribute("cantidadDia", ticketsDia.size());
        model.addAttribute("cantidadSemana", ticketsSemana.size());
        model.addAttribute("cantidadMes", ticketsMes.size());
        model.addAttribute("cantidadFiltrada", ticketsFiltrados.size());
        model.addAttribute("vendedores", vendedores);
        model.addAttribute("vendedorSeleccionado", vendedor);
        model.addAttribute("fechaInicio", fechaInicio != null ? fechaInicio.toString() : "");
        model.addAttribute("fechaFin", fechaFin != null ? fechaFin.toString() : "");
        return "reportes";
    }

    private int calcularTotal(List<TicketVenta> tickets) {
        return tickets.stream().mapToInt(TicketVenta::getTotal).sum();
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

    private boolean coincideVendedor(TicketVenta ticket, String vendedor) {
        return vendedor.isBlank() || vendedor.equalsIgnoreCase(ticket.getUsernameVendedor());
    }

    private boolean coincideRango(TicketVenta ticket, LocalDate fechaInicio, LocalDate fechaFin) {
        if (ticket.getFechaVenta() == null) {
            return false;
        }
        LocalDate fechaTicket = ticket.getFechaVenta().toLocalDate();
        if (fechaInicio != null && fechaTicket.isBefore(fechaInicio)) {
            return false;
        }
        if (fechaFin != null && fechaTicket.isAfter(fechaFin)) {
            return false;
        }
        return true;
    }
}
