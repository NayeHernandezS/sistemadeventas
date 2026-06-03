-- Flag de onboarding completado en preferencias_tenant
-- Idempotente (MySQL 8+)


SET @col_onb = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'preferencias_tenant'
      AND COLUMN_NAME = 'onboarding_completado'
);
SET @sql_onb = IF(@col_onb = 0,
    'ALTER TABLE preferencias_tenant ADD COLUMN onboarding_completado TINYINT NOT NULL DEFAULT 0 AFTER stock_minimo',
    'SELECT 1');
PREPARE stmt FROM @sql_onb;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
