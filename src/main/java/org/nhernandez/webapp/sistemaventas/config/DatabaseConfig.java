package org.nhernandez.webapp.sistemaventas.config;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

@Configuration
public class DatabaseConfig {

    /**
     * Proxy singleton: delega al hilo actual. {@link Connection#close()} no cierra la conexion real;
     * solo {@link org.nhernandez.webapp.sistemaventas.filters.ConexionFilter} la devuelve al pool.
     */
    @Bean
    @MysqlConn
    public Connection connection() {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("close".equals(method.getName()) && (args == null || args.length == 0)) {
                return null;
            }
            Connection target = JdbcConnectionHolder.require();
            return method.invoke(target, args);
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                handler);
    }
}
