package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaService {

    List<Categoria> listarPorOwner(String ownerUsername);

    Optional<Categoria> porIdPorOwner(Long id, String ownerUsername);

    void guardar(Categoria categoria);

    void eliminarPorOwner(Long id, String ownerUsername);

    void asegurarCategoriasPlantilla(String ownerUsername, String tipoNegocio);
}
