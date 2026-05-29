package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.SolicitudSoporte;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SolicitudSoporteRepositoryJdbcImpl implements SolicitudSoporteRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public void guardar(SolicitudSoporte solicitud) throws SQLException {
        String sql = "INSERT INTO solicitudes_soporte (tenant_owner, username, email_contacto, asunto, mensaje, "
                + "fecha_solicitud, estado) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, solicitud.getTenantOwner());
            stmt.setString(2, solicitud.getUsername());
            stmt.setString(3, solicitud.getEmailContacto());
            stmt.setString(4, solicitud.getAsunto());
            stmt.setString(5, solicitud.getMensaje());
            stmt.setTimestamp(6, Timestamp.valueOf(solicitud.getFechaSolicitud()));
            stmt.setString(7, solicitud.getEstado());
            stmt.executeUpdate();
        }
    }

    @Override
    public List<SolicitudSoporte> listarPorTenant(String tenantOwner) throws SQLException {
        String sql = "SELECT * FROM solicitudes_soporte WHERE tenant_owner = ? ORDER BY fecha_solicitud DESC";
        return listar(sql, tenantOwner);
    }

    @Override
    public List<SolicitudSoporte> listarTodas() throws SQLException {
        String sql = "SELECT * FROM solicitudes_soporte ORDER BY fecha_solicitud DESC";
        return listar(sql, null);
    }

    @Override
    public List<SolicitudSoporte> listarAbiertas() throws SQLException {
        String sql = "SELECT * FROM solicitudes_soporte WHERE estado = 'ABIERTA' ORDER BY fecha_solicitud ASC";
        return listar(sql, null);
    }

    @Override
    public void marcarAtendida(Long id) throws SQLException {
        String sql = "UPDATE solicitudes_soporte SET estado = 'ATENDIDA' WHERE id = ? AND estado = 'ABIERTA'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private List<SolicitudSoporte> listar(String sql, String tenantOwner) throws SQLException {
        List<SolicitudSoporte> lista = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (tenantOwner != null) {
                stmt.setString(1, tenantOwner);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(map(rs));
                }
            }
        }
        return lista;
    }

    private SolicitudSoporte map(ResultSet rs) throws SQLException {
        SolicitudSoporte s = new SolicitudSoporte();
        s.setId(rs.getLong("id"));
        s.setTenantOwner(rs.getString("tenant_owner"));
        s.setUsername(rs.getString("username"));
        s.setEmailContacto(rs.getString("email_contacto"));
        s.setAsunto(rs.getString("asunto"));
        s.setMensaje(rs.getString("mensaje"));
        s.setFechaSolicitud(rs.getTimestamp("fecha_solicitud").toLocalDateTime());
        s.setEstado(rs.getString("estado"));
        return s;
    }
}
