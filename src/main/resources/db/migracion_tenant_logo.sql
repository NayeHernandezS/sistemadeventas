-- Logo de marca por tenant (admin); vendedores comparten el del admin_owner
USE java_curso;

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'preferencias_tenant'
      AND COLUMN_NAME = 'logo_filename'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE preferencias_tenant ADD COLUMN logo_filename VARCHAR(255) NULL AFTER stock_minimo',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
