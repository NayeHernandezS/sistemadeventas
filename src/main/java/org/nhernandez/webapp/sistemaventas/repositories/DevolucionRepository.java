package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.Devolucion;

import java.sql.SQLException;
import java.util.List;

public interface DevolucionRepository {

    void guardar(Devolucion devolucion) throws SQLException;

    List<Devolucion> listarPorTenant(String tenantOwner) throws SQLException;

    int cantidadDevueltaDeProducto(Long ticketId, Long productoId) throws SQLException;
}
