-- Productos vs servicios en el mismo catalogo (servicios sin inventario).
-- Idempotente (MySQL 8+). En BD legacy, owner_username puede faltar (V3 fue no-op).

SET @col_owner = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND COLUMN_NAME = 'owner_username'
);
SET @sql_owner = IF(@col_owner = 0,
    'ALTER TABLE productos ADD COLUMN owner_username VARCHAR(100) NOT NULL DEFAULT '''' AFTER fecha_registro',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_owner;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_tipo_item = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND COLUMN_NAME = 'tipo_item'
);
SET @sql_tipo_item = IF(@col_tipo_item = 0,
    'ALTER TABLE productos ADD COLUMN tipo_item VARCHAR(20) NOT NULL DEFAULT ''PRODUCTO''',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_tipo_item;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE productos
SET tipo_item = 'PRODUCTO'
WHERE id > 0
  AND (tipo_item IS NULL OR tipo_item = '');
