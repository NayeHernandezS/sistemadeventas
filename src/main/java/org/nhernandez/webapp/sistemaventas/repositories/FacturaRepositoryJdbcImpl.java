package org.nhernandez.webapp.sistemaventas.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.Factura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

@Repository
public class FacturaRepositoryJdbcImpl implements FacturaRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public void guardar(Factura factura) throws SQLException {
        String sql = """
                INSERT INTO facturas (ticket_id, folio_factura, rfc, razon_social, email, direccion, uso_cfdi,
                    fecha_emision, codigo_postal_receptor, cfdi_estado, cfdi_mensaje)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            stmt.setLong(i++, factura.getTicketId());
            stmt.setString(i++, factura.getFolioFactura());
            stmt.setString(i++, factura.getRfc());
            stmt.setString(i++, factura.getRazonSocial());
            setNullableString(stmt, i++, factura.getEmail());
            setNullableString(stmt, i++, factura.getDireccion());
            setNullableString(stmt, i++, factura.getUsoCfdi());
            if (factura.getFechaEmision() != null) {
                stmt.setTimestamp(i++, Timestamp.valueOf(factura.getFechaEmision()));
            } else {
                stmt.setNull(i++, Types.TIMESTAMP);
            }
            setNullableString(stmt, i++, factura.getCodigoPostalReceptor());
            stmt.setString(i++, estadoSeguro(factura.getCfdiEstado()));
            setNullableString(stmt, i, factura.getCfdiMensaje());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    factura.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public void actualizarCfdi(Factura factura) throws SQLException {
        String sql = """
                UPDATE facturas SET cfdi_uuid = ?, cfdi_estado = ?, cfdi_mensaje = ?, cfdi_proveedor_id = ?
                WHERE id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setNullableString(stmt, 1, factura.getCfdiUuid());
            stmt.setString(2, estadoSeguro(factura.getCfdiEstado()));
            setNullableString(stmt, 3, factura.getCfdiMensaje());
            setNullableString(stmt, 4, factura.getCfdiProveedorId());
            stmt.setLong(5, factura.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public Factura porTicketId(Long ticketId) throws SQLException {
        String sql = """
                SELECT id, ticket_id, folio_factura, rfc, razon_social, email, direccion, uso_cfdi, fecha_emision,
                       codigo_postal_receptor, cfdi_uuid, cfdi_estado, cfdi_mensaje, cfdi_proveedor_id
                FROM facturas WHERE ticket_id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    private Factura map(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setId(rs.getLong("id"));
        f.setTicketId(rs.getLong("ticket_id"));
        f.setFolioFactura(rs.getString("folio_factura"));
        f.setRfc(rs.getString("rfc"));
        f.setRazonSocial(rs.getString("razon_social"));
        f.setEmail(rs.getString("email"));
        f.setDireccion(rs.getString("direccion"));
        f.setUsoCfdi(rs.getString("uso_cfdi"));
        Timestamp ts = rs.getTimestamp("fecha_emision");
        f.setFechaEmision(ts != null ? ts.toLocalDateTime() : null);
        f.setCodigoPostalReceptor(rs.getString("codigo_postal_receptor"));
        f.setCfdiUuid(rs.getString("cfdi_uuid"));
        f.setCfdiEstado(rs.getString("cfdi_estado"));
        f.setCfdiMensaje(rs.getString("cfdi_mensaje"));
        f.setCfdiProveedorId(rs.getString("cfdi_proveedor_id"));
        return f;
    }

    private static String estadoSeguro(String estado) {
        if (estado == null || estado.isBlank()) {
            return "INFORMATIVO";
        }
        return estado.trim().toUpperCase();
    }

    private static void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value.trim());
        }
    }
}
