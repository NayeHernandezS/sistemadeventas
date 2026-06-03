-- Migracion completa para base existente (orden recomendado)
-- Canal preferido en despliegues: Flyway (src/main/resources/db/migration, ver docs/FLYWAY.md)
-- Ejecutar desde la carpeta db/ para que SOURCE resuelva rutas relativas.
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

-- 6) Rol SUPER_ADMIN / panel plataforma (documentacion)
SOURCE migracion_super_admin.sql;

-- 7) Devoluciones de ventas
SOURCE migracion_devoluciones.sql;

-- 8) Solicitudes de soporte
SOURCE migracion_soporte.sql;

-- 9) Planes de suscripcion (3 niveles)
SOURCE migracion_planes.sql;

-- 10) Recuperacion de contraseña
SOURCE migracion_recuperacion_password.sql;

-- 11) Datos fiscales por defecto del negocio (perfil / carrito)
SOURCE migracion_datos_fiscales_negocio.sql;

-- 12) Preferencias del tenant (perfil / alertas inventario)
SOURCE migracion_preferencias_tenant.sql;

-- 13) Mercado Pago (referencias en pagos_suscripcion)
SOURCE migracion_mercadopago.sql;

SOURCE migracion_renovacion_automatica.sql;

-- 14) Logo de marca por tenant
SOURCE migracion_tenant_logo.sql;

-- 15) CFDI timbrado (Facturama)
SOURCE migracion_cfdi.sql;

-- 16) Catalogo de clientes por tenant
SOURCE migracion_clientes.sql;

-- 17) Factura vinculada a cliente del catalogo
SOURCE migracion_factura_cliente.sql;

-- 18) Movimientos de inventario
SOURCE migracion_movimientos_inventario.sql;

-- 19) Idempotencia correos de aviso de suscripcion
SOURCE migracion_suscripcion_correos_enviados.sql;

-- 20) Onboarding post-registro
SOURCE migracion_onboarding_tenant.sql;

-- 21) Aceptacion legal en registro
SOURCE migracion_aceptacion_legal.sql;
