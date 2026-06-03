package org.nhernandez.webapp.sistemaventas.repositories;

import java.sql.SQLException;
import java.time.LocalDate;

public interface SuscripcionCorreoEnviadoRepository {

    boolean yaEnviado(String username, String tipo, LocalDate fechaVencimientoRef) throws SQLException;

    void registrar(String username, String tipo, LocalDate fechaVencimientoRef) throws SQLException;
}
