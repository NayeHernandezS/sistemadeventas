package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PreferenciasTenant;
import org.nhernandez.webapp.sistemaventas.repositories.PreferenciasTenantRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
public class PreferenciasTenantServiceImpl implements PreferenciasTenantService {

    private final PreferenciasTenantRepository repository;

    public PreferenciasTenantServiceImpl(PreferenciasTenantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PreferenciasTenant> consultar(String tenantUsername) {
        try {
            return Optional.ofNullable(repository.porTenant(tenantUsername));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void guardarStockMinimo(String tenantUsername, Integer stockMinimo) {
        if (tenantUsername == null || tenantUsername.isBlank()) {
            throw new ServiceJdbcException("Cuenta de negocio no valida", null);
        }
        if (stockMinimo != null && (stockMinimo < 1 || stockMinimo > 999)) {
            throw new ServiceJdbcException("El umbral de stock debe estar entre 1 y 999 unidades", null);
        }
        PreferenciasTenant pref = new PreferenciasTenant();
        pref.setTenantUsername(tenantUsername.trim());
        pref.setStockMinimo(stockMinimo);
        try {
            repository.guardar(pref);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public int resolverStockMinimo(String tenantUsername, int valorGlobalPorDefecto) {
        if (tenantUsername == null || tenantUsername.isBlank()) {
            return valorGlobalPorDefecto;
        }
        return consultar(tenantUsername)
                .map(PreferenciasTenant::getStockMinimo)
                .filter(v -> v != null && v > 0)
                .orElse(valorGlobalPorDefecto);
    }

    @Override
    public boolean tieneLogo(String tenantUsername) {
        if (tenantUsername == null || tenantUsername.isBlank()) {
            return false;
        }
        return consultar(tenantUsername)
                .map(PreferenciasTenant::getLogoFilename)
                .filter(f -> f != null && !f.isBlank())
                .isPresent();
    }

    @Override
    public void guardarLogoFilename(String tenantUsername, String logoFilename) {
        if (tenantUsername == null || tenantUsername.isBlank()) {
            throw new ServiceJdbcException("Cuenta de negocio no valida", null);
        }
        if (logoFilename == null || logoFilename.isBlank()) {
            throw new ServiceJdbcException("Nombre de logo no valido", null);
        }
        try {
            repository.actualizarLogoFilename(tenantUsername.trim(), logoFilename.trim());
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void eliminarLogo(String tenantUsername) {
        if (tenantUsername == null || tenantUsername.isBlank()) {
            throw new ServiceJdbcException("Cuenta de negocio no valida", null);
        }
        try {
            repository.eliminarLogoFilename(tenantUsername.trim());
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }
}
