package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.SolicitudSoporte;

import java.sql.SQLException;
import java.util.List;

public interface SolicitudSoporteRepository {

    void guardar(SolicitudSoporte solicitud) throws SQLException;

    List<SolicitudSoporte> listarPorTenant(String tenantOwner) throws SQLException;

    List<SolicitudSoporte> listarTodas() throws SQLException;

    List<SolicitudSoporte> listarAbiertas() throws SQLException;

    void marcarAtendida(Long id) throws SQLException;
}
