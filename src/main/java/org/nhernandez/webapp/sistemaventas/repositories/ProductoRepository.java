package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.nhernandez.webapp.sistemaventas.models.Producto;

import java.sql.SQLException;
import java.util.List;

public interface ProductoRepository extends CrudRepository<Producto> {

    List<Producto> listarPorOwner(String ownerUsername) throws SQLException;

    Producto porIdPorOwner(Long id, String ownerUsername) throws SQLException;

    void eliminarPorOwner(Long id, String ownerUsername) throws SQLException;

    void agregarExistencias(Long id, String ownerUsername, int cantidad) throws SQLException;

    void descontarExistencias(Long id, String ownerUsername, int cantidad) throws SQLException;

    int contarPorOwner(String ownerUsername) throws SQLException;
}
