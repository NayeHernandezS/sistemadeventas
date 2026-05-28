package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;

import java.sql.SQLException;
import java.util.List;

public interface PagoSuscripcionRepository {

    void guardar(PagoSuscripcion pago) throws SQLException;

    PagoSuscripcion porId(Long id) throws SQLException;

    List<PagoSuscripcion> listarPendientes() throws SQLException;

    List<PagoSuscripcion> listarPendientesPorUsername(String username) throws SQLException;

    List<PagoSuscripcion> listarPorUsername(String username) throws SQLException;

    void confirmar(Long id) throws SQLException;
}
