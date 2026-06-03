-- CFDI timbrado: datos emisor y estado por factura (idempotente MySQL 8+)

-- datos_fiscales_negocio.codigo_postal
SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'datos_fiscales_negocio' AND COLUMN_NAME = 'codigo_postal');
SET @sql = IF(@col = 0, 'ALTER TABLE datos_fiscales_negocio ADD COLUMN codigo_postal VARCHAR(10) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'datos_fiscales_negocio' AND COLUMN_NAME = 'regimen_fiscal');
SET @sql = IF(@col = 0, 'ALTER TABLE datos_fiscales_negocio ADD COLUMN regimen_fiscal VARCHAR(10) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- facturas.cfdi_*
SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'codigo_postal_receptor');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN codigo_postal_receptor VARCHAR(10) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'cfdi_uuid');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN cfdi_uuid VARCHAR(36) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'cfdi_estado');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN cfdi_estado VARCHAR(20) NOT NULL DEFAULT ''INFORMATIVO''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'cfdi_mensaje');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN cfdi_mensaje VARCHAR(500) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'cfdi_proveedor_id');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN cfdi_proveedor_id VARCHAR(80) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
