-- Preferencias por tenant (umbral de stock bajo personalizado)
USE java_curso;

CREATE TABLE IF NOT EXISTS preferencias_tenant (
    tenant_username VARCHAR(100) PRIMARY KEY,
    stock_minimo INT NULL
);
