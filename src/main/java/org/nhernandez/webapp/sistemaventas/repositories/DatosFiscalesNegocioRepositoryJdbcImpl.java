package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Repository
public class DatosFiscalesNegocioRepositoryJdbcImpl implements DatosFiscalesNegocioRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public DatosFiscalesNegocio porTenant(String tenantUsername) throws SQLException {
        String sql = """
                SELECT tenant_username, rfc, razon_social, email, direccion, uso_cfdi
                FROM datos_fiscales_negocio
                WHERE tenant_username = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void guardar(DatosFiscalesNegocio datos) throws SQLException {
        String sql = """
                INSERT INTO datos_fiscales_negocio
                    (tenant_username, rfc, razon_social, email, direccion, uso_cfdi)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    rfc = VALUES(rfc),
                    razon_social = VALUES(razon_social),
                    email = VALUES(email),
                    direccion = VALUES(direccion),
                    uso_cfdi = VALUES(uso_cfdi)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, datos.getTenantUsername());
            setNullable(stmt, 2, datos.getRfc());
            setNullable(stmt, 3, datos.getRazonSocial());
            setNullable(stmt, 4, datos.getEmail());
            setNullable(stmt, 5, datos.getDireccion());
            setNullable(stmt, 6, datos.getUsoCfdi());
            stmt.executeUpdate();
        }
    }

    private static DatosFiscalesNegocio mapear(ResultSet rs) throws SQLException {
        DatosFiscalesNegocio datos = new DatosFiscalesNegocio();
        datos.setTenantUsername(rs.getString("tenant_username"));
        datos.setRfc(rs.getString("rfc"));
        datos.setRazonSocial(rs.getString("razon_social"));
        datos.setEmail(rs.getString("email"));
        datos.setDireccion(rs.getString("direccion"));
        datos.setUsoCfdi(rs.getString("uso_cfdi"));
        return datos;
    }

    private static void setNullable(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value.trim());
        }
    }
}
