-- Precio de compra (costo interno) visible solo para el administrador en inventario.
-- Idempotente (MySQL 8+).

SET @col_precio_compra = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND COLUMN_NAME = 'precio_compra'
);
SET @sql_precio_compra = IF(@col_precio_compra = 0,
    'ALTER TABLE productos ADD COLUMN precio_compra INT NOT NULL DEFAULT 0 AFTER precio',
    'SELECT 1');
PREPARE stmt FROM @sql_precio_compra;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
