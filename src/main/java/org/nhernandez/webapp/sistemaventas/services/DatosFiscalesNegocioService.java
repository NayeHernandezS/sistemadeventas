package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;

import java.util.Optional;

public interface DatosFiscalesNegocioService {

    Optional<DatosFiscalesNegocio> consultar(String tenantUsername);

    void guardar(String tenantUsername, DatosFiscalesNegocio datos);
}
