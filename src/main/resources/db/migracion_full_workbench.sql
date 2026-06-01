-- Migracion completa monolitica para MySQL Workbench (sin SOURCE)
USE java_curso;

-- =========================================================
-- 1) EXISTENCIAS EN PRODUCTOS
-- =========================================================
SET @exists_existencias := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND COLUMN_NAME = 'existencias'
);
SET @sql_existencias := IF(
    @exists_existencias = 0,
    'ALTER TABLE productos ADD COLUMN existencias INT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE stmt_existencias FROM @sql_existencias;
EXECUTE stmt_existencias;
DEALLOCATE PREPARE stmt_existencias;

-- =========================================================
-- 2) MULTIUSUARIO BASE EN PRODUCTOS
-- =========================================================
SET @exists_owner_prod := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND COLUMN_NAME = 'owner_username'
);
SET @sql_owner_prod := IF(
    @exists_owner_prod = 0,
    'ALTER TABLE productos ADD COLUMN owner_username VARCHAR(100) NULL',
    'SELECT 1'
);
PREPARE stmt_owner_prod FROM @sql_owner_prod;
EXECUTE stmt_owner_prod;
DEALLOCATE PREPARE stmt_owner_prod;

UPDATE productos p
INNER JOIN (
    SELECT username FROM usuarios ORDER BY id ASC LIMIT 1
) AS u
SET p.owner_username = u.username
WHERE p.id > 0
  AND (p.owner_username IS NULL OR p.owner_username = '');

ALTER TABLE productos MODIFY owner_username VARCHAR(100) NOT NULL;

SET @idx_prod_owner := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'productos'
      AND INDEX_NAME = 'idx_productos_owner'
);
SET @sql_idx_prod_owner := IF(
    @idx_prod_owner = 0,
    'CREATE INDEX idx_productos_owner ON productos (owner_username)',
    'SELECT 1'
);
PREPARE stmt_idx_prod_owner FROM @sql_idx_prod_owner;
EXECUTE stmt_idx_prod_owner;
DEALLOCATE PREPARE stmt_idx_prod_owner;

-- =========================================================
-- 3) TENANT: admin_owner y tenant_owner
-- =========================================================
SET @exists_admin_owner := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'admin_owner'
);
SET @sql_admin_owner := IF(
    @exists_admin_owner = 0,
    'ALTER TABLE usuarios ADD COLUMN admin_owner VARCHAR(100) NULL',
    'SELECT 1'
);
PREPARE stmt_admin_owner FROM @sql_admin_owner;
EXECUTE stmt_admin_owner;
DEALLOCATE PREPARE stmt_admin_owner;

SET @exists_tenant_owner := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tickets_venta'
      AND COLUMN_NAME = 'tenant_owner'
);
SET @sql_tenant_owner := IF(
    @exists_tenant_owner = 0,
    'ALTER TABLE tickets_venta ADD COLUMN tenant_owner VARCHAR(100) NULL',
    'SELECT 1'
);
PREPARE stmt_tenant_owner FROM @sql_tenant_owner;
EXECUTE stmt_tenant_owner;
DEALLOCATE PREPARE stmt_tenant_owner;

SET @idx_usuarios_admin_owner := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND INDEX_NAME = 'idx_usuarios_admin_owner'
);
SET @sql_idx_usuarios_admin_owner := IF(
    @idx_usuarios_admin_owner = 0,
    'CREATE INDEX idx_usuarios_admin_owner ON usuarios (admin_owner)',
    'SELECT 1'
);
PREPARE stmt_idx_usuarios_admin_owner FROM @sql_idx_usuarios_admin_owner;
EXECUTE stmt_idx_usuarios_admin_owner;
DEALLOCATE PREPARE stmt_idx_usuarios_admin_owner;

