package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.CierreCajaDia;
import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.models.VentaPorVendedorResumen;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CierreCajaServiceImpl implements CierreCajaService {

    private static final int TOP_PRODUCTOS = 5;

    private final ReporteService reporteService;
    private final TicketRepository ticketRepository;

    public CierreCajaServiceImpl(ReporteService reporteService, TicketRepository ticketRepository) {
        this.reporteService = reporteService;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public CierreCajaDia generar(String tenantOwner, String username, boolean esAdmin, LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        String vendedorFiltro = esAdmin ? "" : username;
        try {
            ReporteVentas reporte = reporteService.generar(
                    tenantOwner, username, esAdmin, vendedorFiltro, dia, dia);

            LocalDate ayer = dia.minusDays(1);
            ReporteVentas reporteAyer = reporteService.generar(
                    tenantOwner, username, esAdmin, vendedorFiltro, ayer, ayer);

            LocalDateTime inicio = dia.atStartOfDay();
            LocalDateTime finExclusivo = dia.plusDays(1).atStartOfDay();

            CierreCajaDia cierre = new CierreCajaDia();
            cierre.setFecha(dia.toString());
            cierre.setEsAdmin(esAdmin);
            cierre.setVendedorFiltro(vendedorFiltro);
            cierre.setCantidadTickets(reporte.getCantidadFiltrada());
            cierre.setTotalBruto(reporte.getTotalFiltradoBruto());
            cierre.setTotalDevuelto(reporte.getTotalDevueltoFiltrado());
            cierre.setTotalNeto(reporte.getTotalFiltradoNeto());
            cierre.setCantidadTicketsAyer(reporteAyer.getCantidadFiltrada());
            cierre.setTotalNetoAyer(reporteAyer.getTotalFiltradoNeto());
            cierre.setTickets(reporte.getTicketsFiltrados());
            cierre.setDevueltoPorTicketId(reporte.getDevueltoPorTicketId());
            cierre.setTopProductos(
                    ticketRepository.topProductosVendidosPorTenant(
                            tenantOwner, inicio, finExclusivo, TOP_PRODUCTOS));
            if (esAdmin) {
                cierre.setVentasPorVendedor(agruparPorVendedor(reporte));
            }
            return cierre;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private List<VentaPorVendedorResumen> agruparPorVendedor(ReporteVentas reporte) {
        Map<String, VentaPorVendedorResumen> acumulado = new LinkedHashMap<>();
        for (TicketVenta ticket : reporte.getTicketsFiltrados()) {
            String vendedor = ticket.getUsernameVendedor() != null ? ticket.getUsernameVendedor() : "—";
            VentaPorVendedorResumen resumen = acumulado.computeIfAbsent(vendedor, v -> {
                VentaPorVendedorResumen nuevo = new VentaPorVendedorResumen();
                nuevo.setVendedor(v);
                return nuevo;
            });
            resumen.setCantidadTickets(resumen.getCantidadTickets() + 1);
            resumen.setTotalNeto(resumen.getTotalNeto() + reporte.totalNeto(ticket));
        }
        List<VentaPorVendedorResumen> lista = new ArrayList<>(acumulado.values());
        lista.sort(Comparator.comparingInt(VentaPorVendedorResumen::getTotalNeto).reversed());
        return lista;
    }
}
