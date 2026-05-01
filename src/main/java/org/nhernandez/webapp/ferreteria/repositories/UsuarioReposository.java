package org.nhernandez.webapp.ferreteria.repositories;

import org.nhernandez.webapp.ferreteria.models.Usuario;

import java.sql.SQLException;

public interface UsuarioReposository extends CrudRepository<Usuario> {
    Usuario porUsername(String username) throws SQLException;
}
