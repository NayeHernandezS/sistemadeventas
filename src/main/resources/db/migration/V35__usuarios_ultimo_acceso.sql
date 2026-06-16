-- Ultimo inicio de sesion por usuario (plataforma y perfil ADMIN).
SET @col_ultimo_acceso = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'ultimo_acceso'
);
SET @sql_ultimo_acceso = IF(@col_ultimo_acceso = 0,
    'ALTER TABLE usuarios ADD COLUMN ultimo_acceso DATETIME NULL AFTER aceptacion_legal_version',
    'SET @flyway_skip = 1');
PREPARE stmt FROM @sql_ultimo_acceso;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
