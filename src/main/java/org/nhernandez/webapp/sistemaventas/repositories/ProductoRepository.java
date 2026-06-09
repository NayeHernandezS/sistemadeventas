package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.Producto;

import java.sql.SQLException;
import java.util.List;

public interface ProductoRepository {

    void guardar(Producto producto) throws SQLException;

    List<Producto> listarPorOwner(String ownerUsername) throws SQLException;

    List<Producto> listarServiciosPorOwner(String ownerUsername) throws SQLException;

    Producto porIdPorOwner(Long id, String ownerUsername) throws SQLException;

    Producto porSkuPorOwner(String sku, String ownerUsername) throws SQLException;

    void eliminarPorOwner(Long id, String ownerUsername) throws SQLException;

    void agregarExistencias(Long id, String ownerUsername, int cantidad) throws SQLException;

    void descontarExistencias(Long id, String ownerUsername, int cantidad) throws SQLException;

    void actualizarExistencias(Long id, String ownerUsername, int existencias) throws SQLException;

    int contarPorOwner(String ownerUsername) throws SQLException;

    boolean existeSkuPorOwner(String ownerUsername, String sku) throws SQLException;
}
