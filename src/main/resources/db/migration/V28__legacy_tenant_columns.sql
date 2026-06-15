-- Columnas multi-tenant en BD legacy (V3/V4/V6 fueron no-op; CREATE IF NOT EXISTS no altera tablas viejas).
-- Idempotente (MySQL 8+).

SET @col_admin_owner = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'admin_owner'
);
SET @sql_admin_owner = IF(@col_admin_owner = 0,
    'ALTER TABLE usuarios ADD COLUMN admin_owner VARCHAR(100) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_admin_owner;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_tipo_negocio = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'tipo_negocio'
);
SET @sql_tipo_negocio = IF(@col_tipo_negocio = 0,
    'ALTER TABLE usuarios ADD COLUMN tipo_negocio VARCHAR(50) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_tipo_negocio;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_legal_en = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'aceptacion_legal_en'
);
SET @sql_legal_en = IF(@col_legal_en = 0,
    'ALTER TABLE usuarios ADD COLUMN aceptacion_legal_en DATETIME NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_legal_en;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_legal_ver = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'aceptacion_legal_version'
);
SET @sql_legal_ver = IF(@col_legal_ver = 0,
    'ALTER TABLE usuarios ADD COLUMN aceptacion_legal_version VARCHAR(20) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_legal_ver;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_admin_owner = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND INDEX_NAME = 'idx_usuarios_admin_owner'
);
SET @sql_idx_admin_owner = IF(@idx_admin_owner = 0,
    'CREATE INDEX idx_usuarios_admin_owner ON usuarios (admin_owner)',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_idx_admin_owner;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_cat_owner = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'categorias'
      AND COLUMN_NAME = 'owner_username'
);
SET @sql_cat_owner = IF(@col_cat_owner = 0,
    'ALTER TABLE categorias ADD COLUMN owner_username VARCHAR(100) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_cat_owner;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_ticket_tenant = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tickets_venta'
      AND COLUMN_NAME = 'tenant_owner'
);
SET @sql_ticket_tenant = IF(@col_ticket_tenant = 0,
    'ALTER TABLE tickets_venta ADD COLUMN tenant_owner VARCHAR(100) NULL',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_ticket_tenant;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
