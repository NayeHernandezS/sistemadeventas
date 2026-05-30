package org.nhernandez.webapp.sistemaventas.util;

import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReporteCsvExporter {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public byte[] exportar(ReporteVentas reporte) {
        StringBuilder sb = new StringBuilder();
        sb.append("Resumen del reporte\n");
        sb.append("Tickets,").append(reporte.getCantidadFiltrada()).append('\n');
        sb.append("Total bruto,").append(reporte.getTotalFiltradoBruto()).append('\n');
        sb.append("Total devuelto,").append(reporte.getTotalDevueltoFiltrado()).append('\n');
        sb.append("Total neto,").append(reporte.getTotalFiltradoNeto()).append('\n');
        if (reporte.getFechaInicio() != null && !reporte.getFechaInicio().isBlank()) {
            sb.append("Fecha inicio,").append(escapar(reporte.getFechaInicio())).append('\n');
        }
        if (reporte.getFechaFin() != null && !reporte.getFechaFin().isBlank()) {
            sb.append("Fecha fin,").append(escapar(reporte.getFechaFin())).append('\n');
        }
        if (reporte.getVendedorSeleccionado() != null && !reporte.getVendedorSeleccionado().isBlank()) {
            sb.append("Vendedor,").append(escapar(reporte.getVendedorSeleccionado())).append('\n');
        }
        sb.append('\n');
        sb.append("Folio,Vendedor,Fecha,Estado,Bruto,Devuelto,Neto\n");

        List<TicketVenta> tickets = reporte.getTicketsFiltrados();
        if (tickets != null) {
            for (TicketVenta ticket : tickets) {
                sb.append(escapar(ticket.getFolio())).append(',');
                sb.append(escapar(ticket.getUsernameVendedor())).append(',');
                sb.append(escapar(formatearFecha(ticket))).append(',');
                sb.append(escapar(estado(ticket))).append(',');
                sb.append(ticket.getTotal()).append(',');
                sb.append(reporte.totalDevuelto(ticket)).append(',');
                sb.append(reporte.totalNeto(ticket)).append('\n');
            }
        }

        byte[] contenido = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] conBom = new byte[UTF8_BOM.length + contenido.length];
        System.arraycopy(UTF8_BOM, 0, conBom, 0, UTF8_BOM.length);
        System.arraycopy(contenido, 0, conBom, UTF8_BOM.length, contenido.length);
        return conBom;
    }

    private String formatearFecha(TicketVenta ticket) {
        if (ticket.getFechaVenta() == null) {
            return "";
        }
        return ticket.getFechaVenta().format(FECHA);
    }

    private String estado(TicketVenta ticket) {
        String estado = ticket.getEstado();
        return estado != null && !estado.isBlank() ? estado : "ACTIVO";
    }

    static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
