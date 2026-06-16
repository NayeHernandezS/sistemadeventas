-- Recetas de platillos (restaurante): costo por ingrediente y margen

CREATE TABLE IF NOT EXISTS recetas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_owner VARCHAR(100) NOT NULL,
    producto_id BIGINT NOT NULL,
    UNIQUE INDEX uk_recetas_tenant_producto (tenant_owner, producto_id),
    INDEX idx_recetas_tenant (tenant_owner)
);

CREATE TABLE IF NOT EXISTS receta_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receta_id BIGINT NOT NULL,
    insumo_producto_id BIGINT NOT NULL,
    cantidad DECIMAL(12, 4) NOT NULL,
    unidad VARCHAR(20) NOT NULL DEFAULT 'pza',
    INDEX idx_receta_lineas_receta (receta_id),
    INDEX idx_receta_lineas_insumo (insumo_producto_id)
);
