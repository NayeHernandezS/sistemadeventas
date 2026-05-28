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
    public List<Categoria> listar() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from categorias")){
            while (rs.next()) {
                Categoria categoria = getCategoria(rs);
                categorias.add(categoria);
            }

        }
        return categorias;
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
    public Categoria porId(Long id) throws SQLException {
        Categoria categoria = null;
        try (PreparedStatement stmt = conn.prepareStatement("select * from categorias as c where c.id=?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    categoria = getCategoria(rs);
                }
            }
        }
        return categoria;
    }

    @Override
    public void guardar(Categoria categoria) throws SQLException {

    }

    @Override
    public void eliminar(Long id) throws SQLException {

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
        return categoria;
    }
}
