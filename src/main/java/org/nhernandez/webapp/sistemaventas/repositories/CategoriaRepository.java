package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.Categoria;

import java.sql.SQLException;
import java.util.List;

public interface CategoriaRepository extends CrudRepository<Categoria> {
    List<Categoria> listarPorOwner(String ownerUsername) throws SQLException;

    Categoria porIdPorOwner(Long id, String ownerUsername) throws SQLException;

    void eliminarPorOwner(Long id, String ownerUsername) throws SQLException;

    int contarProductosAsociados(Long categoriaId, String ownerUsername) throws SQLException;

    void crearSugeridasSiNoExisten(String ownerUsername, List<String> nombres) throws SQLException;
}
