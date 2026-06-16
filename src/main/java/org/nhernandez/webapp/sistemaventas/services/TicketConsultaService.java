package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.util.List;
import java.util.Optional;

public interface TicketConsultaService {

    List<TicketVenta> listar(String tenantOwner, Optional<String> usernameVendedor, String textoBusqueda);

    List<TicketVenta> historialPorNombreCliente(String tenantOwner, String nombreCliente, int limite);
}
