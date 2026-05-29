-- Solicitudes de soporte de cuentas negocio hacia la plataforma
USE java_curso;

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
