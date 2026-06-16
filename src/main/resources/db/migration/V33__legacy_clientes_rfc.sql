-- Columnas base de clientes faltantes en BD legacy (nombre, rfc, email).
-- Idempotente (MySQL 8+).

SET @col_nombre = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'nombre'
);
SET @sql_nombre = IF(@col_nombre = 0,
    'ALTER TABLE clientes ADD COLUMN nombre VARCHAR(200) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_nombre;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_rfc = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'rfc'
);
SET @sql_rfc = IF(@col_rfc = 0,
    'ALTER TABLE clientes ADD COLUMN rfc VARCHAR(13) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_rfc;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_email = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'email'
);
SET @sql_email = IF(@col_email = 0,
    'ALTER TABLE clientes ADD COLUMN email VARCHAR(150) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_email;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
