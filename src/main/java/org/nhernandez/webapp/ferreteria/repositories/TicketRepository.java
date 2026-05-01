package org.nhernandez.webapp.ferreteria.repositories;

import org.nhernandez.webapp.ferreteria.models.TicketVenta;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface TicketRepository extends CrudRepository<TicketVenta> {
    TicketVenta porFolio(String folio) throws SQLException;
    List<TicketVenta> listarPorVendedor(String usernameVendedor) throws SQLException;
    List<TicketVenta> listarPorRango(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException;
}
