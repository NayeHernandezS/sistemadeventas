package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

public interface CfdiTimbradoService {

    boolean disponible();

    /**
     * Intenta timbrar la factura. Actualiza estado en BD (TIMBRADO o ERROR).
     * No lanza excepcion al caller: la venta ya quedo registrada.
     */
    void intentarTimbrar(String tenantOwner, TicketVenta ticket, Factura factura);

    byte[] descargarPdfTimbrado(Factura factura);

    byte[] descargarXmlTimbrado(Factura factura);
}
