package org.nhernandez.webapp.sistemaventas.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.config.JdbcConnectionHolder;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Registrado solo desde {@link org.nhernandez.webapp.sistemaventas.config.FilterConfig}
 * (sin @Component) para no duplicar el filtro en el contenedor.
 */
public class ConexionFilter implements Filter {

    private final DataSource dataSource;

    public ConexionFilter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpReq)) {
            chain.doFilter(request, response);
            return;
        }

        if (esRutaPublica(httpReq)) {
            chain.doFilter(request, response);
            return;
        }

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            JdbcConnectionHolder.bind(conn);

            chain.doFilter(request, response);
            if (estaAbierta(conn)) {
                conn.commit();
            }
        } catch (Exception e) {
            if (estaAbierta(conn)) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                    // ignorar error al revertir
                }
            }
            if (e instanceof ServletException servletException) {
                throw servletException;
            }
            if (e instanceof IOException ioException) {
                throw ioException;
            }
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ServletException(e);
        } finally {
            JdbcConnectionHolder.clear();
            if (estaAbierta(conn)) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                    // ignorar error al cerrar
                }
            }
        }
    }

    private static boolean estaAbierta(Connection conn) {
        if (conn == null) {
            return false;
        }
        try {
            return !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean esRutaPublica(HttpServletRequest req) {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.isEmpty()) {
            path = "/";
        }
        String method = req.getMethod();
        if (path.equals("/login") || path.equals("/login.html")) {
            return "GET".equalsIgnoreCase(method);
        }
        if (path.equals("/registro")) {
            return "GET".equalsIgnoreCase(method);
        }
        if (path.equals("/")) {
            return "GET".equalsIgnoreCase(method);
        }
        if (path.startsWith("/registro/")) {
            return "GET".equalsIgnoreCase(method);
        }
        return path.equals("/logout")
                || path.startsWith("/css/")
                || path.startsWith("/img/")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".svg")
                || path.endsWith(".png");
    }
}
