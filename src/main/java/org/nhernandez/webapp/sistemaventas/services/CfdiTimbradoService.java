package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

public interface CfdiTimbradoService {

    boolean disponible(String tenantOwner);

    /**
     * Intenta timbrar la factura. Actualiza estado en BD (TIMBRADO o ERROR).
     * No lanza excepcion al caller: la venta ya quedo registrada.
     */
    void intentarTimbrar(String tenantOwner, TicketVenta ticket, Factura factura);

    /**
     * Reintenta timbrar una factura que no quedo TIMBRADA. Actualiza datos en BD.
     *
     * @return mensaje para mostrar al usuario (exito o error de timbrado)
     */
    String reintentarTimbrar(String tenantOwner, TicketVenta ticket, Factura factura);

    byte[] descargarPdfTimbrado(String tenantOwner, Factura factura);

    byte[] descargarXmlTimbrado(String tenantOwner, Factura factura);

    boolean usaCredencialesTenant(String tenantOwner);
}
