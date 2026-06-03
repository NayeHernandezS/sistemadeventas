-- Multiusuario: owner_username en productos (idempotente; compatible con V1)

UPDATE productos p
INNER JOIN (
    SELECT username FROM usuarios ORDER BY id ASC LIMIT 1
) AS u
SET p.owner_username = u.username
WHERE p.id > 0
  AND (p.owner_username IS NULL OR p.owner_username = '');

ALTER TABLE productos MODIFY owner_username VARCHAR(100) NOT NULL;

SET @idx_owner := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND INDEX_NAME = 'idx_productos_owner'
);
SET @sql_idx := IF(@idx_owner = 0,
    'CREATE INDEX idx_productos_owner ON productos (owner_username)',
    'SELECT 1');
PREPARE stmt FROM @sql_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
