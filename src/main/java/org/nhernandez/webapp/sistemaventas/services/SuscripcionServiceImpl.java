package org.nhernandez.webapp.sistemaventas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.repositories.PagoSuscripcionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.SuscripcionRepository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
public class SuscripcionServiceImpl implements SuscripcionService {

    private static final int MESES_GRATIS_DEFAULT = 1;

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private PagoSuscripcionRepository pagoRepository;

    private final Properties config = loadConfig();

    @Override
    public void iniciarMesGratis(String username) {
        try {
            if (suscripcionRepository.porUsername(username) != null) {
                return;
            }
            LocalDateTime inicio = LocalDateTime.now();
            int mesesGratis = mesesGratisConfig();
            Suscripcion suscripcion = new Suscripcion();
            suscripcion.setUsername(username);
            suscripcion.setFechaInicio(inicio);
            suscripcion.setFechaFin(inicio.plusMonths(mesesGratis));
            suscripcion.setEnPeriodoPrueba(true);
            suscripcion.setEstado("ACTIVA");
            suscripcionRepository.guardar(suscripcion);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
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
    public BigDecimal precioPorMes() {
        String valor = config.getProperty("suscripcion.precio.mes", "199.00");
        return new BigDecimal(valor);
    }

    @Override
    public BigDecimal calcularMonto(int meses) {
        return precioPorMes().multiply(BigDecimal.valueOf(meses)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void solicitarPago(String username, int meses) {
        if (meses < 1 || meses > 24) {
            throw new ServiceJdbcException("Los meses deben estar entre 1 y 24", null);
        }
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
            pago.setMonto(calcularMonto(meses));
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
    public void confirmarPago(Long pagoId, String tenantOwner) {
        try {
            PagoSuscripcion pago = pagoRepository.porId(pagoId);
            if (pago == null || !"PENDIENTE".equals(pago.getEstado())) {
                throw new ServiceJdbcException("Pago no valido para confirmar", null);
            }
            if (!tenantOwner.equals(pago.getUsername())) {
                throw new ServiceJdbcException("No puedes confirmar pagos de otra cuenta", null);
            }
            pagoRepository.confirmar(pagoId);

            Suscripcion actual = suscripcionRepository.porUsername(pago.getUsername());
            LocalDateTime base = LocalDateTime.now();
            if (actual != null && actual.getFechaFin().isAfter(base)) {
                base = actual.getFechaFin();
            }
            LocalDateTime nuevaFin = base.plusMonths(pago.getMeses());
            suscripcionRepository.extenderVigencia(pago.getUsername(), nuevaFin, false);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private int mesesGratisConfig() {
        try {
            return Integer.parseInt(config.getProperty("suscripcion.meses.gratis", "1"));
        } catch (NumberFormatException e) {
            return MESES_GRATIS_DEFAULT;
        }
    }

    private Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
        }
        return properties;
    }
}
