-- Devoluciones de ventas (idempotente)

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
