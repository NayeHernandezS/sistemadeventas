package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.DevolucionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final TicketRepository ticketRepository;
    private final DevolucionRepository devolucionRepository;

    public ReporteServiceImpl(TicketRepository ticketRepository,
                              DevolucionRepository devolucionRepository) {
        this.ticketRepository = ticketRepository;
        this.devolucionRepository = devolucionRepository;
    }

    @Override
    public ReporteVentas generar(String tenantOwner, String usernameVendedor, boolean esAdmin,
                                 String vendedorFiltro, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            List<TicketVenta> tickets = esAdmin
                    ? ticketRepository.listarPorTenant(tenantOwner)
                    : ticketRepository.listarPorVendedor(usernameVendedor);

            Map<Long, Integer> devueltoPorTicket = devolucionRepository.totalesDevueltosPorTenant(tenantOwner);

            String vendedor = vendedorFiltro != null ? vendedorFiltro.trim() : "";
            LocalDate inicio = fechaInicio;
            LocalDate fin = fechaFin;
            if (inicio != null && fin != null && inicio.isAfter(fin)) {
                LocalDate tmp = inicio;
                inicio = fin;
                fin = tmp;
            }

            final LocalDate fechaInicioFiltro = inicio;
            final LocalDate fechaFinFiltro = fin;

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

            List<TicketVenta> ticketsDia = filtrarPorPeriodo(ticketsFiltrados, hoy, hoy);
            List<TicketVenta> ticketsSemana = ticketsFiltrados.stream()
                    .filter(t -> enSemanaActual(t, hoy, weekFields, anioActual, semanaActual))
                    .collect(Collectors.toList());
            List<TicketVenta> ticketsMes = ticketsFiltrados.stream()
                    .filter(t -> enMesActual(t, anioActual, mesActual))
                    .collect(Collectors.toList());

            ReporteVentas reporte = new ReporteVentas();
            reporte.setDevueltoPorTicketId(devueltoPorTicket);
            reporte.setTicketsFiltrados(ticketsFiltrados);
            reporte.setTicketsDia(ticketsDia);
            reporte.setTicketsSemana(ticketsSemana);
            reporte.setTicketsMes(ticketsMes);
            reporte.setVendedores(vendedores);
            reporte.setVendedorSeleccionado(vendedor);
            reporte.setFechaInicio(inicio != null ? inicio.toString() : "");
            reporte.setFechaFin(fin != null ? fin.toString() : "");

            reporte.setCantidadFiltrada(ticketsFiltrados.size());
            reporte.setTotalFiltradoBruto(calcularBruto(ticketsFiltrados));
            reporte.setTotalDevueltoFiltrado(calcularDevuelto(ticketsFiltrados, devueltoPorTicket));
            reporte.setTotalFiltradoNeto(calcularNeto(ticketsFiltrados, devueltoPorTicket));

            reporte.setCantidadDia(ticketsDia.size());
            reporte.setTotalDiaBruto(calcularBruto(ticketsDia));
            reporte.setTotalDiaNeto(calcularNeto(ticketsDia, devueltoPorTicket));

            reporte.setCantidadSemana(ticketsSemana.size());
            reporte.setTotalSemanaBruto(calcularBruto(ticketsSemana));
            reporte.setTotalSemanaNeto(calcularNeto(ticketsSemana, devueltoPorTicket));

            reporte.setCantidadMes(ticketsMes.size());
            reporte.setTotalMesBruto(calcularBruto(ticketsMes));
            reporte.setTotalMesNeto(calcularNeto(ticketsMes, devueltoPorTicket));

            return reporte;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private List<TicketVenta> filtrarPorPeriodo(List<TicketVenta> tickets, LocalDate inicio, LocalDate fin) {
        return tickets.stream()
                .filter(t -> coincideRango(t, inicio, fin))
                .collect(Collectors.toList());
    }

    private boolean enSemanaActual(TicketVenta ticket, LocalDate hoy, WeekFields weekFields,
                                   int anioActual, int semanaActual) {
        if (ticket.getFechaVenta() == null) {
            return false;
        }
        LocalDate fecha = ticket.getFechaVenta().toLocalDate();
        int semanaTicket = fecha.get(weekFields.weekOfWeekBasedYear());
        return fecha.getYear() == anioActual && semanaTicket == semanaActual;
    }

    private boolean enMesActual(TicketVenta ticket, int anioActual, int mesActual) {
        return ticket.getFechaVenta() != null
                && ticket.getFechaVenta().getYear() == anioActual
                && ticket.getFechaVenta().getMonthValue() == mesActual;
    }

    private int calcularBruto(List<TicketVenta> tickets) {
        return tickets.stream().mapToInt(TicketVenta::getTotal).sum();
    }

    private int calcularDevuelto(List<TicketVenta> tickets, Map<Long, Integer> devueltoPorTicket) {
        return tickets.stream()
                .mapToInt(t -> devueltoPorTicket.getOrDefault(t.getId(), 0))
                .sum();
    }

    private int calcularNeto(List<TicketVenta> tickets, Map<Long, Integer> devueltoPorTicket) {
        return tickets.stream()
                .mapToInt(t -> Math.max(0, t.getTotal() - devueltoPorTicket.getOrDefault(t.getId(), 0)))
                .sum();
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
