package org.nhernandez.webapp.sistemaventas.util;

import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacturaPdfExporterTest {

    private final FacturaPdfExporter exporter = new FacturaPdfExporter();

    @Test
    void exportar_generaPdfValido() {
        Factura factura = new Factura();
        factura.setFolioFactura("FAC-001");
        factura.setRfc("XAXX010101000");
        factura.setRazonSocial("Cliente Demo SA");
        factura.setEmail("cliente@ejemplo.com");
        factura.setFechaEmision(LocalDateTime.of(2026, 5, 29, 12, 0));

        TicketItem item = new TicketItem();
        item.setNombreProducto("Agua 600ml");
        item.setPrecioUnitario(15);
        item.setCantidad(2);
        item.setImporte(30);

        TicketVenta ticket = new TicketVenta();
        ticket.setFolio("TCK-001");
        ticket.setUsernameVendedor("vendedor1");
        ticket.setTotal(30);
        ticket.setItems(List.of(item));

        byte[] pdf = exporter.exportar(factura, ticket);

        assertTrue(pdf.length > 200);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
    }

    @Test
    void exportar_rechazaSinFactura() {
        assertThrows(Exception.class, () -> exporter.exportar(null, new TicketVenta()));
    }
}
