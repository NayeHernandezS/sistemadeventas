package org.nhernandez.webapp.ferreteria.services;

import org.nhernandez.webapp.ferreteria.models.Categoria;
import org.nhernandez.webapp.ferreteria.models.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<Producto> listar();

    Optional<Producto> porId(Long id);

    void guardar(Producto producto);

    void eliminar(Long id);

    List<Categoria> listarCategoria();

    Optional<Categoria> porIdCategoria(Long id);
}
