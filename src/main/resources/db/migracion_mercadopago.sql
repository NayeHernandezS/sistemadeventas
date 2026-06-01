-- Mercado Pago: referencias de preferencia y pago en pagos_suscripcion
-- Idempotente (MySQL 8+)

USE java_curso;

SET @col_mp_pref = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND COLUMN_NAME = 'mp_preference_id'
);
SET @sql_mp_pref = IF(@col_mp_pref = 0,
    'ALTER TABLE pagos_suscripcion ADD COLUMN mp_preference_id VARCHAR(80) NULL AFTER notas',
    'SELECT 1');
PREPARE stmt FROM @sql_mp_pref;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_mp_pay = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND COLUMN_NAME = 'mp_payment_id'
);
SET @sql_mp_pay = IF(@col_mp_pay = 0,
    'ALTER TABLE pagos_suscripcion ADD COLUMN mp_payment_id VARCHAR(80) NULL AFTER mp_preference_id',
    'SELECT 1');
PREPARE stmt FROM @sql_mp_pay;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_canal = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND COLUMN_NAME = 'canal'
);
SET @sql_canal = IF(@col_canal = 0,
    'ALTER TABLE pagos_suscripcion ADD COLUMN canal VARCHAR(20) NOT NULL DEFAULT ''MANUAL'' AFTER mp_payment_id',
    'SELECT 1');
PREPARE stmt FROM @sql_canal;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