SET @idx_tickets_tenant_owner := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tickets_venta'
      AND INDEX_NAME = 'idx_tickets_tenant_owner'
);
SET @sql_idx_tickets_tenant_owner := IF(
    @idx_tickets_tenant_owner = 0,
    'CREATE INDEX idx_tickets_tenant_owner ON tickets_venta (tenant_owner)',
    'SELECT 1'
);
PREPARE stmt_idx_tickets_tenant_owner FROM @sql_idx_tickets_tenant_owner;
EXECUTE stmt_idx_tickets_tenant_owner;
DEALLOCATE PREPARE stmt_idx_tickets_tenant_owner;

UPDATE usuarios v
INNER JOIN (
    SELECT username FROM usuarios WHERE UPPER(rol) = 'ADMIN' ORDER BY id LIMIT 1
) a ON 1 = 1
SET v.admin_owner = a.username
WHERE v.id > 0
  AND UPPER(v.rol) = 'VENDEDOR'
  AND (v.admin_owner IS NULL OR v.admin_owner = '');

UPDATE productos p
INNER JOIN usuarios u ON p.owner_username = u.username
SET p.owner_username = CASE
    WHEN UPPER(u.rol) = 'ADMIN' THEN u.username
    ELSE COALESCE(u.admin_owner, u.username)
END
WHERE p.id > 0;

UPDATE tickets_venta t
INNER JOIN usuarios u ON t.username_vendedor = u.username
SET t.tenant_owner = CASE
    WHEN UPPER(u.rol) = 'ADMIN' THEN u.username
    ELSE COALESCE(u.admin_owner, u.username)
END
WHERE t.id > 0
  AND (t.tenant_owner IS NULL OR t.tenant_owner = '');

-- =========================================================
-- 4) SUSCRIPCIONES Y PAGOS
-- =========================================================
CREATE TABLE IF NOT EXISTS suscripciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    en_periodo_prueba TINYINT(1) NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    plan_codigo VARCHAR(30) NOT NULL DEFAULT 'EMPRENDEDOR'
);

CREATE TABLE IF NOT EXISTS pagos_suscripcion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    meses INT NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    fecha_solicitud DATETIME NOT NULL,
    fecha_confirmacion DATETIME NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    notas VARCHAR(255) NULL,
    plan_codigo VARCHAR(30) NOT NULL DEFAULT 'EMPRENDEDOR'
);

SET @idx_pagos_username := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND INDEX_NAME = 'idx_pagos_username'
);
SET @sql_idx_pagos_username := IF(
    @idx_pagos_username = 0,
    'CREATE INDEX idx_pagos_username ON pagos_suscripcion (username)',
    'SELECT 1'
);
PREPARE stmt_idx_pagos_username FROM @sql_idx_pagos_username;
EXECUTE stmt_idx_pagos_username;
DEALLOCATE PREPARE stmt_idx_pagos_username;

SET @idx_pagos_estado := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND INDEX_NAME = 'idx_pagos_estado'
);
SET @sql_idx_pagos_estado := IF(
    @idx_pagos_estado = 0,
    'CREATE INDEX idx_pagos_estado ON pagos_suscripcion (estado)',
    'SELECT 1'
);
PREPARE stmt_idx_pagos_estado FROM @sql_idx_pagos_estado;
EXECUTE stmt_idx_pagos_estado;
DEALLOCATE PREPARE stmt_idx_pagos_estado;

INSERT INTO suscripciones (username, fecha_inicio, fecha_fin, en_periodo_prueba, estado)
SELECT u.username, NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), 1, 'ACTIVA'
FROM usuarios u
WHERE NOT EXISTS (SELECT 1 FROM suscripciones s WHERE s.username = u.username);

-- =========================================================
-- 5) CATEGORIAS POR TENANT + TIPO DE NEGOCIO
-- =========================================================
SET @exists_tipo_negocio := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'tipo_negocio'
);
SET @sql_tipo_negocio := IF(
    @exists_tipo_negocio = 0,
    'ALTER TABLE usuarios ADD COLUMN tipo_negocio VARCHAR(50) NULL',
    'SELECT 1'
);
PREPARE stmt_tipo_negocio FROM @sql_tipo_negocio;
EXECUTE stmt_tipo_negocio;
DEALLOCATE PREPARE stmt_tipo_negocio;

