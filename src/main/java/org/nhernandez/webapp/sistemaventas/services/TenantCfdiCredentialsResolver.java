package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.cfdi.CfdiCredentials;
import org.nhernandez.webapp.sistemaventas.config.CfdiProperties;
import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;
import org.nhernandez.webapp.sistemaventas.repositories.DatosFiscalesNegocioRepository;
import org.nhernandez.webapp.sistemaventas.util.CfdiSecretCipher;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
public class TenantCfdiCredentialsResolver {

    private final CfdiProperties cfdiProperties;
    private final DatosFiscalesNegocioRepository datosFiscalesRepository;

    public TenantCfdiCredentialsResolver(CfdiProperties cfdiProperties,
                                         DatosFiscalesNegocioRepository datosFiscalesRepository) {
        this.cfdiProperties = cfdiProperties;
        this.datosFiscalesRepository = datosFiscalesRepository;
    }

    public boolean disponible(String tenantOwner) {
        return resolver(tenantOwner).isPresent();
    }

    public boolean usaCredencialesTenant(String tenantOwner) {
        try {
            DatosFiscalesNegocio datos = datosFiscalesRepository.porTenant(tenantOwner);
            return datos != null && datos.tieneFacturamaConfigurado() && datos.isCfdiHabilitado();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    public Optional<CfdiCredentials> resolver(String tenantOwner) {
        Optional<CfdiCredentials> tenant = credencialesTenant(tenantOwner);
        if (tenant.isPresent()) {
            return tenant;
        }
        if (cfdiProperties.habilitado()) {
            return Optional.of(new CfdiCredentials(
                    cfdiProperties.getFacturamaUsername(),
                    cfdiProperties.getFacturamaPassword(),
                    cfdiProperties.isFacturamaSandbox()));
        }
        return Optional.empty();
    }

    private Optional<CfdiCredentials> credencialesTenant(String tenantOwner) {
        if (tenantOwner == null || tenantOwner.isBlank()) {
            return Optional.empty();
        }
        try {
            DatosFiscalesNegocio datos = datosFiscalesRepository.porTenant(tenantOwner.trim());
            if (datos == null || !datos.isCfdiHabilitado() || !datos.tieneFacturamaConfigurado()) {
                return Optional.empty();
            }
            String password = CfdiSecretCipher.descifrar(
                    datos.getFacturamaPasswordEnc(),
                    cfdiProperties.getEncryptionKey());
            CfdiCredentials credenciales = new CfdiCredentials(
                    datos.getFacturamaUsername(),
                    password,
                    datos.isFacturamaSandbox());
            return credenciales.validas() ? Optional.of(credenciales) : Optional.empty();
        } catch (IllegalStateException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }
}
