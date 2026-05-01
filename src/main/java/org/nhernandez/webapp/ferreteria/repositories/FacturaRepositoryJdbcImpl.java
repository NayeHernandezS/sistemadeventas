package org.nhernandez.webapp.ferreteria.repositories;

import jakarta.inject.Inject;
import org.nhernandez.webapp.ferreteria.configs.MysqlConn;
import org.nhernandez.webapp.ferreteria.configs.Repository;
import org.nhernandez.webapp.ferreteria.models.Factura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

@Repository
public class FacturaRepositoryJdbcImpl implements FacturaRepository {

    @Inject
    @MysqlConn
    private Connection conn;

    @Override
    public void guardar(Factura factura) throws SQLException {
        String sql = "insert into facturas (ticket_id, folio_factura, rfc, razon_social, email, direccion, uso_cfdi, fecha_emision) values (?,?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, factura.getTicketId());
            stmt.setString(2, factura.getFolioFactura());
            stmt.setString(3, factura.getRfc());
            stmt.setString(4, factura.getRazonSocial());
            if (factura.getEmail() != null && !factura.getEmail().isBlank()) {
                stmt.setString(5, factura.getEmail());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            if (factura.getDireccion() != null && !factura.getDireccion().isBlank()) {
                stmt.setString(6, factura.getDireccion());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }
            if (factura.getUsoCfdi() != null && !factura.getUsoCfdi().isBlank()) {
                stmt.setString(7, factura.getUsoCfdi());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }
            if (factura.getFechaEmision() != null) {
                stmt.setTimestamp(8, Timestamp.valueOf(factura.getFechaEmision()));
            } else {
                stmt.setNull(8, Types.TIMESTAMP);
            }
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    factura.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Factura porTicketId(Long ticketId) throws SQLException {
        String sql = "select id, ticket_id, folio_factura, rfc, razon_social, email, direccion, uso_cfdi, fecha_emision from facturas where ticket_id = ?";
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
        return f;
    }
}
