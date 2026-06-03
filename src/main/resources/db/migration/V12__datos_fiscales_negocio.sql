-- Datos fiscales por defecto del tenant (precarga en carrito al facturar)

CREATE TABLE IF NOT EXISTS datos_fiscales_negocio (
    tenant_username VARCHAR(100) PRIMARY KEY,
    rfc VARCHAR(13) NULL,
    razon_social VARCHAR(200) NULL,
    email VARCHAR(150) NULL,
    direccion VARCHAR(255) NULL,
    uso_cfdi VARCHAR(10) NULL
);
