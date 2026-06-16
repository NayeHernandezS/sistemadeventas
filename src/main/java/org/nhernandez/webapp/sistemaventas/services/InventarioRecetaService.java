package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ItemCarro;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.sql.SQLException;
import java.util.List;

public interface InventarioRecetaService {

    void validarStockCarrito(String tenantOwner, List<ItemCarro> items);

    void descontarPorTicket(TicketVenta ticket) throws SQLException;
}
