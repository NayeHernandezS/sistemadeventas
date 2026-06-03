-- Esquema minimo H2 (modo MySQL) para tests de integracion

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'VENDEDOR',
    admin_owner VARCHAR(100) NULL,
    tipo_negocio VARCHAR(50) NULL,
    aceptacion_legal_en DATETIME NULL,
    aceptacion_legal_version VARCHAR(20) NULL
);

CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    owner_username VARCHAR(100) NOT NULL
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
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    precio INT NOT NULL DEFAULT 0,
    existencias INT NOT NULL DEFAULT 0,
    sku VARCHAR(50) NULL,
    categoria_id BIGINT NOT NULL,
    fecha_registro DATE NOT NULL,
    owner_username VARCHAR(100) NOT NULL
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
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS suscripciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NOT NULL,
    en_periodo_prueba BOOLEAN NOT NULL DEFAULT TRUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    plan_codigo VARCHAR(30) NOT NULL DEFAULT 'EMPRENDEDOR',
    renovacion_automatica BOOLEAN NOT NULL DEFAULT FALSE,
    mp_preapproval_id VARCHAR(80) NULL
);

CREATE TABLE IF NOT EXISTS pagos_suscripcion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    meses INT NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    fecha_solicitud TIMESTAMP NOT NULL,
    fecha_confirmacion TIMESTAMP NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    notas VARCHAR(255) NULL,
    mp_preference_id VARCHAR(80) NULL,
    mp_payment_id VARCHAR(80) NULL,
    canal VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    plan_codigo VARCHAR(30) NOT NULL DEFAULT 'EMPRENDEDOR'
);

CREATE TABLE IF NOT EXISTS preferencias_tenant (
    tenant_username VARCHAR(100) PRIMARY KEY,
    stock_minimo INT NULL,
    onboarding_completado TINYINT NOT NULL DEFAULT 0,
    logo_filename VARCHAR(255) NULL
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
    fecha_emision TIMESTAMP NOT NULL,
    codigo_postal_receptor VARCHAR(10) NULL,
    cfdi_uuid VARCHAR(36) NULL,
    cfdi_estado VARCHAR(20) NOT NULL DEFAULT 'INFORMATIVO',
    cfdi_mensaje VARCHAR(500) NULL,
    cfdi_proveedor_id VARCHAR(80) NULL
);
