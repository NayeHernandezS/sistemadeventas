package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.config.CfdiProperties;
import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;
import org.nhernandez.webapp.sistemaventas.repositories.DatosFiscalesNegocioRepository;
import org.nhernandez.webapp.sistemaventas.util.CfdiSecretCipher;
import org.nhernandez.webapp.sistemaventas.util.FacturaDatosUtil;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
public class DatosFiscalesNegocioServiceImpl implements DatosFiscalesNegocioService {

    private final DatosFiscalesNegocioRepository repository;
    private final CfdiProperties cfdiProperties;

    public DatosFiscalesNegocioServiceImpl(DatosFiscalesNegocioRepository repository,
                                           CfdiProperties cfdiProperties) {
        this.repository = repository;
        this.cfdiProperties = cfdiProperties;
    }

    @Override
    public Optional<DatosFiscalesNegocio> consultar(String tenantUsername) {
        try {
            return Optional.ofNullable(repository.porTenant(tenantUsername));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void guardar(String tenantUsername, DatosFiscalesNegocio datos) {
        if (tenantUsername == null || tenantUsername.isBlank()) {
            throw new ServiceJdbcException("Cuenta de negocio no valida", null);
        }
        DatosFiscalesNegocio normalizado = normalizar(datos);
        validar(normalizado);
        normalizado.setTenantUsername(tenantUsername.trim());
        try {
            repository.guardar(normalizado);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void guardarConfiguracionFacturama(String tenantUsername,
                                              String facturamaUsername,
                                              String facturamaPasswordNueva,
                                              boolean facturamaSandbox,
                                              boolean cfdiHabilitado) {
        if (tenantUsername == null || tenantUsername.isBlank()) {
            throw new ServiceJdbcException("Cuenta de negocio no valida", null);
        }
        String usuario = vacioANull(facturamaUsername);
        String passwordNueva = vacioANull(facturamaPasswordNueva);
        if (cfdiHabilitado) {
            if (usuario == null) {
                throw new ServiceJdbcException("Indica el usuario API de Facturama.", null);
            }
        }
        try {
            DatosFiscalesNegocio existente = repository.porTenant(tenantUsername.trim());
            boolean actualizarPassword = passwordNueva != null;
            if (cfdiHabilitado && (existente == null || !existente.tieneFacturamaConfigurado()) && !actualizarPassword) {
                throw new ServiceJdbcException("Indica la contraseña API de Facturama.", null);
            }
            DatosFiscalesNegocio datos = new DatosFiscalesNegocio();
            datos.setTenantUsername(tenantUsername.trim());
            datos.setFacturamaUsername(usuario);
            datos.setFacturamaSandbox(facturamaSandbox);
            datos.setCfdiHabilitado(cfdiHabilitado);
            if (actualizarPassword) {
                datos.setFacturamaPasswordEnc(CfdiSecretCipher.cifrar(passwordNueva, cfdiProperties.getEncryptionKey()));
            }
            repository.guardarConfiguracionFacturama(datos, actualizarPassword);
        } catch (IllegalStateException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private static DatosFiscalesNegocio normalizar(DatosFiscalesNegocio datos) {
        DatosFiscalesNegocio n = new DatosFiscalesNegocio();
        if (datos == null) {
            return n;
        }
        n.setRfc(vacioANull(FacturaDatosUtil.normalizarRfc(datos.getRfc())));
        n.setRazonSocial(vacioANull(datos.getRazonSocial()));
        n.setEmail(vacioANull(datos.getEmail()));
        n.setDireccion(vacioANull(datos.getDireccion()));
        n.setUsoCfdi(vacioANull(datos.getUsoCfdi()));
        n.setCodigoPostal(vacioANull(datos.getCodigoPostal()));
        n.setRegimenFiscal(vacioANull(datos.getRegimenFiscal()));
        return n;
    }

    private static void validar(DatosFiscalesNegocio datos) {
        if (!datos.tieneDatos()) {
            return;
        }
        if (datos.getRfc() != null) {
            String errorRfc = FacturaDatosUtil.validarRfcObligatorio(datos.getRfc());
            if (errorRfc != null) {
                throw new ServiceJdbcException(errorRfc, null);
            }
        }
        if (datos.getRazonSocial() == null && datos.getRfc() != null) {
            throw new ServiceJdbcException("Indica la razon social o nombre del cliente.", null);
        }
        if (datos.getEmail() != null && !datos.getEmail().contains("@")) {
            throw new ServiceJdbcException("Indica un email valido para facturacion.", null);
        }
        if (datos.getCodigoPostal() != null && !datos.getCodigoPostal().matches("\\d{5}")) {
            throw new ServiceJdbcException("El codigo postal del emisor debe tener 5 digitos.", null);
        }
        if (datos.getRegimenFiscal() != null && !datos.getRegimenFiscal().matches("\\d{3}")) {
            throw new ServiceJdbcException("El regimen fiscal del emisor debe ser un codigo SAT de 3 digitos (ej. 601).", null);
        }
    }

    private static String vacioANull(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
