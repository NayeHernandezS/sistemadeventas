package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.sql.SQLException;
import java.util.List;

public interface TicketRepository {

    void guardar(TicketVenta ticket) throws SQLException;

    List<TicketVenta> listarPorVendedor(String usernameVendedor) throws SQLException;

    List<TicketVenta> listarPorTenant(String tenantOwner) throws SQLException;

    TicketVenta porFolioDeTenant(String folio, String tenantOwner) throws SQLException;

    TicketVenta porIdDeTenant(Long id, String tenantOwner) throws SQLException;

    void actualizarEstado(Long ticketId, String tenantOwner, String estado) throws SQLException;
}
