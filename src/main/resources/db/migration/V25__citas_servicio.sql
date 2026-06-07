-- Agenda de citas para servicios del catalogo.

CREATE TABLE IF NOT EXISTS citas_servicio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_owner VARCHAR(100) NOT NULL,
    producto_id BIGINT NOT NULL,
    cliente_id BIGINT NULL,
    fecha_hora DATETIME NOT NULL,
    duracion_minutos INT NOT NULL DEFAULT 30,
    estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    notas VARCHAR(500) NULL,
    username_registro VARCHAR(100) NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ticket_id BIGINT NULL,
    INDEX idx_citas_tenant_fecha (tenant_owner, fecha_hora),
    INDEX idx_citas_producto (producto_id),
    INDEX idx_citas_cliente (cliente_id)
);
