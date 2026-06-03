-- Flyway V1: esquema completo (equivalente a schema.sql sin CREATE DATABASE).
-- Idempotente con CREATE IF NOT EXISTS. Ver docs/FLYWAY.md.


-- ---------------------------------------------------------------------------
-- Nucleo del curso + columnas multi-tenant
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'VENDEDOR',
    admin_owner VARCHAR(100) NULL,
    tipo_negocio VARCHAR(50) NULL,
    aceptacion_legal_en DATETIME NULL,
    aceptacion_legal_version VARCHAR(20) NULL,
    INDEX idx_usuarios_admin_owner (admin_owner)
);

CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    owner_username VARCHAR(100) NOT NULL,
    UNIQUE INDEX uk_categorias_owner_nombre (owner_username, nombre),
    INDEX idx_categorias_owner (owner_username)
);

CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_owner VARCHAR(100) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    rfc VARCHAR(13) NULL,
    razon_social VARCHAR(200) NULL,
    email VARCHAR(150) NULL,
    codigo_postal VARCHAR(10) NULL,
    uso_cfdi VARCHAR(10) NULL,
    activo TINYINT NOT NULL DEFAULT 1,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_clientes_tenant (tenant_owner),
    UNIQUE INDEX uk_clientes_tenant_rfc (tenant_owner, rfc)
);

CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    precio INT NOT NULL DEFAULT 0,
    existencias INT NOT NULL DEFAULT 0,
    sku VARCHAR(50) NULL,
    categoria_id BIGINT NOT NULL,
    fecha_registro DATE NOT NULL,
    owner_username VARCHAR(100) NOT NULL,
    INDEX idx_productos_owner (owner_username),
    INDEX idx_productos_categoria (categoria_id)
);

CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_owner VARCHAR(100) NOT NULL,
    producto_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    existencias_antes INT NOT NULL,
    existencias_despues INT NOT NULL,
    motivo VARCHAR(255) NULL,
    username VARCHAR(100) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_mov_tenant_fecha (tenant_owner, fecha),
    INDEX idx_mov_producto (producto_id)
);

CREATE TABLE IF NOT EXISTS suscripcion_correos_enviados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    fecha_vencimiento_ref DATE NOT NULL,
    fecha_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_suscripcion_correo (username, tipo, fecha_vencimiento_ref),
    INDEX idx_correo_fecha (fecha_envio)
);

CREATE TABLE IF NOT EXISTS tickets_venta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    folio VARCHAR(40) NOT NULL,
    username_vendedor VARCHAR(100) NOT NULL,
    tenant_owner VARCHAR(100) NOT NULL,
    fecha_venta DATETIME NOT NULL,
    total INT NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    INDEX idx_tickets_vendedor (username_vendedor),
    INDEX idx_tickets_tenant_owner (tenant_owner),
    INDEX idx_tickets_folio (folio)
);

CREATE TABLE IF NOT EXISTS ticket_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    nombre_producto VARCHAR(200) NOT NULL,
    precio_unitario INT NOT NULL,
    cantidad INT NOT NULL,
    importe INT NOT NULL,
    INDEX idx_ticket_items_ticket (ticket_id)
);

CREATE TABLE IF NOT EXISTS facturas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    cliente_id BIGINT NULL,
    folio_factura VARCHAR(40) NOT NULL,
    rfc VARCHAR(13) NOT NULL,
    razon_social VARCHAR(200) NOT NULL,
    email VARCHAR(150) NULL,
    direccion VARCHAR(255) NULL,
    uso_cfdi VARCHAR(10) NULL,
    codigo_postal_receptor VARCHAR(10) NULL,
    fecha_emision DATETIME NOT NULL,
    cfdi_uuid VARCHAR(36) NULL,
    cfdi_estado VARCHAR(20) NOT NULL DEFAULT 'INFORMATIVO',
    cfdi_mensaje VARCHAR(500) NULL,
    cfdi_proveedor_id VARCHAR(80) NULL,
    UNIQUE INDEX uk_facturas_ticket (ticket_id),
    INDEX idx_facturas_cliente (cliente_id)
);

-- ---------------------------------------------------------------------------
-- SaaS: suscripciones, pagos, devoluciones, soporte
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS suscripciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    en_periodo_prueba TINYINT(1) NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    plan_codigo VARCHAR(30) NOT NULL DEFAULT 'EMPRENDEDOR',
    renovacion_automatica TINYINT(1) NOT NULL DEFAULT 0,
    mp_preapproval_id VARCHAR(80) NULL
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
    mp_preference_id VARCHAR(80) NULL,
    mp_payment_id VARCHAR(80) NULL,
    canal VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    plan_codigo VARCHAR(30) NOT NULL DEFAULT 'EMPRENDEDOR',
    INDEX idx_pagos_username (username),
    INDEX idx_pagos_estado (estado)
);

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

CREATE TABLE IF NOT EXISTS datos_fiscales_negocio (
    tenant_username VARCHAR(100) PRIMARY KEY,
    rfc VARCHAR(13) NULL,
    razon_social VARCHAR(200) NULL,
    email VARCHAR(150) NULL,
    direccion VARCHAR(255) NULL,
    uso_cfdi VARCHAR(10) NULL,
    codigo_postal VARCHAR(10) NULL,
    regimen_fiscal VARCHAR(10) NULL
);

CREATE TABLE IF NOT EXISTS preferencias_tenant (
    tenant_username VARCHAR(100) PRIMARY KEY,
    stock_minimo INT NULL,
    onboarding_completado TINYINT NOT NULL DEFAULT 0,
    logo_filename VARCHAR(255) NULL
);
