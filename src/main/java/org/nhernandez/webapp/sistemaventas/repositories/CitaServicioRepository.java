package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.CitaServicio;
import org.nhernandez.webapp.sistemaventas.models.EstadoCita;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaServicioRepository {

    List<CitaServicio> listarPorTenantEnRango(String tenantOwner,
                                              LocalDateTime desde,
                                              LocalDateTime hastaExclusivo) throws SQLException;

    Optional<CitaServicio> porIdPorTenant(Long id, String tenantOwner) throws SQLException;

    void guardar(CitaServicio cita) throws SQLException;

    void actualizar(CitaServicio cita) throws SQLException;

    void actualizarEstado(Long id, String tenantOwner, EstadoCita estado, Long ticketId) throws SQLException;
}
