package org.nhernandez.webapp.sistemaventas.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Repository
public class DatosFiscalesNegocioRepositoryJdbcImpl implements DatosFiscalesNegocioRepository {

    private static final String SELECT_BASE = """
            SELECT tenant_username, rfc, razon_social, email, direccion, uso_cfdi,
                   codigo_postal, regimen_fiscal, facturama_username, facturama_password_enc,
                   facturama_sandbox, cfdi_habilitado
            FROM datos_fiscales_negocio
            """;

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public DatosFiscalesNegocio porTenant(String tenantUsername) throws SQLException {
        String sql = SELECT_BASE + " WHERE tenant_username = ?";
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
                    (tenant_username, rfc, razon_social, email, direccion, uso_cfdi, codigo_postal, regimen_fiscal)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    rfc = VALUES(rfc),
                    razon_social = VALUES(razon_social),
                    email = VALUES(email),
                    direccion = VALUES(direccion),
                    uso_cfdi = VALUES(uso_cfdi),
                    codigo_postal = VALUES(codigo_postal),
                    regimen_fiscal = VALUES(regimen_fiscal)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, datos.getTenantUsername());
            setNullable(stmt, 2, datos.getRfc());
            setNullable(stmt, 3, datos.getRazonSocial());
            setNullable(stmt, 4, datos.getEmail());
            setNullable(stmt, 5, datos.getDireccion());
            setNullable(stmt, 6, datos.getUsoCfdi());
            setNullable(stmt, 7, datos.getCodigoPostal());
            setNullable(stmt, 8, datos.getRegimenFiscal());
            stmt.executeUpdate();
        }
    }

    @Override
    public void guardarConfiguracionFacturama(DatosFiscalesNegocio datos, boolean actualizarPassword)
            throws SQLException {
        DatosFiscalesNegocio existente = porTenant(datos.getTenantUsername());
        if (existente == null) {
            String sql = """
                    INSERT INTO datos_fiscales_negocio
                        (tenant_username, facturama_username, facturama_password_enc,
                         facturama_sandbox, cfdi_habilitado)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, datos.getTenantUsername());
                setNullable(stmt, 2, datos.getFacturamaUsername());
                setNullable(stmt, 3, datos.getFacturamaPasswordEnc());
                stmt.setInt(4, datos.isFacturamaSandbox() ? 1 : 0);
                stmt.setInt(5, datos.isCfdiHabilitado() ? 1 : 0);
                stmt.executeUpdate();
            }
            return;
        }

        String sql = actualizarPassword
                ? """
                UPDATE datos_fiscales_negocio
                SET facturama_username = ?, facturama_password_enc = ?,
                    facturama_sandbox = ?, cfdi_habilitado = ?
                WHERE tenant_username = ?
                """
                : """
                UPDATE datos_fiscales_negocio
                SET facturama_username = ?, facturama_sandbox = ?, cfdi_habilitado = ?
                WHERE tenant_username = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, datos.getFacturamaUsername());
            if (actualizarPassword) {
                setNullable(stmt, 2, datos.getFacturamaPasswordEnc());
                stmt.setInt(3, datos.isFacturamaSandbox() ? 1 : 0);
                stmt.setInt(4, datos.isCfdiHabilitado() ? 1 : 0);
                stmt.setString(5, datos.getTenantUsername());
            } else {
                stmt.setInt(2, datos.isFacturamaSandbox() ? 1 : 0);
                stmt.setInt(3, datos.isCfdiHabilitado() ? 1 : 0);
                stmt.setString(4, datos.getTenantUsername());
            }
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
        datos.setCodigoPostal(rs.getString("codigo_postal"));
        datos.setRegimenFiscal(rs.getString("regimen_fiscal"));
        datos.setFacturamaUsername(rs.getString("facturama_username"));
        datos.setFacturamaPasswordEnc(rs.getString("facturama_password_enc"));
        datos.setFacturamaSandbox(rs.getInt("facturama_sandbox") == 1);
        datos.setCfdiHabilitado(rs.getInt("cfdi_habilitado") == 1);
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
