package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoService {

    List<Producto> listarPorOwner(String ownerUsername);

    Optional<Producto> porIdPorOwner(Long id, String ownerUsername);

    Optional<Producto> porSkuPorOwner(String sku, String ownerUsername);

    void guardar(Producto producto);

    void eliminarPorOwner(Long id, String ownerUsername);

    List<Categoria> listarCategoria(String ownerUsername);
}
