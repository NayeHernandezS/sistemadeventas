-- Ejecutar en MySQL (Workbench) una sola vez
USE java_curso;

-- PASO 1: Solo si aun NO tienes la columna (si sale error 1060, salta al PASO 2)
-- ALTER TABLE productos ADD COLUMN owner_username VARCHAR(100) NULL;

-- PASO 2: Asignar productos existentes al primer usuario
UPDATE productos p
INNER JOIN (
    SELECT username FROM usuarios ORDER BY id ASC LIMIT 1
) AS u
SET p.owner_username = u.username
WHERE p.id > 0
  AND (p.owner_username IS NULL OR p.owner_username = '');

-- PASO 3: Obligatorio y no nulo
ALTER TABLE productos MODIFY owner_username VARCHAR(100) NOT NULL;

-- PASO 4: Indice (si ya existe, ignora el error o comenta esta linea)
CREATE INDEX idx_productos_owner ON productos (owner_username);