SET @exists_cat_owner := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'categorias'
      AND COLUMN_NAME = 'owner_username'
);
SET @sql_cat_owner := IF(
    @exists_cat_owner = 0,
    'ALTER TABLE categorias ADD COLUMN owner_username VARCHAR(100) NULL',
    'SELECT 1'
);
PREPARE stmt_cat_owner FROM @sql_cat_owner;
EXECUTE stmt_cat_owner;
DEALLOCATE PREPARE stmt_cat_owner;

UPDATE categorias c
JOIN (
    SELECT username FROM usuarios WHERE UPPER(rol) = 'ADMIN' ORDER BY id ASC LIMIT 1
) a ON 1 = 1
SET c.owner_username = a.username
WHERE (c.owner_username IS NULL OR c.owner_username = '')
  AND c.id > 0;

UPDATE categorias c
JOIN (
    SELECT username FROM usuarios ORDER BY id ASC LIMIT 1
) u ON 1 = 1
SET c.owner_username = u.username
WHERE (c.owner_username IS NULL OR c.owner_username = '')
  AND c.id > 0;

ALTER TABLE categorias MODIFY owner_username VARCHAR(100) NOT NULL;

SET @idx_categorias_owner := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'categorias'
      AND INDEX_NAME = 'idx_categorias_owner'
);
SET @sql_idx_categorias_owner := IF(
    @idx_categorias_owner = 0,
    'CREATE INDEX idx_categorias_owner ON categorias (owner_username)',
    'SELECT 1'
);
PREPARE stmt_idx_categorias_owner FROM @sql_idx_categorias_owner;
EXECUTE stmt_idx_categorias_owner;
DEALLOCATE PREPARE stmt_idx_categorias_owner;

SET @uk_categorias_owner_nombre := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'categorias'
      AND INDEX_NAME = 'uk_categorias_owner_nombre'
);
SET @sql_uk_categorias_owner_nombre := IF(
    @uk_categorias_owner_nombre = 0,
    'CREATE UNIQUE INDEX uk_categorias_owner_nombre ON categorias (owner_username, nombre)',
    'SELECT 1'
);
PREPARE stmt_uk_categorias_owner_nombre FROM @sql_uk_categorias_owner_nombre;
EXECUTE stmt_uk_categorias_owner_nombre;
DEALLOCATE PREPARE stmt_uk_categorias_owner_nombre;

-- =========================================================
-- 6) SUPER_ADMIN (manual: ver migracion_super_admin.sql)
-- UPDATE usuarios SET rol = 'SUPER_ADMIN' WHERE id = 1;
-- =========================================================

-- =========================================================
-- 7) DEVOLUCIONES DE VENTAS
-- =========================================================
SET @exists_ticket_estado := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tickets_venta'
      AND COLUMN_NAME = 'estado'
);
SET @sql_ticket_estado := IF(
    @exists_ticket_estado = 0,
    'ALTER TABLE tickets_venta ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT ''ACTIVO''',
    'SELECT 1'
);
PREPARE stmt_ticket_estado FROM @sql_ticket_estado;
EXECUTE stmt_ticket_estado;
DEALLOCATE PREPARE stmt_ticket_estado;

UPDATE tickets_venta SET estado = 'ACTIVO' WHERE id > 0 AND (estado IS NULL OR estado = '');

CREATE TABLE IF NOT EXISTS devoluciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio VARCHAR(40) NOT NULL,
    ticket_id BIGINT NOT NULL,
    ticket_folio VARCHAR(40) NOT NULL,
    tenant_owner VARCHAR(100) NOT NULL,
    username_registro VARCHAR(100) NOT NULL,
    fecha_devolucion DATETIME NOT NULL,
    motivo VARCHAR(255) NULL,
    total_devuelto INT NOT NULL,
    INDEX idx_devoluciones_tenant (tenant_owner),
    INDEX idx_devoluciones_ticket (ticket_id)
);

