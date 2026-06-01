package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ClienteCuenta;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;

import java.util.List;
import java.util.Optional;

public interface PlataformaService {

    List<ClienteCuenta> listarClientes();

    Optional<ClienteCuenta> buscarCliente(String username);

    List<PagoSuscripcion> pagosDelCliente(String username);

    List<PagoSuscripcion> pagosPendientesGlobales();

    List<PagoSuscripcion> pagosExpiradosGlobales();

    void confirmarPago(Long pagoId);

    void expirarPago(Long pagoId);

    int expirarPagosVencidos();

    void extenderMeses(String usernameCliente, int meses);

    void suspenderCuenta(String username);

    void reactivarCuenta(String username);

    void cambiarPlan(String username, String planCodigo);

    List<PlanSuscripcion> planesDisponibles();
}
