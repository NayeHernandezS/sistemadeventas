-- Credenciales Facturama por tenant (cada negocio timbra con su cuenta PAC).
-- Idempotente (MySQL 8+).

SET @col_user = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'datos_fiscales_negocio'
      AND COLUMN_NAME = 'facturama_username'
);
SET @sql_user = IF(@col_user = 0,
    'ALTER TABLE datos_fiscales_negocio ADD COLUMN facturama_username VARCHAR(150) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_user;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_pass = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'datos_fiscales_negocio'
      AND COLUMN_NAME = 'facturama_password_enc'
);
SET @sql_pass = IF(@col_pass = 0,
    'ALTER TABLE datos_fiscales_negocio ADD COLUMN facturama_password_enc VARCHAR(512) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_pass;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_sandbox = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'datos_fiscales_negocio'
      AND COLUMN_NAME = 'facturama_sandbox'
);
SET @sql_sandbox = IF(@col_sandbox = 0,
    'ALTER TABLE datos_fiscales_negocio ADD COLUMN facturama_sandbox TINYINT NOT NULL DEFAULT 1',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_sandbox;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_hab = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'datos_fiscales_negocio'
      AND COLUMN_NAME = 'cfdi_habilitado'
);
SET @sql_hab = IF(@col_hab = 0,
    'ALTER TABLE datos_fiscales_negocio ADD COLUMN cfdi_habilitado TINYINT NOT NULL DEFAULT 0',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_hab;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
