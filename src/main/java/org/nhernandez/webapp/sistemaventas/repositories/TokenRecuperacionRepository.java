package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.models.TokenRecuperacion;

import java.sql.SQLException;

public interface TokenRecuperacionRepository {

    void guardar(TokenRecuperacion token) throws SQLException;

    TokenRecuperacion porToken(String token) throws SQLException;

    void invalidarPorUsername(String username) throws SQLException;

    void marcarUsado(Long id) throws SQLException;
}
