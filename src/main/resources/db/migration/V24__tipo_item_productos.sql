-- Productos vs servicios en el mismo catalogo (servicios sin inventario).
-- Idempotente (MySQL 8+).

SET @col_tipo_item = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND COLUMN_NAME = 'tipo_item'
);
SET @sql_tipo_item = IF(@col_tipo_item = 0,
    'ALTER TABLE productos ADD COLUMN tipo_item VARCHAR(20) NOT NULL DEFAULT ''PRODUCTO'' AFTER owner_username',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_tipo_item;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE productos
SET tipo_item = 'PRODUCTO'
WHERE id > 0
  AND (tipo_item IS NULL OR tipo_item = '');
