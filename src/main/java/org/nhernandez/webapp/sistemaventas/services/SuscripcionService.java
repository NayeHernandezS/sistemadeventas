package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;

import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SuscripcionService {

    void iniciarMesGratis(String username, String planCodigo);

    /** Activa el periodo de prueba inicial tras crear la cuenta (solo si aun no hay suscripcion). */
    void activarPeriodoPruebaInicial(String username, String planCodigo);

    Optional<Suscripcion> consultar(String username);

    boolean tieneAccesoActivo(String username);

    BigDecimal precioPorMes(String planCodigo);

    BigDecimal calcularMonto(String planCodigo, int meses);

    void solicitarPago(String username, int meses, String planCodigo);

    List<PlanSuscripcion> planesDisponibles();

    List<PagoSuscripcion> pagosDelUsuario(String username);

    List<PagoSuscripcion> pagosPendientes();

    List<PagoSuscripcion> pagosPendientesDelTenant(String tenantOwner);

    List<PagoSuscripcion> pagosExpirados();

    void confirmarPagoPlataforma(Long pagoId);

    void expirarPagoPlataforma(Long pagoId);

    void cancelarPagoPendienteDelTenant(String tenantOwner, Long pagoId);

    /**
     * Crea pago pendiente y preferencia Checkout Pro; devuelve URL init_point de Mercado Pago.
     */
    String iniciarPagoMercadoPago(String username, int meses, String planCodigo, String baseUrlPublica);

    /**
     * Confirma suscripcion tras pago aprobado en Mercado Pago (idempotente si ya estaba confirmado).
     */
    void confirmarPagoMercadoPago(Long pagoId, String mpPaymentId, java.math.BigDecimal montoRecibido,
                                  String moneda);

    void extenderSuscripcionMeses(String username, int meses);

    void suspenderCuenta(String username);

    void reactivarCuenta(String username);

    void cambiarPlanPlataforma(String username, String planCodigo);
}
