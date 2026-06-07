package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.CitaServicio;
import org.nhernandez.webapp.sistemaventas.models.EstadoCita;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CitaServicioRepositoryJdbcImpl implements CitaServicioRepository {

    private static final String SELECT_BASE = """
            SELECT c.id, c.tenant_owner, c.producto_id, c.cliente_id, c.fecha_hora, c.duracion_minutos,
                   c.estado, c.notas, c.username_registro, c.fecha_registro, c.ticket_id,
                   p.nombre AS servicio_nombre,
                   cl.nombre AS cliente_nombre
            FROM citas_servicio c
            INNER JOIN productos p ON (p.id = c.producto_id AND p.owner_username = c.tenant_owner)
            LEFT JOIN clientes cl ON (cl.id = c.cliente_id AND cl.tenant_owner = c.tenant_owner)
            """;

    private final Connection conn;

    @Autowired
    public CitaServicioRepositoryJdbcImpl(@MysqlConn Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<CitaServicio> listarPorTenantEnRango(String tenantOwner,
                                                       LocalDateTime desde,
                                                       LocalDateTime hastaExclusivo) throws SQLException {
        String sql = SELECT_BASE + """
                WHERE c.tenant_owner = ?
                  AND c.fecha_hora >= ?
                  AND c.fecha_hora < ?
                ORDER BY c.fecha_hora ASC
                """;
        List<CitaServicio> citas = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantOwner);
            stmt.setTimestamp(2, Timestamp.valueOf(desde));
            stmt.setTimestamp(3, Timestamp.valueOf(hastaExclusivo));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    citas.add(mapear(rs));
                }
            }
        }
        return citas;
    }

    @Override
    public Optional<CitaServicio> porIdPorTenant(Long id, String tenantOwner) throws SQLException {
        String sql = SELECT_BASE + " WHERE c.id = ? AND c.tenant_owner = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, tenantOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void guardar(CitaServicio cita) throws SQLException {
        String sql = """
                INSERT INTO citas_servicio (tenant_owner, producto_id, cliente_id, fecha_hora, duracion_minutos,
                    estado, notas, username_registro, fecha_registro, ticket_id)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindCita(stmt, cita);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    cita.setId(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void actualizar(CitaServicio cita) throws SQLException {
        String sql = """
                UPDATE citas_servicio SET producto_id=?, cliente_id=?, fecha_hora=?, duracion_minutos=?,
                    estado=?, notas=?, ticket_id=?
                WHERE id=? AND tenant_owner=?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cita.getProductoId());
            if (cita.getClienteId() != null && cita.getClienteId() > 0) {
                stmt.setLong(2, cita.getClienteId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setTimestamp(3, Timestamp.valueOf(cita.getFechaHora()));
            stmt.setInt(4, cita.getDuracionMinutos());
            stmt.setString(5, cita.getEstado().name());
            stmt.setString(6, cita.getNotas());
            if (cita.getTicketId() != null) {
                stmt.setLong(7, cita.getTicketId());
            } else {
                stmt.setNull(7, Types.BIGINT);
            }
            stmt.setLong(8, cita.getId());
            stmt.setString(9, cita.getTenantOwner());
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Cita no encontrada o sin permiso");
            }
        }
    }

    @Override
    public void actualizarEstado(Long id, String tenantOwner, EstadoCita estado, Long ticketId) throws SQLException {
        String sql = "UPDATE citas_servicio SET estado = ?, ticket_id = ? WHERE id = ? AND tenant_owner = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado.name());
            if (ticketId != null) {
                stmt.setLong(2, ticketId);
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setLong(3, id);
            stmt.setString(4, tenantOwner);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Cita no encontrada o sin permiso");
            }
        }
    }

    private void bindCita(PreparedStatement stmt, CitaServicio cita) throws SQLException {
        stmt.setString(1, cita.getTenantOwner());
        stmt.setLong(2, cita.getProductoId());
        if (cita.getClienteId() != null && cita.getClienteId() > 0) {
            stmt.setLong(3, cita.getClienteId());
        } else {
            stmt.setNull(3, Types.BIGINT);
        }
        stmt.setTimestamp(4, Timestamp.valueOf(cita.getFechaHora()));
        stmt.setInt(5, cita.getDuracionMinutos());
        stmt.setString(6, cita.getEstado().name());
        stmt.setString(7, cita.getNotas());
        stmt.setString(8, cita.getUsernameRegistro());
        LocalDateTime registro = cita.getFechaRegistro() != null ? cita.getFechaRegistro() : LocalDateTime.now();
        stmt.setTimestamp(9, Timestamp.valueOf(registro));
        if (cita.getTicketId() != null) {
            stmt.setLong(10, cita.getTicketId());
        } else {
            stmt.setNull(10, Types.BIGINT);
        }
    }

    private CitaServicio mapear(ResultSet rs) throws SQLException {
        CitaServicio cita = new CitaServicio();
        cita.setId(rs.getLong("id"));
        cita.setTenantOwner(rs.getString("tenant_owner"));
        cita.setProductoId(rs.getLong("producto_id"));
        long clienteId = rs.getLong("cliente_id");
        if (!rs.wasNull()) {
            cita.setClienteId(clienteId);
        }
        Timestamp fh = rs.getTimestamp("fecha_hora");
        if (fh != null) {
            cita.setFechaHora(fh.toLocalDateTime());
        }
        cita.setDuracionMinutos(rs.getInt("duracion_minutos"));
        cita.setEstado(EstadoCita.porCodigo(rs.getString("estado")).orElse(EstadoCita.PROGRAMADA));
        cita.setNotas(rs.getString("notas"));
        cita.setUsernameRegistro(rs.getString("username_registro"));
        Timestamp fr = rs.getTimestamp("fecha_registro");
        if (fr != null) {
            cita.setFechaRegistro(fr.toLocalDateTime());
        }
        long ticketId = rs.getLong("ticket_id");
        if (!rs.wasNull()) {
            cita.setTicketId(ticketId);
        }
        cita.setServicioNombre(rs.getString("servicio_nombre"));
        cita.setClienteNombre(rs.getString("cliente_nombre"));
        return cita;
    }
}
