package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PreferenciasTenant;

import java.util.Optional;

public interface PreferenciasTenantService {

    Optional<PreferenciasTenant> consultar(String tenantUsername);

    void guardarStockMinimo(String tenantUsername, Integer stockMinimo);

    int resolverStockMinimo(String tenantUsername, int valorGlobalPorDefecto);

    boolean tieneLogo(String tenantUsername);

    void guardarLogoFilename(String tenantUsername, String logoFilename);

    void eliminarLogo(String tenantUsername);

    boolean onboardingCompletado(String tenantUsername);

    void iniciarOnboarding(String tenantUsername);

    void marcarOnboardingCompletado(String tenantUsername);
}
