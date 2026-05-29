package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.SolicitudSoporte;
import org.nhernandez.webapp.sistemaventas.repositories.SolicitudSoporteRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SoporteServiceImpl implements SoporteService {

    private final SolicitudSoporteRepository solicitudSoporteRepository;

    public SoporteServiceImpl(SolicitudSoporteRepository solicitudSoporteRepository) {
        this.solicitudSoporteRepository = solicitudSoporteRepository;
    }

    @Override
    public void enviarSolicitud(String tenantOwner, String username, String emailContacto,
                                String asunto, String mensaje) {
        if (asunto == null || asunto.isBlank()) {
            throw new ServiceJdbcException("El asunto es obligatorio", null);
        }
        if (mensaje == null || mensaje.isBlank()) {
            throw new ServiceJdbcException("El mensaje es obligatorio", null);
        }
        if (asunto.length() > 120) {
            throw new ServiceJdbcException("El asunto no puede superar 120 caracteres", null);
        }
        SolicitudSoporte s = new SolicitudSoporte();
        s.setTenantOwner(tenantOwner);
        s.setUsername(username);
        s.setEmailContacto(emailContacto != null && !emailContacto.isBlank() ? emailContacto.trim() : null);
        s.setAsunto(asunto.trim());
        s.setMensaje(mensaje.trim());
        s.setFechaSolicitud(LocalDateTime.now());
        s.setEstado("ABIERTA");
        try {
            solicitudSoporteRepository.guardar(s);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public List<SolicitudSoporte> historialTenant(String tenantOwner) {
        try {
            return solicitudSoporteRepository.listarPorTenant(tenantOwner);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public List<SolicitudSoporte> listarAbiertasPlataforma() {
        try {
            return solicitudSoporteRepository.listarAbiertas();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void marcarAtendida(Long id) {
        try {
            solicitudSoporteRepository.marcarAtendida(id);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }
}
