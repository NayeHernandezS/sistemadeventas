package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.Receta;
import org.nhernandez.webapp.sistemaventas.models.RecetaLinea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RecetaRepositoryImpl implements RecetaRepository {

    private final Connection conn;

    @Autowired
    public RecetaRepositoryImpl(@MysqlConn Connection conn) {
        this.conn = conn;
    }

    @Override
    public Optional<Receta> porProductoId(String tenantOwner, Long productoId) throws SQLException {
        String sql = "SELECT id, tenant_owner, producto_id FROM recetas "
                + "WHERE tenant_owner = ? AND producto_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantOwner);
            stmt.setLong(2, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearReceta(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<RecetaLinea> listarLineasPorReceta(Long recetaId) throws SQLException {
        List<RecetaLinea> lineas = new ArrayList<>();
        String sql = """
                SELECT rl.id, rl.receta_id, rl.insumo_producto_id, rl.cantidad, rl.unidad, p.nombre AS insumo_nombre
                FROM receta_lineas rl
                INNER JOIN productos p ON p.id = rl.insumo_producto_id
                WHERE rl.receta_id = ?
                ORDER BY rl.id ASC
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, recetaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecetaLinea linea = new RecetaLinea();
                    linea.setId(rs.getLong("id"));
                    linea.setRecetaId(rs.getLong("receta_id"));
                    linea.setInsumoProductoId(rs.getLong("insumo_producto_id"));
                    linea.setCantidad(rs.getBigDecimal("cantidad"));
                    linea.setUnidad(rs.getString("unidad"));
                    linea.setInsumoNombre(rs.getString("insumo_nombre"));
                    lineas.add(linea);
                }
            }
        }
        return lineas;
    }

    @Override
    public Long crearReceta(String tenantOwner, Long productoId) throws SQLException {
        String sql = "INSERT INTO recetas (tenant_owner, producto_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tenantOwner);
            stmt.setLong(2, productoId);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo crear la receta");
    }

    @Override
    public void eliminarLineasPorReceta(Long recetaId) throws SQLException {
        String sql = "DELETE FROM receta_lineas WHERE receta_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, recetaId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void insertarLinea(Long recetaId, Long insumoProductoId, BigDecimal cantidad, String unidad)
            throws SQLException {
        String sql = "INSERT INTO receta_lineas (receta_id, insumo_producto_id, cantidad, unidad) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, recetaId);
            stmt.setLong(2, insumoProductoId);
            stmt.setBigDecimal(3, cantidad);
            stmt.setString(4, unidad);
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminarRecetaPorProducto(String tenantOwner, Long productoId) throws SQLException {
        Optional<Receta> receta = porProductoId(tenantOwner, productoId);
        if (receta.isEmpty()) {
            return;
        }
        eliminarLineasPorReceta(receta.get().getId());
        String sql = "DELETE FROM recetas WHERE id = ? AND tenant_owner = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, receta.get().getId());
            stmt.setString(2, tenantOwner);
            stmt.executeUpdate();
        }
    }

    private static Receta mapearReceta(ResultSet rs) throws SQLException {
        Receta receta = new Receta();
        receta.setId(rs.getLong("id"));
        receta.setTenantOwner(rs.getString("tenant_owner"));
        receta.setProductoId(rs.getLong("producto_id"));
        return receta;
    }
}