CREATE TABLE IF NOT EXISTS devolucion_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    devolucion_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    nombre_producto VARCHAR(200) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario INT NOT NULL,
    importe INT NOT NULL,
    INDEX idx_dev_items_devolucion (devolucion_id),
    INDEX idx_dev_items_ticket_prod (ticket_id, producto_id)
);

-- =========================================================
-- 8) SOLICITUDES DE SOPORTE
-- =========================================================
CREATE TABLE IF NOT EXISTS solicitudes_soporte (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_owner VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    email_contacto VARCHAR(150) NULL,
    asunto VARCHAR(120) NOT NULL,
    mensaje TEXT NOT NULL,
    fecha_solicitud DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    INDEX idx_soporte_tenant (tenant_owner),
    INDEX idx_soporte_estado (estado),
    INDEX idx_soporte_fecha (fecha_solicitud)
);

-- =========================================================
-- 9) PLANES DE SUSCRIPCION (EMPRENDEDOR, NEGOCIO, PRO)
-- =========================================================
SET @exists_plan_sus := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'suscripciones'
      AND COLUMN_NAME = 'plan_codigo'
);
SET @sql_plan_sus := IF(
    @exists_plan_sus = 0,
    'ALTER TABLE suscripciones ADD COLUMN plan_codigo VARCHAR(30) NOT NULL DEFAULT ''EMPRENDEDOR''',
    'SELECT 1'
);
PREPARE stmt_plan_sus FROM @sql_plan_sus;
EXECUTE stmt_plan_sus;
DEALLOCATE PREPARE stmt_plan_sus;

SET @exists_plan_pagos := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND COLUMN_NAME = 'plan_codigo'
);
SET @sql_plan_pagos := IF(
    @exists_plan_pagos = 0,
    'ALTER TABLE pagos_suscripcion ADD COLUMN plan_codigo VARCHAR(30) NOT NULL DEFAULT ''EMPRENDEDOR''',
    'SELECT 1'
);
PREPARE stmt_plan_pagos FROM @sql_plan_pagos;
EXECUTE stmt_plan_pagos;
DEALLOCATE PREPARE stmt_plan_pagos;

UPDATE suscripciones
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0 AND (plan_codigo IS NULL OR TRIM(plan_codigo) = '');

UPDATE pagos_suscripcion
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0 AND (plan_codigo IS NULL OR TRIM(plan_codigo) = '');

UPDATE suscripciones
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0
  AND UPPER(TRIM(plan_codigo)) NOT IN ('EMPRENDEDOR', 'NEGOCIO', 'PRO');

UPDATE pagos_suscripcion
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0
  AND UPPER(TRIM(plan_codigo)) NOT IN ('EMPRENDEDOR', 'NEGOCIO', 'PRO');

-- =========================================================
-- 10) RECUPERACION DE CONTRASENA
-- =========================================================
CREATE TABLE IF NOT EXISTS tokens_recuperacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    token VARCHAR(64) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    usado TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_tokens_recuperacion_token (token),
    INDEX idx_tokens_recuperacion_username (username)
);

-- =========================================================
-- 11) DATOS FISCALES DEL NEGOCIO
-- =========================================================
CREATE TABLE IF NOT EXISTS datos_fiscales_negocio (
    tenant_username VARCHAR(100) PRIMARY KEY,
    rfc VARCHAR(13) NULL,
    razon_social VARCHAR(200) NULL,
    email VARCHAR(150) NULL,
    direccion VARCHAR(255) NULL,
    uso_cfdi VARCHAR(10) NULL
);

-- =========================================================
-- 12) PREFERENCIAS TENANT (stock minimo)
-- =========================================================
CREATE TABLE IF NOT EXISTS preferencias_tenant (
    tenant_username VARCHAR(100) PRIMARY KEY,
    stock_minimo INT NULL
);

