package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.MovimientoInventario;

import java.sql.SQLException;
import java.util.List;

public interface MovimientoInventarioRepository {

    void insertar(MovimientoInventario movimiento) throws SQLException;

    List<MovimientoInventario> listarRecientesPorTenant(String tenantOwner, int limite) throws SQLException;
}
