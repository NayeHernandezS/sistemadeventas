-- Registro de correos de aviso de suscripcion (idempotencia por ciclo de vencimiento)
-- Idempotente (MySQL 8+)


CREATE TABLE IF NOT EXISTS suscripcion_correos_enviados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    fecha_vencimiento_ref DATE NOT NULL,
    fecha_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_suscripcion_correo (username, tipo, fecha_vencimiento_ref),
    INDEX idx_correo_fecha (fecha_envio)
);
