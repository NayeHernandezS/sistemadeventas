package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Cliente;
import org.nhernandez.webapp.sistemaventas.repositories.ClienteRepository;
import org.nhernandez.webapp.sistemaventas.util.FacturaDatosUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;

    @Autowired
    public ClienteServiceImpl(ClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Cliente> listarActivos(String tenantOwner) {
        try {
            return repository.listarActivosPorTenant(tenantOwner);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Cliente> porId(String tenantOwner, Long id) {
        try {
            return Optional.ofNullable(repository.porIdPorTenant(id, tenantOwner));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void guardar(String tenantOwner, Cliente cliente) {
        validar(tenantOwner, cliente);
        normalizar(cliente);
        cliente.setTenantOwner(tenantOwner);
        try {
            repository.guardar(cliente);
        } catch (SQLException e) {
            throw new ServiceJdbcException(mensajeAmigable(e), e);
        }
    }

    @Override
    public void desactivar(String tenantOwner, Long id) {
        try {
            repository.desactivarPorTenant(id, tenantOwner);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private static void validar(String tenantOwner, Cliente cliente) {
        if (tenantOwner == null || tenantOwner.isBlank()) {
            throw new ServiceJdbcException("Cuenta de negocio no identificada.", null);
        }
        if (cliente == null) {
            throw new ServiceJdbcException("Datos del cliente no recibidos.", null);
        }
        String nombre = cliente.getNombre() != null ? cliente.getNombre().trim() : "";
        if (nombre.isBlank()) {
            throw new ServiceJdbcException("Indica el nombre del cliente.", null);
        }
        if (nombre.length() > 200) {
            throw new ServiceJdbcException("El nombre no puede superar 200 caracteres.", null);
        }
        String rfc = FacturaDatosUtil.normalizarRfc(cliente.getRfc());
        if (!rfc.isBlank()) {
            String errorRfc = FacturaDatosUtil.validarRfcObligatorio(rfc);
            if (errorRfc != null) {
                throw new ServiceJdbcException(errorRfc, null);
            }
        }
        String cp = cliente.getCodigoPostal() != null ? cliente.getCodigoPostal().trim() : "";
        if (!cp.isBlank() && !cp.matches("\\d{5}")) {
            throw new ServiceJdbcException("Codigo postal invalido (5 digitos).", null);
        }
        String uso = cliente.getUsoCfdi() != null ? cliente.getUsoCfdi().trim() : "";
        if (!uso.isBlank() && uso.length() > 10) {
            throw new ServiceJdbcException("Uso CFDI invalido.", null);
        }
        String email = cliente.getEmail() != null ? cliente.getEmail().trim() : "";
        if (!email.isBlank() && email.length() > 150) {
            throw new ServiceJdbcException("El correo es demasiado largo.", null);
        }
    }

    private static void normalizar(Cliente cliente) {
        cliente.setNombre(cliente.getNombre().trim());
        String rfc = FacturaDatosUtil.normalizarRfc(cliente.getRfc());
        cliente.setRfc(rfc.isBlank() ? null : rfc);
        String razon = cliente.getRazonSocial() != null ? cliente.getRazonSocial().trim() : "";
        cliente.setRazonSocial(razon.isBlank() ? null : razon);
        String email = cliente.getEmail() != null ? cliente.getEmail().trim() : "";
        cliente.setEmail(email.isBlank() ? null : email);
        String cp = cliente.getCodigoPostal() != null ? cliente.getCodigoPostal().trim() : "";
        cliente.setCodigoPostal(cp.isBlank() ? null : cp);
        String uso = cliente.getUsoCfdi() != null ? cliente.getUsoCfdi().trim().toUpperCase() : "";
        cliente.setUsoCfdi(uso.isBlank() ? null : uso);
    }

    private static String mensajeAmigable(SQLException e) {
        String msg = e.getMessage();
        if (msg != null && (msg.contains("uk_clientes_tenant_rfc") || msg.contains("Duplicate entry"))) {
            return "Ya existe un cliente con ese RFC en tu cuenta.";
        }
        return msg != null ? msg : "Error al guardar el cliente";
    }
}
