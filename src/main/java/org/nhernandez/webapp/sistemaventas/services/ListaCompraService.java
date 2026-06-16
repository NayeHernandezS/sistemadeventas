package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ListaCompraHoy;

public interface ListaCompraService {

    ListaCompraHoy generar(String tenantOwner);

    ListaCompraHoy generar(String tenantOwner, Integer limiteVista);
}
