package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.CitaServicio;
import org.nhernandez.webapp.sistemaventas.models.EstadoCita;
import org.nhernandez.webapp.sistemaventas.models.Producto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CitaServicioService {

    List<CitaServicio> listarDelDia(String tenantOwner, LocalDate fecha);

    List<Producto> listarServicios(String tenantOwner);

    Optional<CitaServicio> porId(String tenantOwner, Long id);

    Map<String, String> validar(CitaServicio cita, boolean esEdicion);

    CitaServicio guardar(String tenantOwner, String username, CitaServicio cita);

    void cancelar(String tenantOwner, Long id);

    void confirmar(String tenantOwner, Long id);

    void marcarCompletada(String tenantOwner, Long id);
}
