# Migraciones con Flyway

El esquema de MySQL se versiona con [Flyway](https://flywaydb.org/). Los scripts viven en:

`src/main/resources/db/migration/`

Convención de nombre: `V{n}__descripcion_corta.sql` (por ejemplo `V24__inventario_lotes.sql`).

## Arranque de la aplicación

Con `FLYWAY_ENABLED=true` (por defecto), Spring Boot ejecuta Flyway **antes** de servir peticiones. Las migraciones son **idempotentes** (mismo estilo que los antiguos `migracion_*.sql`).

## Orden actual (V1–V23)

| Versión | Contenido |
|---------|-----------|
| V1 | Esquema base (`schema.sql` sin `CREATE DATABASE`) |
| V2–V6 | Existencias, multiusuario, tenant, suscripciones, categorías |
| V7 | Documentación SUPER_ADMIN (sin DDL) |
| V8–V23 | Devoluciones, soporte, planes, fiscal, MP, clientes, onboarding, legal, etc. |

Los archivos `migracion_*.sql` en `src/main/resources/db/` se mantienen como referencia manual; **el canal oficial para producción es Flyway**.

## Base nueva

- **Docker:** `schema.sql` en `docker-entrypoint-initdb.d` + Flyway al arrancar la app (V1–V23 en no-op o ajustes menores).
- **Solo Flyway:** base vacía `java_curso` + arrancar la app o `deploy/scripts/flyway-migrate.sh`.

## Base existente (ya migrada a mano)

1. Comprueba que tienes hasta la migración 21 (legal) aplicada.
2. Arranca la app: Flyway crea `flyway_schema_history` y aplica solo versiones pendientes.
3. Si una versión falla por objeto ya existente, revisa el log; los scripts usan `IF NOT EXISTS` / comprobación en `information_schema`.

Para marcar el esquema al día sin reejecutar DDL (caso excepcional):

```bash
# Solo si estás seguro de que el esquema ya coincide con V23
mvn flyway:baseline -Dflyway.baselineVersion=23
```

## Añadir un cambio nuevo

1. Crea `V24__mi_cambio.sql` (nunca edites un `V{n}` ya desplegado en producción).
2. Añade el mismo cambio en `schema.sql` si aplica a instalaciones desde cero.
3. Opcional: `migracion_mi_cambio.sql` legacy para quien aún use `mysql <` a mano.
4. Actualiza README (lista de migraciones) y prueba en local.

## Error tras un intento fallido (checksum o V3 duplicado)

Si el arranque fallo a mitad de una migracion:

```sql
USE java_curso;
DELETE FROM flyway_schema_history WHERE success = 0;
```

Luego vuelve a arrancar la app. Si cambiaste un script ya aplicado, usa `mvn flyway:repair` (ver documentacion Flyway).

## Desactivar Flyway

```properties
FLYWAY_ENABLED=false
```

Útil si ejecutas SQL manualmente en un entorno aislado (no recomendado en prod).

## CLI sin arrancar Tomcat

```bash
source .env   # DB_URL, DB_USER, DB_PASSWORD
./deploy/scripts/flyway-migrate.sh
```

Equivale a `mvn flyway:migrate` con las variables del `.env`.
