package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.MovimientoInventario;
import org.nhernandez.webapp.sistemaventas.models.TipoMovimientoInventario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MovimientoInventarioRepositoryJdbcImpl implements MovimientoInventarioRepository {

    private final Connection conn;

    @Autowired
    public MovimientoInventarioRepositoryJdbcImpl(@MysqlConn Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insertar(MovimientoInventario movimiento) throws SQLException {
        String sql = """
                INSERT INTO movimientos_inventario
                    (tenant_owner, producto_id, tipo, cantidad, existencias_antes, existencias_despues, motivo, username)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, movimiento.getTenantOwner());
            stmt.setLong(2, movimiento.getProductoId());
            stmt.setString(3, movimiento.getTipo().name());
            stmt.setInt(4, movimiento.getCantidad());
            stmt.setInt(5, movimiento.getExistenciasAntes());
            stmt.setInt(6, movimiento.getExistenciasDespues());
            setNullableString(stmt, 7, movimiento.getMotivo());
            stmt.setString(8, movimiento.getUsername());
            stmt.executeUpdate();
        }
    }

    @Override
    public List<MovimientoInventario> listarRecientesPorTenant(String tenantOwner, int limite) throws SQLException {
        int max = Math.min(Math.max(limite, 1), 200);
        String sql = """
                SELECT m.id, m.tenant_owner, m.producto_id, m.tipo, m.cantidad,
                       m.existencias_antes, m.existencias_despues, m.motivo, m.username, m.fecha,
                       p.nombre AS nombre_producto
                FROM movimientos_inventario m
                INNER JOIN productos p ON p.id = m.producto_id AND p.owner_username = m.tenant_owner
                WHERE m.tenant_owner = ?
                ORDER BY m.fecha DESC, m.id DESC
                LIMIT ?
                """;
        List<MovimientoInventario> lista = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantOwner);
            stmt.setInt(2, max);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private static MovimientoInventario mapear(ResultSet rs) throws SQLException {
        MovimientoInventario m = new MovimientoInventario();
        m.setId(rs.getLong("id"));
        m.setTenantOwner(rs.getString("tenant_owner"));
        m.setProductoId(rs.getLong("producto_id"));
        m.setNombreProducto(rs.getString("nombre_producto"));
        TipoMovimientoInventario.porCodigo(rs.getString("tipo")).ifPresent(m::setTipo);
        m.setCantidad(rs.getInt("cantidad"));
        m.setExistenciasAntes(rs.getInt("existencias_antes"));
        m.setExistenciasDespues(rs.getInt("existencias_despues"));
        m.setMotivo(rs.getString("motivo"));
        m.setUsername(rs.getString("username"));
        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) {
            m.setFecha(ts.toLocalDateTime());
        }
        return m;
    }

    private static void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            stmt.setNull(index, java.sql.Types.VARCHAR);
        } else {
            stmt.setString(index, value.trim());
        }
    }
}
