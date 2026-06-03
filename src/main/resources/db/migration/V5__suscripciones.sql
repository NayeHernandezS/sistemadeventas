-- Ejecutar en MySQL (Workbench) despues de migracion_multiusuario.sql

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
    plan_codigo VARCHAR(30) NOT NULL DEFAULT 'EMPRENDEDOR',
    INDEX idx_pagos_username (username),
    INDEX idx_pagos_estado (estado)
);

-- Suscripcion de prueba (1 mes) para usuarios que ya existen sin suscripcion
INSERT INTO suscripciones (username, fecha_inicio, fecha_fin, en_periodo_prueba, estado)
SELECT u.username, NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), 1, 'ACTIVA'
FROM usuarios u
WHERE NOT EXISTS (SELECT 1 FROM suscripciones s WHERE s.username = u.username);
