package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.PreferenciasTenant;

import java.sql.SQLException;

public interface PreferenciasTenantRepository {

    PreferenciasTenant porTenant(String tenantUsername) throws SQLException;

    void guardar(PreferenciasTenant preferencias) throws SQLException;

    void actualizarLogoFilename(String tenantUsername, String logoFilename) throws SQLException;

    void eliminarLogoFilename(String tenantUsername) throws SQLException;
}
