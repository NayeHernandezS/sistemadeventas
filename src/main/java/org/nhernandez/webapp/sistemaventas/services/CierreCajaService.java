package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.CierreCajaDia;

import java.time.LocalDate;

public interface CierreCajaService {

    CierreCajaDia generar(String tenantOwner, String username, boolean esAdmin, LocalDate fecha);
}
