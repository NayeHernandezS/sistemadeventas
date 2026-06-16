-- Columnas de clientes en BD legacy (V18 no altera tablas clientes ya existentes del curso).
-- Idempotente (MySQL 8+).

SET @col_tenant = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'tenant_owner'
);
SET @sql_tenant = IF(@col_tenant = 0,
    'ALTER TABLE clientes ADD COLUMN tenant_owner VARCHAR(100) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_tenant;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_razon = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'razon_social'
);
SET @sql_razon = IF(@col_razon = 0,
    'ALTER TABLE clientes ADD COLUMN razon_social VARCHAR(200) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_razon;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_cp = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'codigo_postal'
);
SET @sql_cp = IF(@col_cp = 0,
    'ALTER TABLE clientes ADD COLUMN codigo_postal VARCHAR(10) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_cp;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_uso = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'uso_cfdi'
);
SET @sql_uso = IF(@col_uso = 0,
    'ALTER TABLE clientes ADD COLUMN uso_cfdi VARCHAR(10) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_uso;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_activo = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'activo'
);
SET @sql_activo = IF(@col_activo = 0,
    'ALTER TABLE clientes ADD COLUMN activo TINYINT NOT NULL DEFAULT 1',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_activo;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_fecha = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'fecha_registro'
);
SET @sql_fecha = IF(@col_fecha = 0,
    'ALTER TABLE clientes ADD COLUMN fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_fecha;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_tenant = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND INDEX_NAME = 'idx_clientes_tenant'
);
SET @sql_idx_tenant = IF(@idx_tenant = 0,
    'CREATE INDEX idx_clientes_tenant ON clientes (tenant_owner)',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_idx_tenant;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
