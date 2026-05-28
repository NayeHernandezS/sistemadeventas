-- Columna de existencias para inventario
USE java_curso;

ALTER TABLE productos ADD COLUMN existencias INT NOT NULL DEFAULT 0;
