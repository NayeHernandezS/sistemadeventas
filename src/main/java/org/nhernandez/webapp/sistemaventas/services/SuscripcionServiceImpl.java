package org.nhernandez.webapp.sistemaventas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.repositories.PagoSuscripcionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.SuscripcionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SuscripcionServiceImpl implements SuscripcionService {

    private static final int MESES_GRATIS_DEFAULT = 1;

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private PagoSuscripcionRepository pagoRepository;

    @Value("${suscripcion.meses.gratis:1}")
    private int mesesGratis;

    @Override
    public void iniciarMesGratis(String username, String planCodigo) {
        try {
            if (suscripcionRepository.porUsername(username) != null) {
                return;
            }
            PlanSuscripcion plan = PlanSuscripcion.porCodigoODefault(planCodigo);
            LocalDateTime inicio = LocalDateTime.now();
            int meses = mesesGratis > 0 ? mesesGratis : MESES_GRATIS_DEFAULT;
            Suscripcion suscripcion = new Suscripcion();
            suscripcion.setUsername(username);
            suscripcion.setFechaInicio(inicio);
            suscripcion.setFechaFin(inicio.plusMonths(meses));
            suscripcion.setEnPeriodoPrueba(true);
            suscripcion.setEstado("ACTIVA");
            suscripcion.setPlanCodigo(plan.getCodigo());
            suscripcionRepository.guardar(suscripcion);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public List<PlanSuscripcion> planesDisponibles() {
        return PlanSuscripcion.todos();
    }

    @Override
    public Optional<Suscripcion> consultar(String username) {
        try {
            return Optional.ofNullable(suscripcionRepository.porUsername(username));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public boolean tieneAccesoActivo(String username) {
        return consultar(username).map(Suscripcion::estaVigente).orElse(false);
    }

    @Override
    public BigDecimal precioPorMes(String planCodigo) {
        return PlanSuscripcion.porCodigoODefault(planCodigo).getPrecioMensual();
    }

    @Override
    public BigDecimal calcularMonto(String planCodigo, int meses) {
        return precioPorMes(planCodigo).multiply(BigDecimal.valueOf(meses)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void solicitarPago(String username, int meses, String planCodigo) {
        if (meses < 1 || meses > 24) {
            throw new ServiceJdbcException("Los meses deben estar entre 1 y 24", null);
        }
        PlanSuscripcion plan = PlanSuscripcion.porCodigo(planCodigo)
                .orElseThrow(() -> new ServiceJdbcException("Plan no valido", null));
        try {
            List<PagoSuscripcion> pendientes = pagoRepository.listarPorUsername(username).stream()
                    .filter(p -> "PENDIENTE".equals(p.getEstado()))
                    .toList();
            if (!pendientes.isEmpty()) {
                throw new ServiceJdbcException("Ya tienes un pago pendiente de confirmacion", null);
            }

            PagoSuscripcion pago = new PagoSuscripcion();
            pago.setUsername(username);
            pago.setMeses(meses);
            pago.setPlanCodigo(plan.getCodigo());
            pago.setMonto(calcularMonto(plan.getCodigo(), meses));
            pago.setFechaSolicitud(LocalDateTime.now());
            pago.setEstado("PENDIENTE");
            pagoRepository.guardar(pago);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public List<PagoSuscripcion> pagosDelUsuario(String username) {
        try {
            return pagoRepository.listarPorUsername(username);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public List<PagoSuscripcion> pagosPendientes() {
        try {
            return pagoRepository.listarPendientes();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public List<PagoSuscripcion> pagosPendientesDelTenant(String tenantOwner) {
        try {
            return pagoRepository.listarPendientesPorUsername(tenantOwner);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void confirmarPagoPlataforma(Long pagoId) {
        try {
            PagoSuscripcion pago = pagoRepository.porId(pagoId);
            if (pago == null || !"PENDIENTE".equals(pago.getEstado())) {
                throw new ServiceJdbcException("Pago no valido para confirmar", null);
            }
            aplicarConfirmacionPago(pagoId, pago);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void extenderSuscripcionMeses(String username, int meses) {
        if (meses < 1 || meses > 24) {
            throw new ServiceJdbcException("Los meses deben estar entre 1 y 24", null);
        }
        try {
            String plan = consultar(username).map(Suscripcion::getPlanCodigo).orElse("EMPRENDEDOR");
            extenderVigenciaPorMeses(username, meses, false, plan);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private void aplicarConfirmacionPago(Long pagoId, PagoSuscripcion pago) throws SQLException {
        pagoRepository.confirmar(pagoId);
        String plan = PlanSuscripcion.porCodigoODefault(pago.getPlanCodigo()).getCodigo();
        extenderVigenciaPorMeses(pago.getUsername(), pago.getMeses(), false, plan);
    }

    private void extenderVigenciaPorMeses(String username, int meses, boolean enPeriodoPrueba, String planCodigo)
            throws SQLException {
        Suscripcion actual = suscripcionRepository.porUsername(username);
        LocalDateTime base = LocalDateTime.now();
        if (actual != null && actual.getFechaFin() != null && actual.getFechaFin().isAfter(base)) {
            base = actual.getFechaFin();
        }
        LocalDateTime nuevaFin = base.plusMonths(meses);
        if (actual == null) {
            Suscripcion nueva = new Suscripcion();
            nueva.setUsername(username);
            nueva.setFechaInicio(LocalDateTime.now());
            nueva.setFechaFin(nuevaFin);
            nueva.setEnPeriodoPrueba(enPeriodoPrueba);
            nueva.setEstado("ACTIVA");
            nueva.setPlanCodigo(planCodigo);
            suscripcionRepository.guardar(nueva);
        } else {
            suscripcionRepository.extenderVigencia(username, nuevaFin, enPeriodoPrueba);
            suscripcionRepository.actualizarPlan(username, planCodigo, enPeriodoPrueba);
        }
    }
}