-- =========================================================
-- 13) MERCADO PAGO (referencias en pagos_suscripcion)
-- =========================================================
SET @col_mp_pref = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND COLUMN_NAME = 'mp_preference_id'
);
SET @sql_mp_pref = IF(@col_mp_pref = 0,
    'ALTER TABLE pagos_suscripcion ADD COLUMN mp_preference_id VARCHAR(80) NULL AFTER notas',
    'SELECT 1');
PREPARE stmt FROM @sql_mp_pref;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_mp_pay = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND COLUMN_NAME = 'mp_payment_id'
);
SET @sql_mp_pay = IF(@col_mp_pay = 0,
    'ALTER TABLE pagos_suscripcion ADD COLUMN mp_payment_id VARCHAR(80) NULL AFTER mp_preference_id',
    'SELECT 1');
PREPARE stmt FROM @sql_mp_pay;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_canal = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND COLUMN_NAME = 'canal'
);
SET @sql_canal = IF(@col_canal = 0,
    'ALTER TABLE pagos_suscripcion ADD COLUMN canal VARCHAR(20) NOT NULL DEFAULT ''MANUAL'' AFTER mp_payment_id',
    'SELECT 1');
PREPARE stmt FROM @sql_canal;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================================
-- 14) RENOVACION AUTOMATICA (suscripciones)
-- =========================================================
SET @col_renov = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'suscripciones'
      AND COLUMN_NAME = 'renovacion_automatica'
);
SET @sql_renov = IF(@col_renov = 0,
    'ALTER TABLE suscripciones ADD COLUMN renovacion_automatica TINYINT(1) NOT NULL DEFAULT 0 AFTER plan_codigo',
    'SELECT 1');
PREPARE stmt FROM @sql_renov;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_preapp = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'suscripciones'
      AND COLUMN_NAME = 'mp_preapproval_id'
);
SET @sql_preapp = IF(@col_preapp = 0,
    'ALTER TABLE suscripciones ADD COLUMN mp_preapproval_id VARCHAR(80) NULL AFTER renovacion_automatica',
    'SELECT 1');
PREPARE stmt FROM @sql_preapp;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================================
-- 15) LOGO DEL TENANT (preferencias_tenant)
-- =========================================================
SET @col_logo = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'preferencias_tenant'
      AND COLUMN_NAME = 'logo_filename'
);
SET @sql_logo = IF(@col_logo = 0,
    'ALTER TABLE preferencias_tenant ADD COLUMN logo_filename VARCHAR(255) NULL AFTER stock_minimo',
    'SELECT 1');
PREPARE stmt FROM @sql_logo;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================================================
-- 16) CFDI TIMBRADO (Facturama)
-- =========================================================
SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'datos_fiscales_negocio' AND COLUMN_NAME = 'codigo_postal');
SET @sql = IF(@col = 0, 'ALTER TABLE datos_fiscales_negocio ADD COLUMN codigo_postal VARCHAR(10) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'datos_fiscales_negocio' AND COLUMN_NAME = 'regimen_fiscal');
SET @sql = IF(@col = 0, 'ALTER TABLE datos_fiscales_negocio ADD COLUMN regimen_fiscal VARCHAR(10) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'codigo_postal_receptor');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN codigo_postal_receptor VARCHAR(10) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'cfdi_uuid');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN cfdi_uuid VARCHAR(36) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'cfdi_estado');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN cfdi_estado VARCHAR(20) NOT NULL DEFAULT ''INFORMATIVO''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'cfdi_mensaje');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN cfdi_mensaje VARCHAR(500) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'facturas' AND COLUMN_NAME = 'cfdi_proveedor_id');
SET @sql = IF(@col = 0, 'ALTER TABLE facturas ADD COLUMN cfdi_proveedor_id VARCHAR(80) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
