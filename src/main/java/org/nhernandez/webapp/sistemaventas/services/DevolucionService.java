package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Devolucion;
import org.nhernandez.webapp.sistemaventas.models.LineaDevolucionVista;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;

import java.util.List;
import java.util.Map;

public interface DevolucionService {

    List<Devolucion> listarPorTenant(String tenantOwner);

    TicketVenta obtenerTicketParaDevolucion(Long ticketId, String tenantOwner);

    List<LineaDevolucionVista> lineasDisponibles(TicketVenta ticket);

    Devolucion registrarDevolucion(Long ticketId, String tenantOwner, String username,
                                   Map<Long, Integer> cantidadesPorProducto, String motivo);
}
