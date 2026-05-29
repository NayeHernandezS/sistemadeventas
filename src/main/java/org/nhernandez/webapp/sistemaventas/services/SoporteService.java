package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.SolicitudSoporte;

import java.util.List;

public interface SoporteService {

    void enviarSolicitud(String tenantOwner, String username, String emailContacto,
                         String asunto, String mensaje);

    List<SolicitudSoporte> historialTenant(String tenantOwner);

    List<SolicitudSoporte> listarAbiertasPlataforma();

    void marcarAtendida(Long id);
}
