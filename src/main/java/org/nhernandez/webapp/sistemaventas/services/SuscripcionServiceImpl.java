package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoApiClient;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoCheckoutService;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoException;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoUrls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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

    @Autowired
    private MercadoPagoApiClient mercadoPagoApiClient;

    @Autowired
    @Lazy
    private PlanLimiteService planLimiteService;

    @Value("${suscripcion.meses.gratis:1}")
    private int mesesGratis;

    @Value("${mercadopago.currency-id:MXN}")
    private String monedaMercadoPago;

    @Value("${app.base-url:}")
    private String appBaseUrl;

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
        return consultar(username).map(Suscripcion::permiteAcceso).orElse(false);
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
        PlanSuscripcion plan = validarSolicitudPago(username, meses, planCodigo);
        try {
            PagoSuscripcion pago = new PagoSuscripcion();
            pago.setUsername(username);
            pago.setMeses(meses);
            pago.setPlanCodigo(plan.getCodigo());
            pago.setMonto(calcularMonto(plan.getCodigo(), meses));
            pago.setFechaSolicitud(LocalDateTime.now());
            pago.setEstado("PENDIENTE");
            pago.setCanal("MANUAL");
            pagoRepository.guardar(pago);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private PlanSuscripcion validarSolicitudPago(String username, int meses, String planCodigo) {
        if (meses < 1 || meses > 24) {
            throw new ServiceJdbcException("Los meses deben estar entre 1 y 24", null);
        }
        PlanSuscripcion plan = PlanSuscripcion.porCodigo(planCodigo)
                .orElseThrow(() -> new ServiceJdbcException("Plan no valido", null));
        planLimiteService.validarPlanContratable(username, plan.getCodigo());
        try {
            List<PagoSuscripcion> pendientes = pagoRepository.listarPorUsername(username).stream()
                    .filter(p -> "PENDIENTE".equals(p.getEstado()))
                    .toList();
            if (!pendientes.isEmpty()) {
                throw new ServiceJdbcException("Ya tienes un pago pendiente de confirmacion", null);
            }
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
        return plan;
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
    public List<PagoSuscripcion> pagosExpirados() {
        try {
            return pagoRepository.listarPorEstado("EXPIRADO");
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void expirarPagoPlataforma(Long pagoId) {
        try {
            if (pagoRepository.expirarPorId(pagoId) == 0) {
                throw new ServiceJdbcException("Pago no encontrado o ya no esta pendiente", null);
            }
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void cancelarPagoPendienteDelTenant(String tenantOwner, Long pagoId) {
        try {
            PagoSuscripcion pago = pagoRepository.porId(pagoId);
            if (pago == null || !tenantOwner.equals(pago.getUsername())) {
                throw new ServiceJdbcException("Pago no encontrado", null);
            }
            if (!"PENDIENTE".equals(pago.getEstado())) {
                throw new ServiceJdbcException("Solo puedes cancelar pagos pendientes", null);
            }
            if (pagoRepository.expirarPorId(pagoId) == 0) {
                throw new ServiceJdbcException("No se pudo cancelar el pago", null);
            }
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
            pagoRepository.confirmar(pagoId);
            aplicarExtensionSuscripcion(pago);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public String iniciarPagoMercadoPago(String username, int meses, String planCodigo, String baseUrlPublica) {
        PlanSuscripcion plan = validarSolicitudPago(username, meses, planCodigo);
        try {
            PagoSuscripcion pago = new PagoSuscripcion();
            pago.setUsername(username);
            pago.setMeses(meses);
            pago.setPlanCodigo(plan.getCodigo());
            pago.setMonto(calcularMonto(plan.getCodigo(), meses));
            pago.setFechaSolicitud(LocalDateTime.now());
            pago.setEstado("PENDIENTE");
            pago.setCanal("MERCADOPAGO");
            pagoRepository.guardar(pago);

            if (pago.getId() == null) {
                throw new ServiceJdbcException("No se pudo registrar el pago", null);
            }

            String base = MercadoPagoUrls.resolverBase(appBaseUrl, baseUrlPublica);
            String ref = MercadoPagoCheckoutService.REF_PREFIX + pago.getId();
            String titulo = plan.getNombre() + " - " + meses + " mes(es)";

            MercadoPagoApiClient.PreferenciaCreada preferencia = mercadoPagoApiClient.crearPreferencia(
                    new MercadoPagoApiClient.PreferenciaRequest(
                            titulo,
                            pago.getMonto(),
                            ref,
                            MercadoPagoUrls.urlNotificacionOpcional(appBaseUrl, baseUrlPublica),
                            base + "/suscripcion/pago-exitoso",
                            base + "/suscripcion/pago-fallido",
                            base + "/suscripcion/pago-pendiente"
                    ));
            pagoRepository.actualizarReferenciaMercadoPago(pago.getId(), preferencia.preferenceId());
            return preferencia.initPoint();
        } catch (MercadoPagoException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void confirmarPagoMercadoPago(Long pagoId, String mpPaymentId, BigDecimal montoRecibido, String moneda) {
        try {
            PagoSuscripcion pago = pagoRepository.porId(pagoId);
            if (pago == null) {
                throw new ServiceJdbcException("Pago no encontrado", null);
            }
            if (!"PENDIENTE".equals(pago.getEstado())) {
                return;
            }
            if (moneda != null && !moneda.isBlank()
                    && !monedaMercadoPago.equalsIgnoreCase(moneda.trim())) {
                throw new ServiceJdbcException("Moneda de pago no valida", null);
            }
            if (!MercadoPagoCheckoutService.montosCoinciden(pago.getMonto(), montoRecibido)) {
                throw new ServiceJdbcException("El monto del pago no coincide con la solicitud", null);
            }
            pagoRepository.confirmarMercadoPago(pagoId, mpPaymentId);
            aplicarExtensionSuscripcion(pago);
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

    @Override
    public void suspenderCuenta(String username) {
        validarCuentaAdmin(username);
        try {
            if (suscripcionRepository.porUsername(username) == null) {
                throw new ServiceJdbcException("La cuenta no tiene suscripcion registrada", null);
            }
            suscripcionRepository.actualizarEstado(username, "SUSPENDIDA");
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void reactivarCuenta(String username) {
        validarCuentaAdmin(username);
        try {
            if (suscripcionRepository.porUsername(username) == null) {
                throw new ServiceJdbcException("La cuenta no tiene suscripcion registrada", null);
            }
            suscripcionRepository.actualizarEstado(username, "ACTIVA");
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void cambiarPlanPlataforma(String username, String planCodigo) {
        validarCuentaAdmin(username);
        PlanSuscripcion plan = PlanSuscripcion.porCodigo(planCodigo)
                .orElseThrow(() -> new ServiceJdbcException("Plan no valido", null));
        planLimiteService.validarPlanContratable(username, plan.getCodigo());
        try {
            if (suscripcionRepository.porUsername(username) == null) {
                throw new ServiceJdbcException("La cuenta no tiene suscripcion registrada", null);
            }
            suscripcionRepository.actualizarPlan(username, plan.getCodigo(), false);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private static void validarCuentaAdmin(String username) {
        if (username == null || username.isBlank()) {
            throw new ServiceJdbcException("Cuenta no valida", null);
        }
    }

    private void aplicarExtensionSuscripcion(PagoSuscripcion pago) throws SQLException {
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
