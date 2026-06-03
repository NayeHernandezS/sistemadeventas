-- Catalogo de clientes por tenant (idempotente)

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
