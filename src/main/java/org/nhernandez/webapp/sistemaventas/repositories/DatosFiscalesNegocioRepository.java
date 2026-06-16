package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;

import java.sql.SQLException;

public interface DatosFiscalesNegocioRepository {

    DatosFiscalesNegocio porTenant(String tenantUsername) throws SQLException;

    void guardar(DatosFiscalesNegocio datos) throws SQLException;

    void guardarConfiguracionFacturama(DatosFiscalesNegocio datos, boolean actualizarPassword) throws SQLException;
}
