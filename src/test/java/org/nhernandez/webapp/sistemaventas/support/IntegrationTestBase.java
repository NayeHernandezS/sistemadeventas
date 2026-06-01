package org.nhernandez.webapp.sistemaventas.support;

import org.nhernandez.webapp.sistemaventas.config.JdbcConnectionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    protected DataSource dataSource;

    @FunctionalInterface
    protected interface JdbcAction {
        void run() throws Exception;
    }

    /**
     * Ejecuta codigo con conexion JDBC enlazada al hilo (como {@code ConexionFilter}).
     * Hace commit al finalizar.
     */
    protected void withJdbc(JdbcAction action) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            JdbcConnectionHolder.bind(conn);
            try {
                action.run();
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                JdbcConnectionHolder.clear();
            }
        }
    }

    /**
     * Igual que {@link #withJdbc(JdbcAction)} pero revierte cambios (aislamiento entre tests).
     */
    protected void withJdbcRollback(JdbcAction action) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            JdbcConnectionHolder.bind(conn);
            try {
                action.run();
            } finally {
                conn.rollback();
                JdbcConnectionHolder.clear();
            }
        }
    }

    protected void ejecutarSql(String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
