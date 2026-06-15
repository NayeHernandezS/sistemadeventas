-- Porcentaje de ganancia deseado sobre el precio de compra (solo admin).
-- Idempotente (MySQL 8+).

SET @col_porcentaje_ganancia = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND COLUMN_NAME = 'porcentaje_ganancia'
);
SET @sql_porcentaje_ganancia = IF(@col_porcentaje_ganancia = 0,
    'ALTER TABLE productos ADD COLUMN porcentaje_ganancia INT NOT NULL DEFAULT 0 AFTER precio_compra',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_porcentaje_ganancia;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
