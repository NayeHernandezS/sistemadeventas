package org.nhernandez.webapp.sistemaventas.cfdi.facturama;

import org.junit.jupiter.api.Test;
import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacturamaCfdiRequestBuilderTest {

    @Test
    void construir_incluyeEmisorReceptorEItems() {
        DatosFiscalesNegocio emisor = new DatosFiscalesNegocio();
        emisor.setRfc("EKU9003173C9");
        emisor.setRazonSocial("Empresa Demo SA");
        emisor.setCodigoPostal("26015");
        emisor.setRegimenFiscal("601");

        Factura factura = new Factura();
        factura.setFolioFactura("FAC-001");
        factura.setRfc("XAXX010101000");
        factura.setRazonSocial("Publico General");
        factura.setUsoCfdi("G03");
        factura.setCodigoPostalReceptor("64000");

        TicketItem item = new TicketItem();
        item.setNombreProducto("Producto A");
        item.setCantidad(2);
        item.setPrecioUnitario(10);
        item.setImporte(20);
        TicketVenta ticket = new TicketVenta();
        ticket.setItems(List.of(item));
        ticket.setTotal(20);

        Map<String, Object> body = FacturamaCfdiRequestBuilder.construir(emisor, factura, ticket);

        assertEquals("I", body.get("CfdiType"));
        assertEquals("26015", body.get("ExpeditionPlace"));
        @SuppressWarnings("unchecked")
        Map<String, Object> receiver = (Map<String, Object>) body.get("Receiver");
        assertEquals("616", receiver.get("FiscalRegime"));
        assertEquals("64000", receiver.get("TaxZipCode"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("Items");
        assertEquals(1, items.size());
        assertEquals("Producto A", items.getFirst().get("Description"));
    }

    @Test
    void regimenReceptor_moralUsa601() {
        assertEquals("601", FacturamaCfdiRequestBuilder.regimenReceptor("URE180429TM6"));
    }

    @Test
    void regimenReceptor_fisicaUsa612() {
        assertEquals("612", FacturamaCfdiRequestBuilder.regimenReceptor("FUGM8606085HA"));
    }

    @Test
    void regimenReceptor_publicoGeneral616() {
        assertEquals("616", FacturamaCfdiRequestBuilder.regimenReceptor("XAXX010101000"));
    }
}
