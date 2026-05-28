package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.nhernandez.webapp.sistemaventas.models.Usuario;

import java.sql.SQLException;
import java.util.List;

public interface UsuarioReposository extends CrudRepository<Usuario> {
    Usuario porUsername(String username) throws SQLException;

    boolean existeUsername(String username) throws SQLException;

    List<Usuario> listarPorAdminOwner(String adminOwner) throws SQLException;

    Usuario porIdDeTenant(Long id, String adminOwner) throws SQLException;
}
