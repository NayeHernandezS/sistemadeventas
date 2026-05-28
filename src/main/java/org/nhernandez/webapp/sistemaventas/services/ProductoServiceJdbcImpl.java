package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.repositories.CrudRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@ProductoServicePrincipal
public class ProductoServiceJdbcImpl implements ProductoService {

    @Autowired
    private ProductoRepository repository;

    @Autowired
    private CrudRepository<Categoria> repositoryCategoriaJdbc;

    @Override
    public List<Producto> listarPorOwner(String ownerUsername) {
        try {
            return repository.listarPorOwner(ownerUsername);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Optional<Producto> porIdPorOwner(Long id, String ownerUsername) {
        try {
            return Optional.ofNullable(repository.porIdPorOwner(id, ownerUsername));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void guardar(Producto producto) {
        try {
            repository.guardar(producto);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void eliminarPorOwner(Long id, String ownerUsername) {
        try {
            repository.eliminarPorOwner(id, ownerUsername);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public List<Categoria> listarCategoria() {
        try {
            return repositoryCategoriaJdbc.listar();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }
}
