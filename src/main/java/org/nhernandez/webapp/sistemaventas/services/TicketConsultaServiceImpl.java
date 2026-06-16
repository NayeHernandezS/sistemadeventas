package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class TicketConsultaServiceImpl implements TicketConsultaService {

    static final int LIMITE_BUSQUEDA = 200;
    static final int LIMITE_HISTORIAL_CLIENTE = 20;

    private final TicketRepository ticketRepository;

    @Autowired
    public TicketConsultaServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<TicketVenta> listar(String tenantOwner, Optional<String> usernameVendedor, String textoBusqueda) {
        try {
            String texto = textoBusqueda != null ? textoBusqueda.trim() : "";
            if (texto.isBlank()) {
                if (usernameVendedor.isPresent()) {
                    return ticketRepository.listarPorVendedor(usernameVendedor.get());
                }
                return ticketRepository.listarPorTenant(tenantOwner);
            }
            if (usernameVendedor.isPresent()) {
                return ticketRepository.buscarPorVendedor(usernameVendedor.get(), texto, LIMITE_BUSQUEDA);
            }
            return ticketRepository.buscarPorTenant(tenantOwner, texto, LIMITE_BUSQUEDA);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public List<TicketVenta> historialPorNombreCliente(String tenantOwner, String nombreCliente, int limite) {
        String nombre = nombreCliente != null ? nombreCliente.trim() : "";
        if (nombre.isBlank()) {
            return List.of();
        }
        int max = limite > 0 ? limite : LIMITE_HISTORIAL_CLIENTE;
        try {
            return ticketRepository.listarPorTenantYNombreCliente(tenantOwner, nombre, max);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }
}
