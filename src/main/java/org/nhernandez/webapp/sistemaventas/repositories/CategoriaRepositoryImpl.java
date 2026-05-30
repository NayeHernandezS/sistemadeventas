package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CategoriaRepositoryImpl implements CategoriaRepository {

    private Connection conn;

    @Autowired
    public CategoriaRepositoryImpl(@MysqlConn Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<Categoria> listarPorOwner(String ownerUsername) throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "select id, nombre from categorias where owner_username = ? order by nombre asc";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ownerUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categorias.add(getCategoria(rs));
                }
            }
        }
        return categorias;
    }

    @Override
    public Categoria porIdPorOwner(Long id, String ownerUsername) throws SQLException {
        String sql = "select id, nombre, owner_username from categorias where id = ? and owner_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, ownerUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getCategoria(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void guardar(Categoria categoria) throws SQLException {
        if (categoria.getOwnerUsername() == null || categoria.getOwnerUsername().isBlank()) {
            throw new SQLException("owner_username es obligatorio al guardar una categoria");
        }
        String nombre = categoria.getNombre() != null ? categoria.getNombre().trim() : "";
        if (nombre.isBlank()) {
            throw new SQLException("El nombre de la categoria es obligatorio");
        }

        String sql;
        if (categoria.getId() != null && categoria.getId() > 0) {
            sql = "UPDATE categorias SET nombre = ? WHERE id = ? AND owner_username = ?";
        } else {
            sql = "INSERT INTO categorias (nombre, owner_username) VALUES (?, ?)";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            if (categoria.getId() != null && categoria.getId() > 0) {
                stmt.setLong(2, categoria.getId());
                stmt.setString(3, categoria.getOwnerUsername());
            } else {
                stmt.setString(2, categoria.getOwnerUsername());
            }
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo guardar la categoria o no pertenece a tu cuenta");
            }
        }
    }

    @Override
    public void eliminarPorOwner(Long id, String ownerUsername) throws SQLException {
        if (contarProductosAsociados(id, ownerUsername) > 0) {
            throw new SQLException("No se puede eliminar: hay productos que usan esta categoria");
        }
        String sql = "DELETE FROM categorias WHERE id = ? AND owner_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, ownerUsername);
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Categoria no encontrada o sin permiso");
            }
        }
    }

    @Override
    public int contarProductosAsociados(Long categoriaId, String ownerUsername) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM productos WHERE categoria_id = ? AND owner_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, categoriaId);
            stmt.setString(2, ownerUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    @Override
    public void crearSugeridasSiNoExisten(String ownerUsername, List<String> nombres) throws SQLException {
        if (ownerUsername == null || ownerUsername.isBlank() || nombres == null || nombres.isEmpty()) {
            return;
        }
        String existeSql = "select count(1) from categorias where owner_username = ? and nombre = ?";
        String insertSql = "insert into categorias (nombre, owner_username) values (?, ?)";
        try (PreparedStatement existeStmt = conn.prepareStatement(existeSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            for (String nombre : nombres) {
                if (nombre == null || nombre.isBlank()) {
                    continue;
                }
                String limpio = nombre.trim();
                existeStmt.setString(1, ownerUsername);
                existeStmt.setString(2, limpio);
                try (ResultSet rs = existeStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        continue;
                    }
                }
                insertStmt.setString(1, limpio);
                insertStmt.setString(2, ownerUsername);
                insertStmt.executeUpdate();
            }
        }
    }

    private Categoria getCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setNombre(rs.getString("nombre"));
        categoria.setId(rs.getLong("id"));
        try {
            categoria.setOwnerUsername(rs.getString("owner_username"));
        } catch (SQLException ignored) {
            // columnas antiguas sin owner_username en algunas consultas
        }
        return categoria;
    }
}
