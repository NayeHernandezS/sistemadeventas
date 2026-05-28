-- Migracion completa para base existente (orden recomendado)
USE java_curso;

-- 1) Existencias en productos
SOURCE migracion_existencias.sql;

-- 2) Multiusuario base
SOURCE migracion_multiusuario.sql;

-- 3) Tenant (admin_owner / tenant_owner)
SOURCE migracion_tenant.sql;

-- 4) Suscripciones y pagos
SOURCE migracion_suscripciones.sql;

-- 5) Categorias por tenant + tipo de negocio
SOURCE migracion_categorias_tenant.sql;
