package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ClienteCuenta;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;

import java.util.List;

public interface PlataformaService {

    List<ClienteCuenta> listarClientes();

    List<PagoSuscripcion> pagosPendientesGlobales();

    void confirmarPago(Long pagoId);

    void extenderMeses(String usernameCliente, int meses);
}
