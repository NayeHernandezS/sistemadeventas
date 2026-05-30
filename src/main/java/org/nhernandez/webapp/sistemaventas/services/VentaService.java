package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.ItemCarro;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.util.List;

public interface VentaService {

    void validarStock(String tenantOwner, Long productoId, int cantidadRequerida);

    void validarStockCarrito(String tenantOwner, List<ItemCarro> items);

    TicketVenta registrarVenta(TicketVenta ticket, Factura facturaOpcional);
}
