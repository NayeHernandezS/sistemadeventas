package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteService {

    List<Cliente> listarActivos(String tenantOwner);

    Optional<Cliente> porId(String tenantOwner, Long id);

    void guardar(String tenantOwner, Cliente cliente);

    void desactivar(String tenantOwner, Long id);
}
