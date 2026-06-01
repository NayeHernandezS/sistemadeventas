package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.Suscripcion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SuscripcionRepositoryJdbcImpl implements SuscripcionRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public Suscripcion porUsername(String username) throws SQLException {
        String sql = "SELECT id, username, fecha_inicio, fecha_fin, en_periodo_prueba, estado, plan_codigo, "
                + "renovacion_automatica, mp_preapproval_id "
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
        String sql = "INSERT INTO suscripciones (username, fecha_inicio, fecha_fin, en_periodo_prueba, estado, plan_codigo) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, suscripcion.getUsername());
            stmt.setTimestamp(2, Timestamp.valueOf(suscripcion.getFechaInicio()));
            stmt.setTimestamp(3, Timestamp.valueOf(suscripcion.getFechaFin()));
            stmt.setBoolean(4, suscripcion.isEnPeriodoPrueba());
            stmt.setString(5, suscripcion.getEstado());
            stmt.setString(6, suscripcion.getPlanCodigo() != null ? suscripcion.getPlanCodigo() : "EMPRENDEDOR");
            stmt.executeUpdate();
        }
    }

    @Override
    public void actualizarPlan(String username, String planCodigo, boolean enPeriodoPrueba) throws SQLException {
        String sql = "UPDATE suscripciones SET plan_codigo = ?, en_periodo_prueba = ?, estado = 'ACTIVA' "
                + "WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, planCodigo);
            stmt.setBoolean(2, enPeriodoPrueba);
            stmt.setString(3, username);
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

    @Override
    public List<Suscripcion> listarVigentesQueVencenEn(int diasDesdeHoy) throws SQLException {
        LocalDate dia = LocalDate.now().plusDays(diasDesdeHoy);
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(LocalTime.MAX);
        String sql = "SELECT id, username, fecha_inicio, fecha_fin, en_periodo_prueba, estado, plan_codigo, "
                + "renovacion_automatica, mp_preapproval_id "
                + "FROM suscripciones WHERE fecha_fin >= ? AND fecha_fin <= ? AND estado = 'ACTIVA'";
        List<Suscripcion> lista = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapSuscripcion(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public void activarRenovacionAutomatica(String username, String planCodigo, String mpPreapprovalId)
            throws SQLException {
        String sql = "UPDATE suscripciones SET renovacion_automatica = 1, mp_preapproval_id = ?, "
                + "plan_codigo = ?, en_periodo_prueba = 0, estado = 'ACTIVA' WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mpPreapprovalId);
            stmt.setString(2, planCodigo);
            stmt.setString(3, username);
            stmt.executeUpdate();
        }
    }

    @Override
    public void desactivarRenovacionAutomatica(String username) throws SQLException {
        String sql = "UPDATE suscripciones SET renovacion_automatica = 0, mp_preapproval_id = NULL WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    @Override
    public Suscripcion porPreapprovalId(String mpPreapprovalId) throws SQLException {
        if (mpPreapprovalId == null || mpPreapprovalId.isBlank()) {
            return null;
        }
        String sql = "SELECT id, username, fecha_inicio, fecha_fin, en_periodo_prueba, estado, plan_codigo, "
                + "renovacion_automatica, mp_preapproval_id "
                + "FROM suscripciones WHERE mp_preapproval_id = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mpPreapprovalId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapSuscripcion(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void actualizarEstado(String username, String estado) throws SQLException {
        String sql = "UPDATE suscripciones SET estado = ? WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setString(2, username);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Suscripcion no encontrada");
            }
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
        try {
            s.setPlanCodigo(rs.getString("plan_codigo"));
        } catch (SQLException ignored) {
            s.setPlanCodigo("EMPRENDEDOR");
        }
        if (s.getPlanCodigo() == null || s.getPlanCodigo().isBlank()) {
            s.setPlanCodigo("EMPRENDEDOR");
        }
        try {
            s.setRenovacionAutomatica(rs.getBoolean("renovacion_automatica"));
        } catch (SQLException ignored) {
            s.setRenovacionAutomatica(false);
        }
        try {
            s.setMpPreapprovalId(rs.getString("mp_preapproval_id"));
        } catch (SQLException ignored) {
            s.setMpPreapprovalId(null);
        }
        return s;
    }
}
