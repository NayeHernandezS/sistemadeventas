-- Datos base para tests de integracion (contraseña {noop}...)

DELETE FROM pagos_suscripcion;
DELETE FROM suscripciones;
DELETE FROM productos;
DELETE FROM categorias;
DELETE FROM usuarios;

INSERT INTO usuarios (username, password, email, rol, admin_owner, tipo_negocio) VALUES
('tienda1', '{noop}admin123', 'tienda1@test.local', 'ADMIN', NULL, 'TIENDA'),
('vendedor1', '{noop}vendedor1', 'vendedor1@test.local', 'VENDEDOR', 'tienda1', NULL),
('plataforma', '{noop}plataforma1', 'plataforma@test.local', 'SUPER_ADMIN', NULL, NULL),
('negocio_test', '{noop}admin123', 'negocio@test.local', 'ADMIN', NULL, 'TIENDA'),
('vend_a', '{noop}v1', 'va@test.local', 'VENDEDOR', 'negocio_test', NULL),
('vend_b', '{noop}v2', 'vb@test.local', 'VENDEDOR', 'negocio_test', NULL),
('vend_c', '{noop}v3', 'vc@test.local', 'VENDEDOR', 'negocio_test', NULL),
('vend_d', '{noop}v4', 'vd@test.local', 'VENDEDOR', 'negocio_test', NULL);

INSERT INTO categorias (nombre, owner_username) VALUES
('General', 'tienda1'),
('General', 'negocio_test');

INSERT INTO productos (nombre, precio, existencias, sku, categoria_id, fecha_registro, owner_username) VALUES
('Producto demo', 10, 5, 'DEMO-1', 1, CURRENT_DATE, 'tienda1');

INSERT INTO suscripciones (username, fecha_inicio, fecha_fin, en_periodo_prueba, estado, plan_codigo) VALUES
('tienda1', CURRENT_TIMESTAMP, DATEADD('MONTH', 1, CURRENT_TIMESTAMP), TRUE, 'ACTIVA', 'EMPRENDEDOR'),
('negocio_test', CURRENT_TIMESTAMP, DATEADD('MONTH', 3, CURRENT_TIMESTAMP), FALSE, 'ACTIVA', 'NEGOCIO');
