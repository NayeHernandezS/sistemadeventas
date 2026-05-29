-- Panel de plataforma: operador del SaaS (creadora del producto)
-- Ejecutar en MySQL Workbench (USE java_curso;)
USE java_curso;

-- 1) Ver tu usuario y su id (copia el id de tu fila)
SELECT id, username, email, rol FROM usuarios ORDER BY id;

-- 2) Activar SUPER_ADMIN usando la clave primaria (compatible con safe updates)
--    Reemplaza 1 por el id que viste en el paso anterior:
UPDATE usuarios SET rol = 'SUPER_ADMIN' WHERE id = 1;

-- Alternativa por username (si desactivas safe updates o username tiene indice UNIQUE):
-- UPDATE usuarios SET rol = 'SUPER_ADMIN' WHERE username = 'nayely';

-- Opcion B sin SQL: en application.properties
-- plataforma.superadmins=tu_usuario

-- Tras el UPDATE, cierra sesion en la app e inicia de nuevo -> /plataforma
