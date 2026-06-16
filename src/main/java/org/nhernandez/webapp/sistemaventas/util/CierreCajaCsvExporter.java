package org.nhernandez.webapp.sistemaventas.util;

import org.nhernandez.webapp.sistemaventas.models.CierreCajaDia;
import org.nhernandez.webapp.sistemaventas.models.ProductoVentaRanking;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.models.VentaPorVendedorResumen;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CierreCajaCsvExporter {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public byte[] exportar(CierreCajaDia cierre) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cierre de caja\n");
        sb.append("Fecha,").append(ReporteCsvExporter.escapar(cierre.getFecha())).append('\n');
        sb.append("Tickets,").append(cierre.getCantidadTickets()).append('\n');
        sb.append("Total bruto,").append(cierre.getTotalBruto()).append('\n');
        sb.append("Total devuelto,").append(cierre.getTotalDevuelto()).append('\n');
        sb.append("Total neto,").append(cierre.getTotalNeto()).append('\n');
        sb.append("Ayer tickets,").append(cierre.getCantidadTicketsAyer()).append('\n');
        sb.append("Ayer neto,").append(cierre.getTotalNetoAyer()).append('\n');
        sb.append('\n');

        if (cierre.isEsAdmin() && !cierre.getVentasPorVendedor().isEmpty()) {
            sb.append("Vendedor,Cantidad tickets,Neto\n");
            for (VentaPorVendedorResumen v : cierre.getVentasPorVendedor()) {
                sb.append(ReporteCsvExporter.escapar(v.getVendedor())).append(',');
                sb.append(v.getCantidadTickets()).append(',');
                sb.append(v.getTotalNeto()).append('\n');
            }
            sb.append('\n');
        }

        if (!cierre.getTopProductos().isEmpty()) {
            sb.append("Top productos,Unidades,Importe\n");
            for (ProductoVentaRanking p : cierre.getTopProductos()) {
                sb.append(ReporteCsvExporter.escapar(p.getNombreProducto())).append(',');
                sb.append(p.getUnidadesVendidas()).append(',');
                sb.append(p.getImporteTotal()).append('\n');
            }
            sb.append('\n');
        }

        sb.append("Folio,Vendedor,Fecha,Estado,Bruto,Devuelto,Neto\n");
        List<TicketVenta> tickets = cierre.getTickets();
        for (TicketVenta ticket : tickets) {
            sb.append(ReporteCsvExporter.escapar(ticket.getFolio())).append(',');
            sb.append(ReporteCsvExporter.escapar(ticket.getUsernameVendedor())).append(',');
            sb.append(ReporteCsvExporter.escapar(formatearFecha(ticket))).append(',');
            sb.append(ReporteCsvExporter.escapar(estado(ticket))).append(',');
            sb.append(ticket.getTotal()).append(',');
            sb.append(cierre.totalDevuelto(ticket)).append(',');
            sb.append(cierre.totalNeto(ticket)).append('\n');
        }

        byte[] contenido = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] conBom = new byte[UTF8_BOM.length + contenido.length];
        System.arraycopy(UTF8_BOM, 0, conBom, 0, UTF8_BOM.length);
        System.arraycopy(contenido, 0, conBom, UTF8_BOM.length, contenido.length);
        return conBom;
    }

    private static String formatearFecha(TicketVenta ticket) {
        if (ticket.getFechaVenta() == null) {
            return "";
        }
        return ticket.getFechaVenta().format(FECHA);
    }

    private static String estado(TicketVenta ticket) {
        String estado = ticket.getEstado();
        return estado != null && !estado.isBlank() ? estado : "ACTIVO";
    }
}
