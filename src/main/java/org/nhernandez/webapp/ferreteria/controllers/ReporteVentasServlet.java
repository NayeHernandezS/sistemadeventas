package org.nhernandez.webapp.ferreteria.controllers;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.ferreteria.models.TicketVenta;
import org.nhernandez.webapp.ferreteria.repositories.TicketRepository;
import org.nhernandez.webapp.ferreteria.services.LoginService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet("/reportes")
public class ReporteVentasServlet extends HttpServlet {

    @Inject
    private TicketRepository ticketRepository;

    @Inject
    private LoginService loginService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión para consultar reportes.");
            return;
        }

        List<TicketVenta> tickets;
        try {
            if (esAdmin(req)) {
                tickets = ticketRepository.listar();
            } else {
                tickets = ticketRepository.listarPorVendedor(usernameOpt.get());
            }
        } catch (SQLException e) {
            throw new ServletException("Error al consultar reportes en base de datos.", e);
        }

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
                .collect(Collectors.toCollection(java.util.TreeSet::new));

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

        req.setAttribute("ticketsDia", ticketsDia);
        req.setAttribute("ticketsSemana", ticketsSemana);
        req.setAttribute("ticketsMes", ticketsMes);
        req.setAttribute("ticketsFiltrados", ticketsFiltrados);

        req.setAttribute("totalDia", calcularTotal(ticketsDia));
        req.setAttribute("totalSemana", calcularTotal(ticketsSemana));
        req.setAttribute("totalMes", calcularTotal(ticketsMes));
        req.setAttribute("totalFiltrado", calcularTotal(ticketsFiltrados));

        req.setAttribute("cantidadDia", ticketsDia.size());
        req.setAttribute("cantidadSemana", ticketsSemana.size());
        req.setAttribute("cantidadMes", ticketsMes.size());
        req.setAttribute("cantidadFiltrada", ticketsFiltrados.size());

        req.setAttribute("vendedores", vendedores);
        req.setAttribute("vendedorSeleccionado", vendedor);
        req.setAttribute("fechaInicio", fechaInicio != null ? fechaInicio.toString() : "");
        req.setAttribute("fechaFin", fechaFin != null ? fechaFin.toString() : "");

        getServletContext().getRequestDispatcher("/reportes.jsp").forward(req, resp);
    }

    private int calcularTotal(List<TicketVenta> tickets) {
        return tickets.stream().mapToInt(TicketVenta::getTotal).sum();
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim();
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
        if (vendedor.isBlank()) {
            return true;
        }
        return vendedor.equalsIgnoreCase(ticket.getUsernameVendedor());
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

    private boolean esAdmin(HttpServletRequest req) {
        Object rol = req.getSession().getAttribute("rol");
        return rol != null && "ADMIN".equalsIgnoreCase(rol.toString());
    }
}
