package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PanelNegocioResumen;
import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PanelNegocioServiceImpl implements PanelNegocioService {

    private static final int TOP_PRODUCTOS = 3;
    private static final int DIAS_RANKING = 7;

    private final ReporteService reporteService;
    private final TicketRepository ticketRepository;

    public PanelNegocioServiceImpl(ReporteService reporteService, TicketRepository ticketRepository) {
        this.reporteService = reporteService;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public PanelNegocioResumen resumenParaAdmin(String tenantOwner, String usernameAdmin) {
        try {
            ReporteVentas reporte = reporteService.generar(
                    tenantOwner, usernameAdmin, true, "", null, null);

            PanelNegocioResumen panel = new PanelNegocioResumen();
            panel.setTicketsHoy(reporte.getCantidadDia());
            panel.setNetoHoy(reporte.getTotalDiaNeto());
            panel.setTicketsSemana(reporte.getCantidadSemana());
            panel.setNetoSemana(reporte.getTotalSemanaNeto());
            panel.setTicketsMes(reporte.getCantidadMes());
            panel.setNetoMes(reporte.getTotalMesNeto());

            LocalDate hoy = LocalDate.now();
            LocalDateTime inicioRanking = hoy.minusDays(DIAS_RANKING - 1L).atStartOfDay();
            LocalDateTime finRanking = hoy.plusDays(1).atStartOfDay();
            panel.setTopProductosSemana(
                    ticketRepository.topProductosVendidosPorTenant(
                            tenantOwner, inicioRanking, finRanking, TOP_PRODUCTOS));

            return panel;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }
}
