package org.nhernandez.webapp.sistemaventas.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Conexion JDBC activa en la peticion HTTP actual (una por hilo).
 */
public final class JdbcConnectionHolder {

    private static final ThreadLocal<Connection> CONNECTION = new ThreadLocal<>();

    private JdbcConnectionHolder() {
    }

    public static void bind(Connection connection) {
        CONNECTION.set(connection);
    }

    public static Connection get() {
        return CONNECTION.get();
    }

    public static Connection require() {
        Connection connection = CONNECTION.get();
        if (connection == null) {
            throw new IllegalStateException(
                    "No hay conexion JDBC activa. La peticion debe pasar por ConexionFilter.");
        }
        return connection;
    }

    public static void clear() {
        CONNECTION.remove();
    }

    /** Revierte la transaccion de la peticion actual sin lanzar excepcion. */
    public static void rollbackSilencioso() {
        Connection connection = get();
        if (connection == null) {
            return;
        }
        try {
            if (!connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException ignored) {
            // ignorar error al revertir
        }
    }
}
