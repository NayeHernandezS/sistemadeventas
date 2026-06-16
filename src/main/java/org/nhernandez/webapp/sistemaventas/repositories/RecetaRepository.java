package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.Receta;
import org.nhernandez.webapp.sistemaventas.models.RecetaLinea;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RecetaRepository {

    Optional<Receta> porProductoId(String tenantOwner, Long productoId) throws SQLException;

    List<RecetaLinea> listarLineasPorReceta(Long recetaId) throws SQLException;

    Long crearReceta(String tenantOwner, Long productoId) throws SQLException;

    void eliminarLineasPorReceta(Long recetaId) throws SQLException;

    void insertarLinea(Long recetaId, Long insumoProductoId, BigDecimal cantidad, String unidad) throws SQLException;

    void eliminarRecetaPorProducto(String tenantOwner, Long productoId) throws SQLException;
}
