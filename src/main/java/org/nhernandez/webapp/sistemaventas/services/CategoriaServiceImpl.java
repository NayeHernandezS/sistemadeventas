package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.repositories.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository repository;

    @Override
    public List<Categoria> listarPorOwner(String ownerUsername) {
        try {
            return repository.listarPorOwner(ownerUsername);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Categoria> porIdPorOwner(Long id, String ownerUsername) {
        try {
            return Optional.ofNullable(repository.porIdPorOwner(id, ownerUsername));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void guardar(Categoria categoria) {
        try {
            repository.guardar(categoria);
        } catch (SQLException e) {
            throw new ServiceJdbcException(mensajeAmigable(e), e);
        }
    }

    @Override
    public void eliminarPorOwner(Long id, String ownerUsername) {
        try {
            repository.eliminarPorOwner(id, ownerUsername);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private static String mensajeAmigable(SQLException e) {
        String msg = e.getMessage();
        if (msg != null && (msg.contains("uk_categorias_owner_nombre") || msg.contains("Duplicate entry"))) {
            return "Ya existe una categoria con ese nombre en tu cuenta";
        }
        return msg != null ? msg : "Error al guardar la categoria";
    }
}
