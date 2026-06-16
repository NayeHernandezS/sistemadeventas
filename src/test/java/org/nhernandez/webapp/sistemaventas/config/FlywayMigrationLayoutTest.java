package org.nhernandez.webapp.sistemaventas.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationLayoutTest {

    private static final Pattern VERSIONED = Pattern.compile("^V\\d+__.+\\.sql$");

    @Test
    void migracionesFlyway_presentesYNumeradas() throws IOException {
        Path dir = new ClassPathResource("db/migration").getFile().toPath();
        long count = Files.list(dir)
                .filter(p -> VERSIONED.matcher(p.getFileName().toString()).matches())
                .count();
        assertEquals(38, count, "Se esperan V1..V38 en db/migration");
        assertTrue(Files.exists(dir.resolve("V1__initial_schema.sql")));
        assertTrue(Files.exists(dir.resolve("V38__ticket_nombre_cliente.sql")));
    }
}
