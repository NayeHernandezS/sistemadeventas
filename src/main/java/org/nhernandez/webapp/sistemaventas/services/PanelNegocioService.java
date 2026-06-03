package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PanelNegocioResumen;

public interface PanelNegocioService {

    PanelNegocioResumen resumenParaAdmin(String tenantOwner, String usernameAdmin);
}
