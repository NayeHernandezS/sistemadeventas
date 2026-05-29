package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class PlanLimiteServiceImpl implements PlanLimiteService {

    private final SuscripcionService suscripcionService;
    private final UsuarioReposository usuarioRepository;
    private final ProductoRepository productoRepository;

    public PlanLimiteServiceImpl(SuscripcionService suscripcionService,
                                 UsuarioReposository usuarioRepository,
                                 ProductoRepository productoRepository) {
        this.suscripcionService = suscripcionService;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public PlanSuscripcion planActivo(String tenantOwner) {
        return suscripcionService.consultar(tenantOwner)
                .map(Suscripcion::getPlanCodigo)
                .map(PlanSuscripcion::porCodigoODefault)
                .orElse(PlanSuscripcion.EMPRENDEDOR);
    }

    @Override
    public int contarVendedores(String tenantOwner) {
        try {
            return usuarioRepository.listarPorAdminOwner(tenantOwner).size();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public int contarProductos(String tenantOwner) {
        try {
            return productoRepository.contarPorOwner(tenantOwner);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void validarNuevoVendedor(String tenantOwner) {
        PlanSuscripcion plan = planActivo(tenantOwner);
        int actual = contarVendedores(tenantOwner);
        if (actual >= plan.getMaxVendedores()) {
            throw new ServiceJdbcException(
                    "Tu plan " + plan.getNombre() + " permite hasta " + plan.getMaxVendedores()
                            + " vendedores. Mejora tu plan en Suscripcion.", null);
        }
    }

    @Override
    public void validarNuevoProducto(String tenantOwner) {
        PlanSuscripcion plan = planActivo(tenantOwner);
        int actual = contarProductos(tenantOwner);
        if (actual >= plan.getMaxProductos()) {
            throw new ServiceJdbcException(
                    "Tu plan " + plan.getNombre() + " permite hasta " + plan.getMaxProductos()
                            + " productos. Mejora tu plan en Suscripcion.", null);
        }
    }
}
