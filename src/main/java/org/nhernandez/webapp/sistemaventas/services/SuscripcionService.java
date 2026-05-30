package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;

import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SuscripcionService {

    void iniciarMesGratis(String username, String planCodigo);

    Optional<Suscripcion> consultar(String username);

    boolean tieneAccesoActivo(String username);

    BigDecimal precioPorMes(String planCodigo);

    BigDecimal calcularMonto(String planCodigo, int meses);

    void solicitarPago(String username, int meses, String planCodigo);

    List<PlanSuscripcion> planesDisponibles();

    List<PagoSuscripcion> pagosDelUsuario(String username);

    List<PagoSuscripcion> pagosPendientes();

    List<PagoSuscripcion> pagosPendientesDelTenant(String tenantOwner);

    void confirmarPagoPlataforma(Long pagoId);

    void extenderSuscripcionMeses(String username, int meses);
}
