package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.nhernandez.webapp.sistemaventas.models.Suscripcion;

import java.sql.SQLException;
import java.time.LocalDateTime;

public interface SuscripcionRepository {

    Suscripcion porUsername(String username) throws SQLException;

    void guardar(Suscripcion suscripcion) throws SQLException;

    void extenderVigencia(String username, LocalDateTime nuevaFechaFin, boolean enPeriodoPrueba) throws SQLException;
}
