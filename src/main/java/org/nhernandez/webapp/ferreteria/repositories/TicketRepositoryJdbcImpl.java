package org.nhernandez.webapp.ferreteria.repositories;

import jakarta.inject.Inject;
import org.nhernandez.webapp.ferreteria.configs.MysqlConn;
import org.nhernandez.webapp.ferreteria.configs.Repository;
import org.nhernandez.webapp.ferreteria.models.TicketItem;
import org.nhernandez.webapp.ferreteria.models.TicketVenta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketRepositoryJdbcImpl implements TicketRepository {

    @Inject
    @MysqlConn
    private Connection conn;

    @Override
    public List<TicketVenta> listar() throws SQLException {
        String sql = "select id, folio, username_vendedor, fecha_venta, total from tickets_venta order by fecha_venta desc";
        List<TicketVenta> tickets = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TicketVenta ticket = mapTicket(rs);
                ticket.setItems(obtenerItemsPorTicket(ticket.getId()));
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    @Override
    public TicketVenta porId(Long id) throws SQLException {
        String sql = "select id, folio, username_vendedor, fecha_venta, total from tickets_venta where id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
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
    public void guardar(TicketVenta ticket) throws SQLException {
        boolean previousAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            if (ticket.getId() != null && ticket.getId() > 0) {
                actualizarTicket(ticket);
                eliminarItems(ticket.getId());
                insertarItems(ticket);
            } else {
                insertarTicket(ticket);
                insertarItems(ticket);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(previousAutoCommit);
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        boolean previousAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            eliminarItems(id);
            try (PreparedStatement stmt = conn.prepareStatement("delete from tickets_venta where id = ?")) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(previousAutoCommit);
        }
    }

    @Override
    public TicketVenta porFolio(String folio) throws SQLException {
        String sql = "select id, folio, username_vendedor, fecha_venta, total from tickets_venta where folio = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, folio);
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
    public List<TicketVenta> listarPorVendedor(String usernameVendedor) throws SQLException {
        String sql = "select id, folio, username_vendedor, fecha_venta, total from tickets_venta where username_vendedor = ? order by fecha_venta desc";
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
    public List<TicketVenta> listarPorRango(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = "select id, folio, username_vendedor, fecha_venta, total from tickets_venta where date(fecha_venta) between ? and ? order by fecha_venta desc";
        List<TicketVenta> tickets = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(fechaInicio));
            stmt.setDate(2, Date.valueOf(fechaFin));
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

    private void insertarTicket(TicketVenta ticket) throws SQLException {
        String sql = "insert into tickets_venta (folio, username_vendedor, fecha_venta, total) values (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ticket.getFolio());
            stmt.setString(2, ticket.getUsernameVendedor());
            if (ticket.getFechaVenta() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(ticket.getFechaVenta()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            stmt.setInt(4, ticket.getTotal());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ticket.setId(rs.getLong(1));
                }
            }
        }
    }

    private void actualizarTicket(TicketVenta ticket) throws SQLException {
        String sql = "update tickets_venta set folio = ?, username_vendedor = ?, fecha_venta = ?, total = ? where id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ticket.getFolio());
            stmt.setString(2, ticket.getUsernameVendedor());
            if (ticket.getFechaVenta() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(ticket.getFechaVenta()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            stmt.setInt(4, ticket.getTotal());
            stmt.setLong(5, ticket.getId());
            stmt.executeUpdate();
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

    private void eliminarItems(Long ticketId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("delete from ticket_items where ticket_id = ?")) {
            stmt.setLong(1, ticketId);
            stmt.executeUpdate();
        }
    }

    private TicketVenta mapTicket(ResultSet rs) throws SQLException {
        TicketVenta ticket = new TicketVenta();
        ticket.setId(rs.getLong("id"));
        ticket.setFolio(rs.getString("folio"));
        ticket.setUsernameVendedor(rs.getString("username_vendedor"));
        Timestamp timestamp = rs.getTimestamp("fecha_venta");
        ticket.setFechaVenta(timestamp != null ? timestamp.toLocalDateTime() : null);
        ticket.setTotal(rs.getInt("total"));
        return ticket;
    }
}
