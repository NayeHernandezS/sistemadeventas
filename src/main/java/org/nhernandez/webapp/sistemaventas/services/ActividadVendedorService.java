package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ResumenVentasVendedor;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ActividadVendedorService {

    private static final int TICKETS_RECIENTES_DEFAULT = 5;
    private static final DateTimeFormatter MES_ANIO =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-MX"));

    private final TicketRepository ticketRepository;

    public ActividadVendedorService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public ResumenVentasVendedor resumenMesActual(String usernameVendedor) {
        YearMonth mes = YearMonth.now();
        LocalDateTime inicio = mes.atDay(1).atStartOfDay();
        LocalDateTime fin = mes.plusMonths(1).atDay(1).atStartOfDay();
        return resumenEnPeriodo(usernameVendedor, inicio, fin);
    }

    public ResumenVentasVendedor resumenEnPeriodo(String usernameVendedor,
                                                  LocalDateTime inicio,
                                                  LocalDateTime fin) {
        try {
            return ticketRepository.resumenPorVendedorEnPeriodo(usernameVendedor, inicio, fin);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    public List<TicketVenta> ticketsRecientes(String usernameVendedor, int limite) {
        try {
            return ticketRepository.listarRecientesPorVendedor(usernameVendedor, limite);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    public List<TicketVenta> ticketsRecientes(String usernameVendedor) {
        return ticketsRecientes(usernameVendedor, TICKETS_RECIENTES_DEFAULT);
    }

    public String etiquetaMesActual() {
        return YearMonth.now().atDay(1).format(MES_ANIO);
    }

    public DateTimeFormatter formatoTicket() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    }
}
