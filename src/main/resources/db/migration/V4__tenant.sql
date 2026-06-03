-- Multi-cuenta: cada ADMIN es dueño de su negocio (tenant)
-- Se puede ejecutar varias veces (omite columnas e índices que ya existen)

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER //

CREATE PROCEDURE add_column_if_missing(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_column, ' ', p_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

CREATE PROCEDURE add_index_if_missing(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_columns VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND INDEX_NAME = p_index
    ) THEN
        SET @sql = CONCAT('CREATE INDEX ', p_index, ' ON ', p_table, ' (', p_columns, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

-- Columnas
CALL add_column_if_missing('usuarios', 'admin_owner', 'VARCHAR(100) NULL');
CALL add_column_if_missing('tickets_venta', 'tenant_owner', 'VARCHAR(100) NULL');

-- Índices
CALL add_index_if_missing('usuarios', 'idx_usuarios_admin_owner', 'admin_owner');
CALL add_index_if_missing('tickets_venta', 'idx_tickets_tenant_owner', 'tenant_owner');

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

-- Asignar vendedores existentes al primer administrador
UPDATE usuarios v
INNER JOIN (
    SELECT username FROM usuarios WHERE UPPER(rol) = 'ADMIN' ORDER BY id LIMIT 1
) a ON 1 = 1
SET v.admin_owner = a.username
WHERE v.id > 0
  AND UPPER(v.rol) = 'VENDEDOR'
  AND (v.admin_owner IS NULL OR v.admin_owner = '');

-- Productos: owner_username = dueño de la cuenta (admin del tenant)
UPDATE productos p
INNER JOIN usuarios u ON p.owner_username = u.username
SET p.owner_username = CASE
    WHEN UPPER(u.rol) = 'ADMIN' THEN u.username
    ELSE COALESCE(u.admin_owner, u.username)
END
WHERE p.id > 0;

-- Tickets existentes
UPDATE tickets_venta t
INNER JOIN usuarios u ON t.username_vendedor = u.username
SET t.tenant_owner = CASE
    WHEN UPPER(u.rol) = 'ADMIN' THEN u.username
    ELSE COALESCE(u.admin_owner, u.username)
END
WHERE t.id > 0
  AND (t.tenant_owner IS NULL OR t.tenant_owner = '');
