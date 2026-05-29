package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;

public interface PlanLimiteService {

    PlanSuscripcion planActivo(String tenantOwner);

    int contarVendedores(String tenantOwner);

    int contarProductos(String tenantOwner);

    void validarNuevoVendedor(String tenantOwner);

    void validarNuevoProducto(String tenantOwner);
}
