package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketRepositoryJdbcImpl implements TicketRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public void guardar(TicketVenta ticket) throws SQLException {
        boolean previousAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            descontarInventario(ticket);
            insertarTicket(ticket);
            insertarItems(ticket);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(previousAutoCommit);
        }
    }

    @Override
    public List<TicketVenta> listarPorVendedor(String usernameVendedor) throws SQLException {
        String sql = "select id, folio, username_vendedor, tenant_owner, fecha_venta, total, estado from tickets_venta where username_vendedor = ? order by fecha_venta desc";
        List<TicketVenta> tickets = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usernameVendedor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TicketVenta ticket = mapTicket(rs);
                    ticket.setItems(obtenerItemsPorTicket(ticket.getId()));
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    @Override
    public List<TicketVenta> listarPorTenant(String tenantOwner) throws SQLException {
        String sql = "select id, folio, username_vendedor, tenant_owner, fecha_venta, total, estado from tickets_venta "
                + "where tenant_owner = ? order by fecha_venta desc";
        List<TicketVenta> tickets = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TicketVenta ticket = mapTicket(rs);
                    ticket.setItems(obtenerItemsPorTicket(ticket.getId()));
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    @Override
    public TicketVenta porFolioDeTenant(String folio, String tenantOwner) throws SQLException {
        String sql = "select id, folio, username_vendedor, tenant_owner, fecha_venta, total, estado from tickets_venta "
                + "where folio = ? and tenant_owner = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, folio);
            stmt.setString(2, tenantOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TicketVenta ticket = mapTicket(rs);
                    ticket.setItems(obtenerItemsPorTicket(ticket.getId()));
                    return ticket;
                }
            }
        }
        return null;
    }

    @Override
    public TicketVenta porIdDeTenant(Long id, String tenantOwner) throws SQLException {
        String sql = "select id, folio, username_vendedor, tenant_owner, fecha_venta, total, estado from tickets_venta "
                + "where id = ? and tenant_owner = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, tenantOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TicketVenta ticket = mapTicket(rs);
                    ticket.setItems(obtenerItemsPorTicket(ticket.getId()));
                    return ticket;
                }
            }
        }
        return null;
    }

    @Override
    public void actualizarEstado(Long ticketId, String tenantOwner, String estado) throws SQLException {
        String sql = "UPDATE tickets_venta SET estado = ? WHERE id = ? AND tenant_owner = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setLong(2, ticketId);
            stmt.setString(3, tenantOwner);
            stmt.executeUpdate();
        }
    }

    private void descontarInventario(TicketVenta ticket) throws SQLException {
        if (ticket.getItems() == null || ticket.getItems().isEmpty()) {
            return;
        }
        String tenantOwner = ticket.getTenantOwner();
        if (tenantOwner == null || tenantOwner.isBlank()) {
            throw new SQLException("tenant_owner es obligatorio para descontar inventario");
        }
        for (TicketItem item : ticket.getItems()) {
            productoRepository.descontarExistencias(
                    item.getProductoId(), tenantOwner, item.getCantidad());
        }
    }

    private void insertarTicket(TicketVenta ticket) throws SQLException {
        String sql = "insert into tickets_venta (folio, username_vendedor, tenant_owner, fecha_venta, total, estado) "
                + "values (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ticket.getFolio());
            stmt.setString(2, ticket.getUsernameVendedor());
            stmt.setString(3, ticket.getTenantOwner());
            if (ticket.getFechaVenta() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(ticket.getFechaVenta()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            stmt.setInt(5, ticket.getTotal());
            stmt.setString(6, ticket.getEstado() != null ? ticket.getEstado() : "ACTIVO");
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ticket.setId(rs.getLong(1));
                }
            }
        }
    }

    private void insertarItems(TicketVenta ticket) throws SQLException {
        if (ticket.getItems() == null || ticket.getItems().isEmpty()) {
            return;
        }

        String sql = "insert into ticket_items (ticket_id, producto_id, nombre_producto, precio_unitario, cantidad, importe) values (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (TicketItem item : ticket.getItems()) {
                stmt.setLong(1, ticket.getId());
                stmt.setLong(2, item.getProductoId());
                stmt.setString(3, item.getNombreProducto());
                stmt.setInt(4, item.getPrecioUnitario());
                stmt.setInt(5, item.getCantidad());
                stmt.setInt(6, item.getImporte());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private List<TicketItem> obtenerItemsPorTicket(Long ticketId) throws SQLException {
        String sql = "select producto_id, nombre_producto, precio_unitario, cantidad, importe from ticket_items where ticket_id = ?";
        List<TicketItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TicketItem item = new TicketItem();
                    item.setProductoId(rs.getLong("producto_id"));
                    item.setNombreProducto(rs.getString("nombre_producto"));
                    item.setPrecioUnitario(rs.getInt("precio_unitario"));
                    item.setCantidad(rs.getInt("cantidad"));
                    item.setImporte(rs.getInt("importe"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private TicketVenta mapTicket(ResultSet rs) throws SQLException {
        TicketVenta ticket = new TicketVenta();
        ticket.setId(rs.getLong("id"));
        ticket.setFolio(rs.getString("folio"));
        ticket.setUsernameVendedor(rs.getString("username_vendedor"));
        ticket.setTenantOwner(rs.getString("tenant_owner"));
        Timestamp timestamp = rs.getTimestamp("fecha_venta");
        ticket.setFechaVenta(timestamp != null ? timestamp.toLocalDateTime() : null);
        ticket.setTotal(rs.getInt("total"));
        try {
            ticket.setEstado(rs.getString("estado"));
        } catch (SQLException ignored) {
            ticket.setEstado("ACTIVO");
        }
        if (ticket.getEstado() == null || ticket.getEstado().isBlank()) {
            ticket.setEstado("ACTIVO");
        }
        return ticket;
    }
}
