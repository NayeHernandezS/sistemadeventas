-- Columnas de preferencias_tenant en BD legacy (V13 solo tenia stock_minimo; V22 es no-op).
-- Idempotente (MySQL 8+).

SET @col_onboarding = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'preferencias_tenant'
      AND COLUMN_NAME = 'onboarding_completado'
);
SET @sql_onboarding = IF(@col_onboarding = 0,
    'ALTER TABLE preferencias_tenant ADD COLUMN onboarding_completado TINYINT NOT NULL DEFAULT 0',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_onboarding;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_logo = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'preferencias_tenant'
      AND COLUMN_NAME = 'logo_filename'
);
SET @sql_logo = IF(@col_logo = 0,
    'ALTER TABLE preferencias_tenant ADD COLUMN logo_filename VARCHAR(255) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_logo;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
