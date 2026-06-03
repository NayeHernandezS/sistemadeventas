package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Repository
public class SuscripcionCorreoEnviadoRepositoryJdbcImpl implements SuscripcionCorreoEnviadoRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public boolean yaEnviado(String username, String tipo, LocalDate fechaVencimientoRef) throws SQLException {
        String sql = "SELECT 1 FROM suscripcion_correos_enviados "
                + "WHERE username = ? AND tipo = ? AND fecha_vencimiento_ref = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, tipo);
            stmt.setDate(3, Date.valueOf(fechaVencimientoRef));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void registrar(String username, String tipo, LocalDate fechaVencimientoRef) throws SQLException {
        String sql = "INSERT INTO suscripcion_correos_enviados (username, tipo, fecha_vencimiento_ref) "
                + "VALUES (?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, tipo);
            stmt.setDate(3, Date.valueOf(fechaVencimientoRef));
            stmt.executeUpdate();
        }
    }
}
