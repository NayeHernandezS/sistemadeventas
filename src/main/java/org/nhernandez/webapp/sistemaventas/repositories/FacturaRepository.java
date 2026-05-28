package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.nhernandez.webapp.sistemaventas.models.Factura;

import java.sql.SQLException;

public interface FacturaRepository {
    void guardar(Factura factura) throws SQLException;

    Factura porTicketId(Long ticketId) throws SQLException;
}
