package org.nhernandez.webapp.sistemaventas.cfdi.facturama;

import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FacturamaCfdiRequestBuilder {

    private static final BigDecimal TASA_IVA = new BigDecimal("0.16");

    private FacturamaCfdiRequestBuilder() {
    }

    public static Map<String, Object> construir(DatosFiscalesNegocio emisor,
                                                Factura factura,
                                                TicketVenta ticket) {
        String cpReceptor = cpReceptor(factura, emisor);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("CfdiType", "I");
        body.put("PaymentForm", "01");
        body.put("PaymentMethod", "PUE");
        body.put("ExpeditionPlace", emisor.getCodigoPostal().trim());
        body.put("Exportation", "01");
        body.put("Folio", factura.getFolioFactura());
        body.put("Issuer", Map.of(
                "Rfc", emisor.getRfc().trim().toUpperCase(Locale.ROOT),
                "Name", emisor.getRazonSocial().trim(),
                "FiscalRegime", emisor.getRegimenFiscal().trim()
        ));
        Map<String, Object> receiver = new LinkedHashMap<>();
        receiver.put("Rfc", factura.getRfc().trim().toUpperCase(Locale.ROOT));
        receiver.put("Name", factura.getRazonSocial().trim());
        receiver.put("CfdiUse", usoCfdi(factura));
        receiver.put("FiscalRegime", regimenReceptor(factura.getRfc()));
        receiver.put("TaxZipCode", cpReceptor);
        body.put("Receiver", receiver);
        body.put("Items", itemsDesdeTicket(ticket));
        return body;
    }

    private static List<Map<String, Object>> itemsDesdeTicket(TicketVenta ticket) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (TicketItem linea : ticket.getItems()) {
            BigDecimal totalConIva = BigDecimal.valueOf(linea.getImporte());
            BigDecimal subtotal = totalConIva.divide(BigDecimal.ONE.add(TASA_IVA), 2, RoundingMode.HALF_UP);
            BigDecimal iva = totalConIva.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("Quantity", linea.getCantidad());
            item.put("ProductCode", "01010101");
            item.put("UnitCode", "H87");
            item.put("Unit", "Pieza");
            item.put("Description", linea.getNombreProducto());
            item.put("UnitPrice", subtotal.divide(BigDecimal.valueOf(linea.getCantidad()), 2, RoundingMode.HALF_UP));
            item.put("Subtotal", subtotal);
            item.put("TaxObject", "02");
            item.put("Taxes", List.of(Map.of(
                    "Name", "IVA",
                    "Rate", "0.16",
                    "Total", iva.toPlainString(),
                    "Base", subtotal.toPlainString(),
                    "IsRetention", "false",
                    "IsFederalTax", "true"
            )));
            item.put("Total", totalConIva);
            items.add(item);
        }
        return items;
    }

    static String regimenReceptor(String rfc) {
        if (rfc == null) {
            return "616";
        }
        String normalizado = rfc.trim().toUpperCase(Locale.ROOT);
        if ("XAXX010101000".equals(normalizado) || "XEXX010101000".equals(normalizado)) {
            return "616";
        }
        return normalizado.length() == 12 ? "601" : "612";
    }

    private static String usoCfdi(Factura factura) {
        if (factura.getUsoCfdi() != null && !factura.getUsoCfdi().isBlank()) {
            return factura.getUsoCfdi().trim().toUpperCase(Locale.ROOT);
        }
        return "G03";
    }

    private static String cpReceptor(Factura factura, DatosFiscalesNegocio emisor) {
        if (factura.getCodigoPostalReceptor() != null && !factura.getCodigoPostalReceptor().isBlank()) {
            return factura.getCodigoPostalReceptor().trim();
        }
        return emisor.getCodigoPostal().trim();
    }
}
