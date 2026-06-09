package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.repositories.CategoriaRepository;
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
    private CategoriaRepository repositoryCategoriaJdbc;

    @Autowired
    private PlanLimiteService planLimiteService;

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
    public Optional<Producto> porSkuPorOwner(String sku, String ownerUsername) {
        try {
            return Optional.ofNullable(repository.porSkuPorOwner(sku, ownerUsername));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void guardar(Producto producto) {
        if (producto.getId() == null || producto.getId() <= 0) {
            planLimiteService.validarNuevoProducto(producto.getOwnerUsername());
        }
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
    public List<Categoria> listarCategoria(String ownerUsername) {
        try {
            return repositoryCategoriaJdbc.listarPorOwner(ownerUsername);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }
}
