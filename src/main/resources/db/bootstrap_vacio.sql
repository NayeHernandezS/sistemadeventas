-- Recrea java_curso desde cero: todas las tablas, sin filas de datos.
-- Ejecutar desde esta carpeta (src/main/resources/db):
--   mysql -u root -p < bootstrap_vacio.sql
--
-- Alternativa con script (lee DB_USER / DB_PASSWORD desde .env):
--   ./reset-db-vacio.sh

DROP DATABASE IF EXISTS java_curso;

SOURCE schema.sql;
