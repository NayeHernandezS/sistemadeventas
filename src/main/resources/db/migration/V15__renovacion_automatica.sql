-- Renovacion automatica mensual (Mercado Pago Preapproval)
-- Idempotente (MySQL 8+)


SET @col_renov = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'suscripciones'
      AND COLUMN_NAME = 'renovacion_automatica'
);
SET @sql_renov = IF(@col_renov = 0,
    'ALTER TABLE suscripciones ADD COLUMN renovacion_automatica TINYINT(1) NOT NULL DEFAULT 0 AFTER plan_codigo',
    'SELECT 1');
PREPARE stmt FROM @sql_renov;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_preapp = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'suscripciones'
      AND COLUMN_NAME = 'mp_preapproval_id'
);
SET @sql_preapp = IF(@col_preapp = 0,
    'ALTER TABLE suscripciones ADD COLUMN mp_preapproval_id VARCHAR(80) NULL AFTER renovacion_automatica',
    'SELECT 1');
PREPARE stmt FROM @sql_preapp;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
