package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.nhernandez.webapp.sistemaventas.models.Suscripcion;

import java.sql.SQLException;
import java.time.LocalDateTime;

public interface SuscripcionRepository {

    Suscripcion porUsername(String username) throws SQLException;

    void guardar(Suscripcion suscripcion) throws SQLException;

    void extenderVigencia(String username, LocalDateTime nuevaFechaFin, boolean enPeriodoPrueba) throws SQLException;

    void actualizarPlan(String username, String planCodigo, boolean enPeriodoPrueba) throws SQLException;

    /**
     * Suscripciones vigentes cuya fecha_fin cae en el dia indicado (0 = hoy, 1 = manana, etc.).
     */
    java.util.List<Suscripcion> listarVigentesQueVencenEn(int diasDesdeHoy) throws SQLException;

    void activarRenovacionAutomatica(String username, String planCodigo, String mpPreapprovalId) throws SQLException;

    void desactivarRenovacionAutomatica(String username) throws SQLException;

    Suscripcion porPreapprovalId(String mpPreapprovalId) throws SQLException;

    void actualizarEstado(String username, String estado) throws SQLException;
}
