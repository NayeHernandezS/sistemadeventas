package org.nhernandez.webapp.sistemaventas.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * En produccion (Railway, etc.) repara migraciones marcadas como fallidas
 * tras cortes de conexion durante el primer deploy, luego aplica pendientes.
 */
@Configuration
@Profile("prod")
public class FlywayProdConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return (Flyway flyway) -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
