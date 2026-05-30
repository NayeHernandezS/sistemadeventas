package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ReporteVentas;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {

    ReporteVentas generar(String tenantOwner, String usernameVendedor, boolean esAdmin,
                            String vendedorFiltro, LocalDate fechaInicio, LocalDate fechaFin);
}
