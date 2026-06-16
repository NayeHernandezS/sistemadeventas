package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.ProductoVentaRanking;
import org.nhernandez.webapp.sistemaventas.models.ResumenVentasVendedor;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.services.InventarioRecetaService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketRepositoryJdbcImpl implements TicketRepository {

    private static final String COLUMNAS_TICKET =
            "id, folio, username_vendedor, tenant_owner, fecha_venta, total, nombre_cliente, estado";

    @Autowired
    @MysqlConn
    private Connection conn;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRecetaService inventarioRecetaService;

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
        String sql = "select " + COLUMNAS_TICKET + " from tickets_venta where username_vendedor = ? order by fecha_venta desc";
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
    public List<TicketVenta> listarRecientesPorVendedor(String usernameVendedor, int limite) throws SQLException {
        int max = limite > 0 ? limite : 5;
        String sql = """
                SELECT %s
                FROM tickets_venta
                WHERE username_vendedor = ?
                ORDER BY fecha_venta DESC
                LIMIT ?
                """.formatted(COLUMNAS_TICKET);
        List<TicketVenta> tickets = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usernameVendedor);
            stmt.setInt(2, max);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        }
        return tickets;
    }

    @Override
    public ResumenVentasVendedor resumenPorVendedorEnPeriodo(String usernameVendedor,
                                                             LocalDateTime inicio,
                                                             LocalDateTime fin) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cantidad, COALESCE(SUM(total), 0) AS total_importe
                FROM tickets_venta
                WHERE username_vendedor = ?
                  AND fecha_venta >= ?
                  AND fecha_venta < ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usernameVendedor);
            stmt.setTimestamp(2, Timestamp.valueOf(inicio));
            stmt.setTimestamp(3, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ResumenVentasVendedor(rs.getInt("cantidad"), rs.getLong("total_importe"));
                }
            }
        }
        return new ResumenVentasVendedor(0, 0);
    }

    @Override
    public List<TicketVenta> listarPorTenant(String tenantOwner) throws SQLException {
        String sql = "select " + COLUMNAS_TICKET + " from tickets_venta "
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
    public List<TicketVenta> buscarPorTenant(String tenantOwner, String texto, int limite) throws SQLException {
        return buscarTickets(
                "tenant_owner = ? AND (folio LIKE ? OR nombre_cliente LIKE ?)",
                tenantOwner,
                texto,
                limite);
    }

    @Override
    public List<TicketVenta> buscarPorVendedor(String usernameVendedor, String texto, int limite) throws SQLException {
        return buscarTickets(
                "username_vendedor = ? AND (folio LIKE ? OR nombre_cliente LIKE ?)",
                usernameVendedor,
                texto,
                limite);
    }

    @Override
    public List<TicketVenta> listarPorTenantYNombreCliente(String tenantOwner, String nombreCliente, int limite)
            throws SQLException {
        int max = limite > 0 ? limite : 20;
        String patron = "%" + nombreCliente.trim() + "%";
        String sql = """
                SELECT %s
                FROM tickets_venta
                WHERE tenant_owner = ?
                  AND nombre_cliente IS NOT NULL
                  AND TRIM(nombre_cliente) <> ''
                  AND LOWER(nombre_cliente) LIKE LOWER(?)
                ORDER BY fecha_venta DESC
                LIMIT ?
                """.formatted(COLUMNAS_TICKET);
        List<TicketVenta> tickets = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantOwner);
            stmt.setString(2, patron);
            stmt.setInt(3, max);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        }
        return tickets;
    }

    private List<TicketVenta> buscarTickets(String condicion, String parametro, String texto, int limite)
            throws SQLException {
        int max = limite > 0 ? limite : 200;
        String patron = "%" + texto.trim() + "%";
        String sql = "SELECT " + COLUMNAS_TICKET + " FROM tickets_venta WHERE " + condicion
                + " ORDER BY fecha_venta DESC LIMIT ?";
        List<TicketVenta> tickets = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, parametro);
            stmt.setString(2, patron);
            stmt.setString(3, patron);
            stmt.setInt(4, max);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        }
        return tickets;
    }

    @Override
    public TicketVenta porFolioDeTenant(String folio, String tenantOwner) throws SQLException {
        String sql = "select " + COLUMNAS_TICKET + " from tickets_venta "
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
        String sql = "select " + COLUMNAS_TICKET + " from tickets_venta "
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

    @Override
    public List<ProductoVentaRanking> topProductosVendidosPorTenant(String tenantOwner,
                                                                    LocalDateTime inicio,
                                                                    LocalDateTime finExclusivo,
                                                                    int limite) throws SQLException {
        int max = Math.min(Math.max(limite, 1), 20);
        String sql = """
                SELECT ti.producto_id, ti.nombre_producto,
                       SUM(ti.cantidad) AS unidades, SUM(ti.importe) AS importe_total
                FROM ticket_items ti
                INNER JOIN tickets_venta t ON ti.ticket_id = t.id
                WHERE t.tenant_owner = ?
                  AND t.estado = 'ACTIVO'
                  AND t.fecha_venta >= ?
                  AND t.fecha_venta < ?
                GROUP BY ti.producto_id, ti.nombre_producto
                ORDER BY unidades DESC, importe_total DESC
                LIMIT ?
                """;
        List<ProductoVentaRanking> ranking = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantOwner);
            stmt.setTimestamp(2, Timestamp.valueOf(inicio));
            stmt.setTimestamp(3, Timestamp.valueOf(finExclusivo));
            stmt.setInt(4, max);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProductoVentaRanking r = new ProductoVentaRanking();
                    r.setProductoId(rs.getLong("producto_id"));
                    r.setNombreProducto(rs.getString("nombre_producto"));
                    r.setUnidadesVendidas(rs.getInt("unidades"));
                    r.setImporteTotal(rs.getLong("importe_total"));
                    ranking.add(r);
                }
            }
        }
        return ranking;
    }

    @Override
    public int contarActivosPorTenant(String tenantOwner) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tickets_venta WHERE tenant_owner = ? AND estado = 'ACTIVO'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private void descontarInventario(TicketVenta ticket) throws SQLException {
        inventarioRecetaService.descontarPorTicket(ticket);
    }

    private void insertarTicket(TicketVenta ticket) throws SQLException {
        String sql = "insert into tickets_venta (folio, username_vendedor, tenant_owner, fecha_venta, total, nombre_cliente, estado) "
                + "values (?, ?, ?, ?, ?, ?, ?)";
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
            if (ticket.getNombreCliente() != null && !ticket.getNombreCliente().isBlank()) {
                stmt.setString(6, ticket.getNombreCliente().trim());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }
            stmt.setString(7, ticket.getEstado() != null ? ticket.getEstado() : "ACTIVO");
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
            ticket.setNombreCliente(rs.getString("nombre_cliente"));
        } catch (SQLException ignored) {
            ticket.setNombreCliente(null);
        }
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
