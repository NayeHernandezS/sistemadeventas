package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.TokenRecuperacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Repository
public class TokenRecuperacionRepositoryJdbcImpl implements TokenRecuperacionRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public void guardar(TokenRecuperacion token) throws SQLException {
        String sql = "INSERT INTO tokens_recuperacion (username, token, fecha_creacion, fecha_expiracion, usado) "
                + "VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token.getUsername());
            stmt.setString(2, token.getToken());
            stmt.setTimestamp(3, Timestamp.valueOf(token.getFechaCreacion()));
            stmt.setTimestamp(4, Timestamp.valueOf(token.getFechaExpiracion()));
            stmt.executeUpdate();
        }
    }

    @Override
    public TokenRecuperacion porToken(String token) throws SQLException {
        String sql = "SELECT id, username, token, fecha_creacion, fecha_expiracion, usado "
                + "FROM tokens_recuperacion WHERE token = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void invalidarPorUsername(String username) throws SQLException {
        String sql = "UPDATE tokens_recuperacion SET usado = 1 WHERE username = ? AND usado = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    @Override
    public void marcarUsado(Long id) throws SQLException {
        String sql = "UPDATE tokens_recuperacion SET usado = 1 WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private TokenRecuperacion map(ResultSet rs) throws SQLException {
        TokenRecuperacion t = new TokenRecuperacion();
        t.setId(rs.getLong("id"));
        t.setUsername(rs.getString("username"));
        t.setToken(rs.getString("token"));
        t.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        t.setFechaExpiracion(rs.getTimestamp("fecha_expiracion").toLocalDateTime());
        t.setUsado(rs.getBoolean("usado"));
        return t;
    }
}
