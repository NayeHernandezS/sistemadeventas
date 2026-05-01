package org.nhernandez.webapp.ferreteria.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.nhernandez.webapp.ferreteria.models.Usuario;
import org.nhernandez.webapp.ferreteria.repositories.UsuarioRepositoryImp;
import org.nhernandez.webapp.ferreteria.repositories.UsuarioReposository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService{

    private UsuarioReposository usuarioReposository;


    @Inject
    public UsuarioServiceImpl(UsuarioReposository usuarioReposository) {
        this.usuarioReposository = usuarioReposository;
    }

    @Override
    public Optional<Usuario> login(String username, String password) {
        try {
            return Optional.ofNullable(usuarioReposository.porUsername(username)).filter(u -> u.getPassword().equals(password));
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables.getMessage(), throwables.getCause());
        }
    }

    @Override
    public List<Usuario> listar() {
        try {
            return usuarioReposository.listar();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Optional<Usuario> porId(Long id) {
        try {
            return Optional.ofNullable(usuarioReposository.porId(id));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void guardar(Usuario usuario) {
        try {
            usuarioReposository.guardar(usuario);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void eliminar(Long id) {
        try {
            usuarioReposository.eliminar(id);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }
}
