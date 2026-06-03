-- Columna de existencias para inventario (idempotente)

SET @exists_existencias := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND COLUMN_NAME = 'existencias'
);
SET @sql_existencias := IF(
    @exists_existencias = 0,
    'ALTER TABLE productos ADD COLUMN existencias INT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE stmt_existencias FROM @sql_existencias;
EXECUTE stmt_existencias;
DEALLOCATE PREPARE stmt_existencias;
