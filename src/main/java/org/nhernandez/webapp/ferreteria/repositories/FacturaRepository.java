package org.nhernandez.webapp.ferreteria.repositories;

import org.nhernandez.webapp.ferreteria.models.Factura;

import java.sql.SQLException;

public interface FacturaRepository {
    void guardar(Factura factura) throws SQLException;

    Factura porTicketId(Long ticketId) throws SQLException;
}
