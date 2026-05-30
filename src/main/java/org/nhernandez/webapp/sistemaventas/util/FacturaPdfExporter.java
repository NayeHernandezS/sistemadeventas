package org.nhernandez.webapp.sistemaventas.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
public class FacturaPdfExporter {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportar(Factura factura, TicketVenta ticket) {
        if (factura == null || ticket == null) {
            throw new ServiceJdbcException("No hay factura registrada para este ticket", null);
        }
        Document document = new Document(PageSize.LETTER, 40, 40, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font pequeno = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);

            document.add(new Paragraph("Factura (comprobante de venta)", titulo));
            document.add(new Paragraph(" ", normal));

            document.add(linea("Folio factura: ", factura.getFolioFactura(), subtitulo, normal));
            document.add(linea("Fecha emision: ", formatear(factura.getFechaEmision()), subtitulo, normal));
            document.add(linea("Ticket venta: ", ticket.getFolio(), subtitulo, normal));
            document.add(linea("Vendedor: ", ticket.getUsernameVendedor(), subtitulo, normal));
            document.add(new Paragraph(" ", normal));

            document.add(new Paragraph("Datos del cliente (fiscal)", subtitulo));
            document.add(linea("RFC: ", factura.getRfc(), subtitulo, normal));
            document.add(linea("Razon social: ", factura.getRazonSocial(), subtitulo, normal));
            agregarSiHay(document, "Correo: ", factura.getEmail(), subtitulo, normal);
            agregarSiHay(document, "Direccion: ", factura.getDireccion(), subtitulo, normal);
            agregarSiHay(document, "Uso CFDI: ", factura.getUsoCfdi(), subtitulo, normal);
            document.add(new Paragraph(" ", normal));

            document.add(new Paragraph("Detalle", subtitulo));
            document.add(tablaItems(ticket, subtitulo, normal));
            document.add(new Paragraph(" ", normal));
            document.add(new Paragraph(
                    "Documento informativo generado por el sistema. No sustituye un CFDI ante el SAT.",
                    pequeno));

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new ServiceJdbcException("No se pudo generar el PDF de la factura", e);
        }
    }

    private PdfPTable tablaItems(TicketVenta ticket, Font headerFont, Font cellFont) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{4f, 1.2f, 1f, 1.2f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(6f);

        agregarCeldaHeader(table, "Producto", headerFont);
        agregarCeldaHeader(table, "Precio", headerFont);
        agregarCeldaHeader(table, "Cant.", headerFont);
        agregarCeldaHeader(table, "Importe", headerFont);

        if (ticket.getItems() != null) {
            for (TicketItem item : ticket.getItems()) {
                agregarCelda(table, item.getNombreProducto(), cellFont, Element.ALIGN_LEFT);
                agregarCelda(table, "$" + item.getPrecioUnitario(), cellFont, Element.ALIGN_RIGHT);
                agregarCelda(table, String.valueOf(item.getCantidad()), cellFont, Element.ALIGN_RIGHT);
                agregarCelda(table, "$" + item.getImporte(), cellFont, Element.ALIGN_RIGHT);
            }
        }

        PdfPCell totalLabel = new PdfPCell(new Phrase("Total", headerFont));
        totalLabel.setColspan(3);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setBorder(PdfPCell.NO_BORDER);
        table.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase("$" + ticket.getTotal(), headerFont));
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalValue);

        return table;
    }

    private void agregarCeldaHeader(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void agregarCelda(PdfPTable table, String texto, Font font, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "", font));
        cell.setHorizontalAlignment(alineacion);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private Paragraph linea(String etiqueta, String valor, Font etiquetaFont, Font valorFont) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(etiqueta, etiquetaFont));
        p.add(new Phrase(valor != null ? valor : "—", valorFont));
        return p;
    }

    private void agregarSiHay(Document document, String etiqueta, String valor, Font etiquetaFont, Font valorFont)
            throws DocumentException {
        if (valor != null && !valor.isBlank()) {
            document.add(linea(etiqueta, valor.trim(), etiquetaFont, valorFont));
        }
    }

    private String formatear(java.time.LocalDateTime fecha) {
        return fecha != null ? fecha.format(FECHA) : "—";
    }
}
