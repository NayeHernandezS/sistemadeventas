package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PlanContratibilidad;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;

import java.util.Map;

public interface PlanLimiteService {

    PlanSuscripcion planActivo(String tenantOwner);

    int contarVendedores(String tenantOwner);

    int contarProductos(String tenantOwner);

    void validarNuevoVendedor(String tenantOwner);

    void validarNuevoProducto(String tenantOwner);

    /**
     * Impide contratar un plan cuyos limites son menores al uso actual del tenant.
     */
    void validarPlanContratable(String tenantOwner, String planCodigo);

    PlanContratibilidad evaluarPlanContratable(String tenantOwner, String planCodigo);

    Map<String, PlanContratibilidad> evaluarPlanesContratables(String tenantOwner);
}
