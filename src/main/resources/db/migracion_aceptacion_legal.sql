-- Aceptacion de terminos y privacidad al registrarse (SaaS)
-- Idempotente (MySQL 8+)

USE java_curso;

SET @col_legal_en = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'aceptacion_legal_en'
);
SET @sql_legal_en = IF(@col_legal_en = 0,
    'ALTER TABLE usuarios ADD COLUMN aceptacion_legal_en DATETIME NULL AFTER tipo_negocio',
    'SELECT 1');
PREPARE stmt FROM @sql_legal_en;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_legal_ver = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'aceptacion_legal_version'
);
SET @sql_legal_ver = IF(@col_legal_ver = 0,
    'ALTER TABLE usuarios ADD COLUMN aceptacion_legal_version VARCHAR(20) NULL AFTER aceptacion_legal_en',
    'SELECT 1');
PREPARE stmt FROM @sql_legal_ver;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
