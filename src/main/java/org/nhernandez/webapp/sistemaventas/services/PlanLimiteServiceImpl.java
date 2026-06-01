package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PlanContratibilidad;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public void validarPlanContratable(String tenantOwner, String planCodigo) {
        PlanContratibilidad evaluacion = evaluarPlanContratable(tenantOwner, planCodigo);
        if (!evaluacion.isContratable()) {
            throw new ServiceJdbcException(String.join(" ", evaluacion.getMotivos()), null);
        }
    }

    @Override
    public PlanContratibilidad evaluarPlanContratable(String tenantOwner, String planCodigo) {
        PlanSuscripcion plan = PlanSuscripcion.porCodigo(planCodigo)
                .orElseThrow(() -> new ServiceJdbcException("Plan no valido", null));
        return evaluarContraPlan(tenantOwner, plan);
    }

    @Override
    public Map<String, PlanContratibilidad> evaluarPlanesContratables(String tenantOwner) {
        Map<String, PlanContratibilidad> resultado = new LinkedHashMap<>();
        for (PlanSuscripcion plan : PlanSuscripcion.todos()) {
            resultado.put(plan.getCodigo(), evaluarContraPlan(tenantOwner, plan));
        }
        return resultado;
    }

    private PlanContratibilidad evaluarContraPlan(String tenantOwner, PlanSuscripcion plan) {
        int vendedores = contarVendedores(tenantOwner);
        int productos = contarProductos(tenantOwner);
        List<String> motivos = new ArrayList<>();
        if (vendedores > plan.getMaxVendedores()) {
            int exceso = vendedores - plan.getMaxVendedores();
            motivos.add("Tienes " + vendedores + " vendedores pero " + plan.getNombre()
                    + " permite solo " + plan.getMaxVendedores()
                    + ". Elimina al menos " + exceso + " vendedor(es) en Usuarios.");
        }
        if (productos > plan.getMaxProductos()) {
            int exceso = productos - plan.getMaxProductos();
            motivos.add("Tienes " + productos + " productos pero " + plan.getNombre()
                    + " permite solo " + plan.getMaxProductos()
                    + ". Elimina al menos " + exceso + " producto(s) en Inventario.");
        }
        return new PlanContratibilidad(
                plan.getCodigo(),
                motivos.isEmpty(),
                motivos,
                vendedores,
                productos,
                plan.getMaxVendedores(),
                plan.getMaxProductos());
    }
}
