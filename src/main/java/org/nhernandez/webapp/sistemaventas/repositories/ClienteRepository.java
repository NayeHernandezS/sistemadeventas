package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.Cliente;

import java.sql.SQLException;
import java.util.List;

public interface ClienteRepository {

    List<Cliente> listarActivosPorTenant(String tenantOwner) throws SQLException;

    Cliente porIdPorTenant(Long id, String tenantOwner) throws SQLException;

    void guardar(Cliente cliente) throws SQLException;

    void desactivarPorTenant(Long id, String tenantOwner) throws SQLException;
}
