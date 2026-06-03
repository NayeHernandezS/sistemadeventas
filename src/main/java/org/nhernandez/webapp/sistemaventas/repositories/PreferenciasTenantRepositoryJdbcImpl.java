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
                SELECT tenant_username, stock_minimo, onboarding_completado, logo_filename
                FROM preferencias_tenant
                WHERE tenant_username = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPreferencias(rs);
                }
            }
        } catch (SQLException e) {
            if (columnaOnboardingAusente(e)) {
                return porTenantSinOnboarding(tenantUsername);
            }
            throw e;
        }
        return null;
    }

    private PreferenciasTenant porTenantSinOnboarding(String tenantUsername) throws SQLException {
        String sql = "SELECT tenant_username, stock_minimo, logo_filename FROM preferencias_tenant WHERE tenant_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PreferenciasTenant pref = mapPreferenciasBasico(rs);
                    pref.setOnboardingCompletado(false);
                    return pref;
                }
            }
        }
        return null;
    }

    private static PreferenciasTenant mapPreferencias(ResultSet rs) throws SQLException {
        PreferenciasTenant pref = mapPreferenciasBasico(rs);
        try {
            pref.setOnboardingCompletado(rs.getBoolean("onboarding_completado"));
        } catch (SQLException ignored) {
            pref.setOnboardingCompletado(false);
        }
        return pref;
    }

    private static PreferenciasTenant mapPreferenciasBasico(ResultSet rs) throws SQLException {
        PreferenciasTenant pref = new PreferenciasTenant();
        pref.setTenantUsername(rs.getString("tenant_username"));
        int stock = rs.getInt("stock_minimo");
        if (!rs.wasNull()) {
            pref.setStockMinimo(stock);
        }
        pref.setLogoFilename(rs.getString("logo_filename"));
        return pref;
    }

    private static boolean columnaOnboardingAusente(SQLException e) {
        String msg = e.getMessage();
        return msg != null && msg.contains("onboarding_completado");
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

    @Override
    public void iniciarOnboarding(String tenantUsername) throws SQLException {
        String sql = """
                INSERT INTO preferencias_tenant (tenant_username, onboarding_completado)
                VALUES (?, 0)
                ON DUPLICATE KEY UPDATE tenant_username = tenant_username
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (columnaOnboardingAusente(e)) {
                String fallback = """
                        INSERT INTO preferencias_tenant (tenant_username)
                        VALUES (?)
                        ON DUPLICATE KEY UPDATE tenant_username = tenant_username
                        """;
                try (PreparedStatement stmt = conn.prepareStatement(fallback)) {
                    stmt.setString(1, tenantUsername);
                    stmt.executeUpdate();
                }
                return;
            }
            throw e;
        }
    }

    @Override
    public void marcarOnboardingCompletado(String tenantUsername) throws SQLException {
        String sql = """
                INSERT INTO preferencias_tenant (tenant_username, onboarding_completado)
                VALUES (?, 1)
                ON DUPLICATE KEY UPDATE onboarding_completado = 1
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (columnaOnboardingAusente(e)) {
                iniciarOnboarding(tenantUsername);
                return;
            }
            throw e;
        }
    }
}
