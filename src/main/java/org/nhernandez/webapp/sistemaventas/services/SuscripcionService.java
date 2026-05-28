package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SuscripcionService {

    void iniciarMesGratis(String username);

    Optional<Suscripcion> consultar(String username);

    boolean tieneAccesoActivo(String username);

    BigDecimal precioPorMes();

    BigDecimal calcularMonto(int meses);

    void solicitarPago(String username, int meses);

    List<PagoSuscripcion> pagosDelUsuario(String username);

    List<PagoSuscripcion> pagosPendientes();

    List<PagoSuscripcion> pagosPendientesDelTenant(String tenantOwner);

    void confirmarPago(Long pagoId, String tenantOwner);
}
