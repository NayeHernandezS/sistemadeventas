-- Movimientos de inventario (entradas, salidas, ajustes) por tenant

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
