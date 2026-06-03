-- Vincula facturas con el catalogo de clientes (idempotente)

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'cliente_id');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN cliente_id BIGINT NULL AFTER ticket_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND INDEX_NAME = 'idx_facturas_cliente');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_facturas_cliente ON facturas (cliente_id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
