-- Categorias por tenant (admin) y tipo de negocio por cuenta
USE java_curso;

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
    IN p_columns VARCHAR(255),
    IN p_unique TINYINT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND INDEX_NAME = p_index
    ) THEN
        IF p_unique = 1 THEN
            SET @sql = CONCAT('CREATE UNIQUE INDEX ', p_index, ' ON ', p_table, ' (', p_columns, ')');
        ELSE
            SET @sql = CONCAT('CREATE INDEX ', p_index, ' ON ', p_table, ' (', p_columns, ')');
        END IF;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

CALL add_column_if_missing('usuarios', 'tipo_negocio', 'VARCHAR(50) NULL');
CALL add_column_if_missing('categorias', 'owner_username', 'VARCHAR(100) NULL');

-- Categorias existentes -> primer admin disponible
UPDATE categorias c
JOIN (
    SELECT username FROM usuarios WHERE UPPER(rol) = 'ADMIN' ORDER BY id ASC LIMIT 1
) a ON 1 = 1
SET c.owner_username = a.username
WHERE (c.owner_username IS NULL OR c.owner_username = '')
  AND c.id > 0;

-- Si hay categorias sin owner y no existe admin, usar primer usuario
UPDATE categorias c
JOIN (
    SELECT username FROM usuarios ORDER BY id ASC LIMIT 1
) u ON 1 = 1
SET c.owner_username = u.username
WHERE (c.owner_username IS NULL OR c.owner_username = '')
  AND c.id > 0;

ALTER TABLE categorias MODIFY owner_username VARCHAR(100) NOT NULL;

CALL add_index_if_missing('categorias', 'idx_categorias_owner', 'owner_username', 0);
CALL add_index_if_missing('categorias', 'uk_categorias_owner_nombre', 'owner_username, nombre', 1);

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
