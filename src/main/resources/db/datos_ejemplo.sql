-- Datos de demostracion (opcional). Ejecutar despues de schema.sql en base vacia.
-- Credenciales de prueba (contraseña en texto plano con prefijo {noop}):
--   tienda1 / admin123     (ADMIN de negocio)
--   vendedor1 / vendedor1  (VENDEDOR de tienda1)
--   plataforma / plataforma1 (SUPER_ADMIN panel /plataforma)

USE java_curso;

DELETE FROM devolucion_items;
DELETE FROM devoluciones;
DELETE FROM ticket_items;
DELETE FROM facturas;
DELETE FROM tickets_venta;
DELETE FROM pagos_suscripcion;
DELETE FROM suscripciones;
DELETE FROM solicitudes_soporte;
DELETE FROM productos;
DELETE FROM categorias;
DELETE FROM usuarios;

INSERT INTO usuarios (username, password, email, rol, admin_owner, tipo_negocio) VALUES
('tienda1', '{noop}admin123', 'tienda1@ejemplo.com', 'ADMIN', NULL, 'TIENDA'),
('vendedor1', '{noop}vendedor1', 'vendedor1@ejemplo.com', 'VENDEDOR', 'tienda1', NULL),
('plataforma', '{noop}plataforma1', 'plataforma@ejemplo.com', 'SUPER_ADMIN', NULL, NULL);

INSERT INTO categorias (nombre, owner_username) VALUES
('Bebidas', 'tienda1'),
('Snacks', 'tienda1');

INSERT INTO productos (nombre, precio, existencias, sku, categoria_id, fecha_registro, owner_username) VALUES
('Agua 600ml', 15, 100, 'AGU-600', 1, CURDATE(), 'tienda1'),
('Refresco 355ml', 20, 80, 'REF-355', 1, CURDATE(), 'tienda1'),
('Papas fritas', 25, 50, 'PAP-001', 2, CURDATE(), 'tienda1');

INSERT INTO suscripciones (username, fecha_inicio, fecha_fin, en_periodo_prueba, estado, plan_codigo) VALUES
('tienda1', NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), 1, 'ACTIVA', 'EMPRENDEDOR');
