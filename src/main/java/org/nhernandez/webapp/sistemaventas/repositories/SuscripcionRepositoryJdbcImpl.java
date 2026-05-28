package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.Suscripcion;

import java.sql.*;
import java.time.LocalDateTime;

@Repository
public class SuscripcionRepositoryJdbcImpl implements SuscripcionRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public Suscripcion porUsername(String username) throws SQLException {
        String sql = "SELECT id, username, fecha_inicio, fecha_fin, en_periodo_prueba, estado "
                + "FROM suscripciones WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapSuscripcion(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void guardar(Suscripcion suscripcion) throws SQLException {
        String sql = "INSERT INTO suscripciones (username, fecha_inicio, fecha_fin, en_periodo_prueba, estado) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, suscripcion.getUsername());
            stmt.setTimestamp(2, Timestamp.valueOf(suscripcion.getFechaInicio()));
            stmt.setTimestamp(3, Timestamp.valueOf(suscripcion.getFechaFin()));
            stmt.setBoolean(4, suscripcion.isEnPeriodoPrueba());
            stmt.setString(5, suscripcion.getEstado());
            stmt.executeUpdate();
        }
    }

    @Override
    public void extenderVigencia(String username, LocalDateTime nuevaFechaFin, boolean enPeriodoPrueba)
            throws SQLException {
        String sql = "UPDATE suscripciones SET fecha_fin = ?, en_periodo_prueba = ?, estado = 'ACTIVA' "
                + "WHERE username = ? AND id > 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(nuevaFechaFin));
            stmt.setBoolean(2, enPeriodoPrueba);
            stmt.setString(3, username);
            stmt.executeUpdate();
        }
    }

    private Suscripcion mapSuscripcion(ResultSet rs) throws SQLException {
        Suscripcion s = new Suscripcion();
        s.setId(rs.getLong("id"));
        s.setUsername(rs.getString("username"));
        s.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        s.setFechaFin(rs.getTimestamp("fecha_fin").toLocalDateTime());
        s.setEnPeriodoPrueba(rs.getBoolean("en_periodo_prueba"));
        s.setEstado(rs.getString("estado"));
        return s;
    }
}
