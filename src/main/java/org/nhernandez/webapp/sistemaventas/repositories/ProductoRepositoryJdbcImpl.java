package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TipoItem;

import java.sql.*;
import java.time.LocalDate;
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
    public List<Producto> listarServiciosPorOwner(String ownerUsername) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.owner_username = ? AND p.tipo_item = 'SERVICIO' ORDER BY p.nombre ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ownerUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapProductos(rs);
            }
        }
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
    public Producto porSkuPorOwner(String sku, String ownerUsername) throws SQLException {
        if (ownerUsername == null || ownerUsername.isBlank() || sku == null || sku.isBlank()) {
            return null;
        }
        String sql = SELECT_BASE + "WHERE p.owner_username = ? AND TRIM(p.sku) = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ownerUsername);
            stmt.setString(2, sku.trim());
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
            sql = "UPDATE productos SET nombre=?, precio=?, precio_compra=?, porcentaje_ganancia=?, existencias=?, sku=?, categoria_id=?, tipo_item=? "
                    + "WHERE id=? AND owner_username=?";
        } else {
            sql = "INSERT INTO productos (nombre, precio, precio_compra, porcentaje_ganancia, existencias, sku, categoria_id, fecha_registro, owner_username, tipo_item) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, producto.getNombre());
            stmt.setInt(2, producto.getPrecio());
            stmt.setInt(3, Math.max(producto.getPrecioCompra(), 0));
            stmt.setInt(4, Math.max(producto.getPorcentajeGanancia(), 0));
            stmt.setInt(5, producto.esServicio() ? 0 : producto.getExistencias());
            stmt.setString(6, producto.getSku());
            stmt.setLong(7, producto.getCategoria().getId());

            if (producto.getId() != null && producto.getId() > 0) {
                stmt.setString(8, producto.getTipoItem().name());
                stmt.setLong(9, producto.getId());
                stmt.setString(10, producto.getOwnerUsername());
            } else {
                LocalDate registro = producto.getFechaRegistro() != null
                        ? producto.getFechaRegistro()
                        : LocalDate.now();
                stmt.setDate(8, Date.valueOf(registro));
                stmt.setString(9, producto.getOwnerUsername());
                stmt.setString(10, producto.getTipoItem().name());
            }
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo guardar el producto o no pertenece al usuario");
            }
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
    public boolean existeSkuPorOwner(String ownerUsername, String sku) throws SQLException {
        if (ownerUsername == null || ownerUsername.isBlank() || sku == null || sku.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM productos WHERE owner_username = ? AND sku = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ownerUsername);
            stmt.setString(2, sku.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
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
    public void actualizarExistencias(Long id, String ownerUsername, int existencias) throws SQLException {
        if (existencias < 0) {
            throw new SQLException("Las existencias no pueden ser negativas");
        }
        String sql = "UPDATE productos SET existencias = ? WHERE id = ? AND owner_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, existencias);
            stmt.setLong(2, id);
            stmt.setString(3, ownerUsername);
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Producto no encontrado para actualizar existencias");
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
        p.setPrecioCompra(leerEnteroOpcional(rs, "precio_compra"));
        p.setPorcentajeGanancia(leerEnteroOpcional(rs, "porcentaje_ganancia"));
        p.setExistencias(rs.getInt("existencias"));
        p.setSku(rs.getString("sku"));
        Date fecha = rs.getDate("fecha_registro");
        p.setFechaRegistro(fecha != null ? fecha.toLocalDate() : LocalDate.now());
        p.setOwnerUsername(rs.getString("owner_username"));
        p.setTipoItem(leerTipoItem(rs));
        Categoria c = new Categoria();
        c.setId(rs.getLong("categoria_id"));
        c.setNombre(rs.getString("categoria"));
        p.setCategoria(c);
        return p;
    }

    private static int leerEnteroOpcional(ResultSet rs, String columna) throws SQLException {
        try {
            return rs.getInt(columna);
        } catch (SQLException e) {
            return 0;
        }
    }

    private TipoItem leerTipoItem(ResultSet rs) throws SQLException {
        try {
            return TipoItem.porCodigo(rs.getString("tipo_item")).orElse(TipoItem.PRODUCTO);
        } catch (SQLException e) {
            return TipoItem.PRODUCTO;
        }
    }
}
