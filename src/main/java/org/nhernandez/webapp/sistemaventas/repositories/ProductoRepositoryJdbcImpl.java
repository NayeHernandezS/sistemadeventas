package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class ProductoRepositoryJdbcImpl implements ProductoRepository {

    private static final String SELECT_BASE =
            "SELECT p.*, c.nombre as categoria FROM productos p "
                    + "INNER JOIN categorias c ON (p.categoria_id = c.id AND c.owner_username = p.owner_username) ";

    @Autowired
    @MysqlConn
    private Connection conn;

    private final Logger log = Logger.getLogger(ProductoRepositoryJdbcImpl.class.getName());

    @PostConstruct
    public void inicializar() {
        log.info("inicializando el beans " + this.getClass().getName());
    }

    @PreDestroy
    public void destruir() {
        log.info("Destruyendo el beans " + getClass().getName());
    }

    @Override
    public List<Producto> listar() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_BASE + "ORDER BY p.id ASC")) {
            return mapProductos(rs);
        }
    }

    @Override
    public List<Producto> listarPorOwner(String ownerUsername) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.owner_username = ? ORDER BY p.id ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ownerUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapProductos(rs);
            }
        }
    }

    @Override
    public Producto porId(Long id) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getProducto(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Producto porIdPorOwner(Long id, String ownerUsername) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id = ? AND p.owner_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, ownerUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getProducto(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void guardar(Producto producto) throws SQLException {
        if (producto.getOwnerUsername() == null || producto.getOwnerUsername().isBlank()) {
            throw new SQLException("owner_username es obligatorio al guardar un producto");
        }

        String sql;
        if (producto.getId() != null && producto.getId() > 0) {
            sql = "UPDATE productos SET nombre=?, precio=?, existencias=?, sku=?, categoria_id=? "
                    + "WHERE id=? AND owner_username=?";
        } else {
            sql = "INSERT INTO productos (nombre, precio, existencias, sku, categoria_id, fecha_registro, owner_username) "
                    + "VALUES (?,?,?,?,?,?,?)";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, producto.getNombre());
            stmt.setInt(2, producto.getPrecio());
            stmt.setInt(3, producto.getExistencias());
            stmt.setString(4, producto.getSku());
            stmt.setLong(5, producto.getCategoria().getId());

            if (producto.getId() != null && producto.getId() > 0) {
                stmt.setLong(6, producto.getId());
                stmt.setString(7, producto.getOwnerUsername());
            } else {
                stmt.setDate(6, Date.valueOf(producto.getFechaRegistro()));
                stmt.setString(7, producto.getOwnerUsername());
            }
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo guardar el producto o no pertenece al usuario");
            }
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public int contarPorOwner(String ownerUsername) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM productos WHERE owner_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ownerUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    @Override
    public void agregarExistencias(Long id, String ownerUsername, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            return;
        }
        String sql = "UPDATE productos SET existencias = existencias + ? WHERE id = ? AND owner_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setLong(2, id);
            stmt.setString(3, ownerUsername);
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Producto no encontrado para reintegrar inventario");
            }
        }
    }

    @Override
    public void descontarExistencias(Long id, String ownerUsername, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            return;
        }
        String sql = "UPDATE productos SET existencias = existencias - ? "
                + "WHERE id = ? AND owner_username = ? AND existencias >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setLong(2, id);
            stmt.setString(3, ownerUsername);
            stmt.setInt(4, cantidad);
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Stock insuficiente para el producto id=" + id);
            }
        }
    }

    @Override
    public void eliminarPorOwner(Long id, String ownerUsername) throws SQLException {
        String sql = "DELETE FROM productos WHERE id=? AND owner_username=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, ownerUsername);
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Producto no encontrado o sin permiso");
            }
        }
    }

    private List<Producto> mapProductos(ResultSet rs) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        while (rs.next()) {
            productos.add(getProducto(rs));
        }
        return productos;
    }

    private Producto getProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getLong("id"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecio(rs.getInt("precio"));
        p.setExistencias(rs.getInt("existencias"));
        p.setSku(rs.getString("sku"));
        p.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
        p.setOwnerUsername(rs.getString("owner_username"));
        Categoria c = new Categoria();
        c.setId(rs.getLong("categoria_id"));
        c.setNombre(rs.getString("categoria"));
        p.setCategoria(c);
        return p;
    }
}
