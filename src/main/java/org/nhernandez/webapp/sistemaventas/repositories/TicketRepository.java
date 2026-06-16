package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.ProductoVentaRanking;
import org.nhernandez.webapp.sistemaventas.models.ResumenVentasVendedor;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository {

    void guardar(TicketVenta ticket) throws SQLException;

    List<TicketVenta> listarPorVendedor(String usernameVendedor) throws SQLException;

    List<TicketVenta> listarRecientesPorVendedor(String usernameVendedor, int limite) throws SQLException;

    ResumenVentasVendedor resumenPorVendedorEnPeriodo(String usernameVendedor,
                                                      LocalDateTime inicio,
                                                      LocalDateTime fin) throws SQLException;

    List<TicketVenta> listarPorTenant(String tenantOwner) throws SQLException;

    TicketVenta porFolioDeTenant(String folio, String tenantOwner) throws SQLException;

    TicketVenta porIdDeTenant(Long id, String tenantOwner) throws SQLException;

    void actualizarEstado(Long ticketId, String tenantOwner, String estado) throws SQLException;

    List<ProductoVentaRanking> topProductosVendidosPorTenant(String tenantOwner,
                                                           LocalDateTime inicio,
                                                           LocalDateTime finExclusivo,
                                                           int limite) throws SQLException;

    int contarActivosPorTenant(String tenantOwner) throws SQLException;
}
