package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PagoSuscripcionRepositoryJdbcImpl implements PagoSuscripcionRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public void guardar(PagoSuscripcion pago) throws SQLException {
        String sql = "INSERT INTO pagos_suscripcion (username, meses, monto, fecha_solicitud, estado, notas, "
                + "mp_preference_id, mp_payment_id, canal, plan_codigo) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, pago.getUsername());
            stmt.setInt(2, pago.getMeses());
            stmt.setBigDecimal(3, pago.getMonto());
            stmt.setTimestamp(4, Timestamp.valueOf(pago.getFechaSolicitud()));
            stmt.setString(5, pago.getEstado());
            stmt.setString(6, pago.getNotas());
            stmt.setString(7, pago.getMpPreferenceId());
            stmt.setString(8, pago.getMpPaymentId());
            stmt.setString(9, canalSeguro(pago.getCanal()));
            stmt.setString(10, pago.getPlanCodigo() != null ? pago.getPlanCodigo() : "EMPRENDEDOR");
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    pago.setId(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void actualizarReferenciaMercadoPago(Long id, String preferenceId) throws SQLException {
        String sql = "UPDATE pagos_suscripcion SET mp_preference_id = ? WHERE id = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, preferenceId);
            stmt.setLong(2, id);
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
    public List<PagoSuscripcion> listarPorEstado(String estado) throws SQLException {
        String sql = "SELECT * FROM pagos_suscripcion WHERE estado = ? ORDER BY fecha_solicitud DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
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

    @Override
    public void confirmarMercadoPago(Long id, String mpPaymentId) throws SQLException {
        String sql = "UPDATE pagos_suscripcion SET estado = 'CONFIRMADO', fecha_confirmacion = NOW(), "
                + "mp_payment_id = ?, canal = 'MERCADOPAGO' WHERE id = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mpPaymentId);
            stmt.setLong(2, id);
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Pago no encontrado o ya confirmado");
            }
        }
    }

    @Override
    public int expirarPorId(Long id) throws SQLException {
        String sql = "UPDATE pagos_suscripcion SET estado = 'EXPIRADO' WHERE id = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate();
        }
    }

    @Override
    public int expirarPendientesAnterioresA(java.time.LocalDateTime limiteManual,
                                            java.time.LocalDateTime limiteMercadoPago) throws SQLException {
        String sql = "UPDATE pagos_suscripcion SET estado = 'EXPIRADO' "
                + "WHERE estado = 'PENDIENTE' AND ("
                + "(UPPER(canal) = 'MERCADOPAGO' AND fecha_solicitud < ?) OR "
                + "(UPPER(canal) <> 'MERCADOPAGO' AND fecha_solicitud < ?)"
                + ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(limiteMercadoPago));
            stmt.setTimestamp(2, Timestamp.valueOf(limiteManual));
            return stmt.executeUpdate();
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
        leerColumnaOpcional(rs, "mp_preference_id", p::setMpPreferenceId);
        leerColumnaOpcional(rs, "mp_payment_id", p::setMpPaymentId);
        try {
            String canal = rs.getString("canal");
            if (canal != null && !canal.isBlank()) {
                p.setCanal(canal);
            }
        } catch (SQLException ignored) {
            p.setCanal("MANUAL");
        }
        try {
            p.setPlanCodigo(rs.getString("plan_codigo"));
        } catch (SQLException ignored) {
            p.setPlanCodigo("EMPRENDEDOR");
        }
        if (p.getPlanCodigo() == null || p.getPlanCodigo().isBlank()) {
            p.setPlanCodigo("EMPRENDEDOR");
        }
        return p;
    }

    private static void leerColumnaOpcional(ResultSet rs, String columna,
                                            java.util.function.Consumer<String> setter) {
        try {
            setter.accept(rs.getString(columna));
        } catch (SQLException ignored) {
            setter.accept(null);
        }
    }

    private static String canalSeguro(String canal) {
        if (canal == null || canal.isBlank()) {
            return "MANUAL";
        }
        return canal.trim().toUpperCase();
    }
}
