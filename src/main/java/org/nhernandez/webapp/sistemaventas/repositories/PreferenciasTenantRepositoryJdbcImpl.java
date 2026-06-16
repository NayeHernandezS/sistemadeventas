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
        try {
            return consultarPreferencias(tenantUsername, true, true);
        } catch (SQLException e) {
            if (columnaAusente(e, "onboarding_completado")) {
                try {
                    return consultarPreferencias(tenantUsername, false, true);
                } catch (SQLException e2) {
                    if (columnaAusente(e2, "logo_filename")) {
                        return consultarPreferencias(tenantUsername, false, false);
                    }
                    throw e2;
                }
            }
            if (columnaAusente(e, "logo_filename")) {
                return consultarPreferencias(tenantUsername, true, false);
            }
            throw e;
        }
    }

    private PreferenciasTenant consultarPreferencias(String tenantUsername,
                                                     boolean incluirOnboarding,
                                                     boolean incluirLogo) throws SQLException {
        String sql = """
                SELECT tenant_username, stock_minimo%s%s
                FROM preferencias_tenant
                WHERE tenant_username = ?
                """.formatted(
                incluirOnboarding ? ", onboarding_completado" : "",
                incluirLogo ? ", logo_filename" : ""
        );
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPreferencias(rs, incluirOnboarding, incluirLogo);
                }
            }
        }
        return null;
    }

    private static PreferenciasTenant mapPreferencias(ResultSet rs,
                                                      boolean incluirOnboarding,
                                                      boolean incluirLogo) throws SQLException {
        PreferenciasTenant pref = new PreferenciasTenant();
        pref.setTenantUsername(rs.getString("tenant_username"));
        int stock = rs.getInt("stock_minimo");
        if (!rs.wasNull()) {
            pref.setStockMinimo(stock);
        }
        if (incluirOnboarding) {
            pref.setOnboardingCompletado(rs.getBoolean("onboarding_completado"));
        } else {
            pref.setOnboardingCompletado(false);
        }
        if (incluirLogo) {
            pref.setLogoFilename(rs.getString("logo_filename"));
        }
        return pref;
    }

    private static boolean columnaAusente(SQLException e, String columna) {
        String msg = e.getMessage();
        return msg != null && msg.contains("Unknown column") && msg.contains(columna);
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
        } catch (SQLException e) {
            if (columnaAusente(e, "logo_filename")) {
                return;
            }
            throw e;
        }
    }

    @Override
    public void eliminarLogoFilename(String tenantUsername) throws SQLException {
        String sql = "UPDATE preferencias_tenant SET logo_filename = NULL WHERE tenant_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantUsername);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (columnaAusente(e, "logo_filename")) {
                return;
            }
            throw e;
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
            if (columnaAusente(e, "onboarding_completado")) {
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
            if (columnaAusente(e, "onboarding_completado")) {
                iniciarOnboarding(tenantUsername);
                return;
            }
            throw e;
        }
    }
}
