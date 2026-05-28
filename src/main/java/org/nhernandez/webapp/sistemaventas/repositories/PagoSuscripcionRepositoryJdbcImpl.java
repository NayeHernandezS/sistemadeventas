package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PagoSuscripcionRepositoryJdbcImpl implements PagoSuscripcionRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public void guardar(PagoSuscripcion pago) throws SQLException {
        String sql = "INSERT INTO pagos_suscripcion (username, meses, monto, fecha_solicitud, estado, notas) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pago.getUsername());
            stmt.setInt(2, pago.getMeses());
            stmt.setBigDecimal(3, pago.getMonto());
            stmt.setTimestamp(4, Timestamp.valueOf(pago.getFechaSolicitud()));
            stmt.setString(5, pago.getEstado());
            stmt.setString(6, pago.getNotas());
            stmt.executeUpdate();
        }
    }

    @Override
    public PagoSuscripcion porId(Long id) throws SQLException {
        String sql = "SELECT * FROM pagos_suscripcion WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPago(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<PagoSuscripcion> listarPendientes() throws SQLException {
        String sql = "SELECT * FROM pagos_suscripcion WHERE estado = 'PENDIENTE' ORDER BY fecha_solicitud ASC";
        return listar(sql);
    }

    @Override
    public List<PagoSuscripcion> listarPendientesPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM pagos_suscripcion WHERE estado = 'PENDIENTE' AND username = ? ORDER BY fecha_solicitud ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapLista(rs);
            }
        }
    }

    @Override
    public List<PagoSuscripcion> listarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM pagos_suscripcion WHERE username = ? ORDER BY fecha_solicitud DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapLista(rs);
            }
        }
    }

    @Override
    public void confirmar(Long id) throws SQLException {
        String sql = "UPDATE pagos_suscripcion SET estado = 'CONFIRMADO', fecha_confirmacion = NOW() "
                + "WHERE id = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Pago no encontrado o ya confirmado");
            }
        }
    }

    private List<PagoSuscripcion> listar(String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return mapLista(rs);
        }
    }

    private List<PagoSuscripcion> mapLista(ResultSet rs) throws SQLException {
        List<PagoSuscripcion> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(mapPago(rs));
        }
        return lista;
    }

    private PagoSuscripcion mapPago(ResultSet rs) throws SQLException {
        PagoSuscripcion p = new PagoSuscripcion();
        p.setId(rs.getLong("id"));
        p.setUsername(rs.getString("username"));
        p.setMeses(rs.getInt("meses"));
        p.setMonto(rs.getBigDecimal("monto"));
        p.setFechaSolicitud(rs.getTimestamp("fecha_solicitud").toLocalDateTime());
        Timestamp conf = rs.getTimestamp("fecha_confirmacion");
        if (conf != null) {
            p.setFechaConfirmacion(conf.toLocalDateTime());
        }
        p.setEstado(rs.getString("estado"));
        p.setNotas(rs.getString("notas"));
        return p;
    }
}
