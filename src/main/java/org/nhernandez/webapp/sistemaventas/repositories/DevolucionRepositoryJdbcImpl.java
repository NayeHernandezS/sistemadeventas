package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.Devolucion;
import org.nhernandez.webapp.sistemaventas.models.DevolucionItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DevolucionRepositoryJdbcImpl implements DevolucionRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public void guardar(Devolucion devolucion) throws SQLException {
        boolean previousAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            String sql = "INSERT INTO devoluciones (folio, ticket_id, ticket_folio, tenant_owner, "
                    + "username_registro, fecha_devolucion, motivo, total_devuelto) "
                    + "VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, devolucion.getFolio());
                stmt.setLong(2, devolucion.getTicketId());
                stmt.setString(3, devolucion.getTicketFolio());
                stmt.setString(4, devolucion.getTenantOwner());
                stmt.setString(5, devolucion.getUsernameRegistro());
                stmt.setTimestamp(6, Timestamp.valueOf(devolucion.getFechaDevolucion()));
                stmt.setString(7, devolucion.getMotivo());
                stmt.setInt(8, devolucion.getTotalDevuelto());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        devolucion.setId(rs.getLong(1));
                    }
                }
            }
            insertarItems(devolucion);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(previousAutoCommit);
        }
    }

    @Override
    public List<Devolucion> listarPorTenant(String tenantOwner) throws SQLException {
        String sql = "SELECT id, folio, ticket_id, ticket_folio, tenant_owner, username_registro, "
                + "fecha_devolucion, motivo, total_devuelto FROM devoluciones "
                + "WHERE tenant_owner = ? ORDER BY fecha_devolucion DESC";
        List<Devolucion> lista = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Devolucion d = mapDevolucion(rs);
                    d.setItems(obtenerItems(d.getId()));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    @Override
    public int cantidadDevueltaDeProducto(Long ticketId, Long productoId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cantidad), 0) AS total FROM devolucion_items "
                + "WHERE ticket_id = ? AND producto_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ticketId);
            stmt.setLong(2, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private void insertarItems(Devolucion devolucion) throws SQLException {
        String sql = "INSERT INTO devolucion_items (devolucion_id, ticket_id, producto_id, nombre_producto, "
                + "cantidad, precio_unitario, importe) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (DevolucionItem item : devolucion.getItems()) {
                stmt.setLong(1, devolucion.getId());
                stmt.setLong(2, devolucion.getTicketId());
                stmt.setLong(3, item.getProductoId());
                stmt.setString(4, item.getNombreProducto());
                stmt.setInt(5, item.getCantidad());
                stmt.setInt(6, item.getPrecioUnitario());
                stmt.setInt(7, item.getImporte());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private List<DevolucionItem> obtenerItems(Long devolucionId) throws SQLException {
        String sql = "SELECT producto_id, nombre_producto, cantidad, precio_unitario, importe "
                + "FROM devolucion_items WHERE devolucion_id = ?";
        List<DevolucionItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, devolucionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DevolucionItem item = new DevolucionItem();
                    item.setProductoId(rs.getLong("producto_id"));
                    item.setNombreProducto(rs.getString("nombre_producto"));
                    item.setCantidad(rs.getInt("cantidad"));
                    item.setPrecioUnitario(rs.getInt("precio_unitario"));
                    item.setImporte(rs.getInt("importe"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Devolucion mapDevolucion(ResultSet rs) throws SQLException {
        Devolucion d = new Devolucion();
        d.setId(rs.getLong("id"));
        d.setFolio(rs.getString("folio"));
        d.setTicketId(rs.getLong("ticket_id"));
        d.setTicketFolio(rs.getString("ticket_folio"));
        d.setTenantOwner(rs.getString("tenant_owner"));
        d.setUsernameRegistro(rs.getString("username_registro"));
        d.setFechaDevolucion(rs.getTimestamp("fecha_devolucion").toLocalDateTime());
        d.setMotivo(rs.getString("motivo"));
        d.setTotalDevuelto(rs.getInt("total_devuelto"));
        return d;
    }
}
