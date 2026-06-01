package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.PreferenciasTenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Repository
public class PreferenciasTenantRepositoryJdbcImpl implements PreferenciasTenantRepository {

    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public PreferenciasTenant porTenant(String tenantUsername) throws SQLException {
        String sql = """
                SELECT tenant_username, stock_minimo, logo_filename
                FROM preferencias_tenant
                WHERE tenant_username = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PreferenciasTenant pref = new PreferenciasTenant();
                    pref.setTenantUsername(rs.getString("tenant_username"));
                    int stock = rs.getInt("stock_minimo");
                    if (!rs.wasNull()) {
                        pref.setStockMinimo(stock);
                    }
                    pref.setLogoFilename(rs.getString("logo_filename"));
                    return pref;
                }
            }
        }
        return null;
    }

    @Override
    public void guardar(PreferenciasTenant preferencias) throws SQLException {
        String sql = """
                INSERT INTO preferencias_tenant (tenant_username, stock_minimo)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE stock_minimo = VALUES(stock_minimo)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, preferencias.getTenantUsername());
            if (preferencias.getStockMinimo() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, preferencias.getStockMinimo());
            }
            stmt.executeUpdate();
        }
    }

    @Override
    public void actualizarLogoFilename(String tenantUsername, String logoFilename) throws SQLException {
        String sql = """
                INSERT INTO preferencias_tenant (tenant_username, logo_filename)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE logo_filename = VALUES(logo_filename)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            stmt.setString(2, logoFilename);
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminarLogoFilename(String tenantUsername) throws SQLException {
        String sql = "UPDATE preferencias_tenant SET logo_filename = NULL WHERE tenant_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            stmt.executeUpdate();
        }
    }
}
