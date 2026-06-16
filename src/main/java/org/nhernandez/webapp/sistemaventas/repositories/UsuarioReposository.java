package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.ClienteCuenta;
import org.nhernandez.webapp.sistemaventas.models.Usuario;

import java.sql.SQLException;
import java.util.List;

public interface UsuarioReposository {

    void guardar(Usuario usuario) throws SQLException;

    void eliminar(Long id) throws SQLException;

    Usuario porUsername(String username) throws SQLException;

    Usuario porEmail(String email) throws SQLException;

    boolean existeUsername(String username) throws SQLException;

    List<Usuario> listarPorAdminOwner(String adminOwner) throws SQLException;

    Usuario porIdDeTenant(Long id, String adminOwner) throws SQLException;

    List<ClienteCuenta> listarCuentasCliente() throws SQLException;

    void registrarUltimoAcceso(String username) throws SQLException;
}
