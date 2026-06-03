# Sistema de Ventas

Aplicacion web SaaS multi-cuenta para gestionar ventas, inventario, tickets, devoluciones y suscripciones. Desarrollada con **Spring Boot 3.4**, **Java 21**, **JSP** y **MySQL**.

Cada cuenta **ADMIN** opera su propio negocio (tenant) con vendedores, productos, categorias y reportes. Un operador **SUPER_ADMIN** administra la plataforma completa.

---

## Requisitos

| Componente | Version |
|------------|---------|
| Java JDK   | 21      |
| Maven      | 3.9+    |
| MySQL      | 8.x     |

Herramientas recomendadas: IntelliJ IDEA, MySQL Workbench.

---

## Base de datos

### Instalacion desde cero

**Tablas vacias (recomendado para produccion o tu propio alta de usuarios):**

```bash
cd src/main/resources/db
chmod +x reset-db-vacio.sh
./reset-db-vacio.sh
```

Equivalente manual (lee `.env` si usas el script; a mano sustituye usuario/contraseña):

```bash
cd src/main/resources/db
mysql -u root -p -e "DROP DATABASE IF EXISTS java_curso;"
mysql -u root -p < schema.sql
```

O en un solo paso desde la carpeta `db/`:

```bash
mysql -u root -p < bootstrap_vacio.sql
```

**Con datos de demostracion** (usuarios y productos de prueba):

```bash
cd src/main/resources/db
mysql -u root -p < schema.sql
mysql -u root -p < datos_ejemplo.sql
```

Credenciales de prueba (`datos_ejemplo.sql`):

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `tienda1` | `admin123` | ADMIN de negocio |
| `vendedor1` | `vendedor1` | VENDEDOR de tienda1 |
| `plataforma` | `plataforma1` | SUPER_ADMIN |

### Esquema base (curso Java EE)

Si ya tienes la base `java_curso` del curso con tablas originales, puedes usar las migraciones incrementales en lugar de `schema.sql`.

Tablas originales del curso:

- `usuarios`, `productos`, `categorias`
- `tickets_venta`, `ticket_items`, `facturas`

Si partes de cero sin `schema.sql`, crea la base antes de migrar:

```sql
CREATE DATABASE IF NOT EXISTS java_curso
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Migraciones con Flyway (recomendado)

Al arrancar la app, **Flyway** aplica `src/main/resources/db/migration/` (V1–V23). Guia completa: **[docs/FLYWAY.md](docs/FLYWAY.md)**.

```bash
# Solo migrar, sin levantar Tomcat
chmod +x deploy/scripts/flyway-migrate.sh
./deploy/scripts/flyway-migrate.sh
```

Nuevos cambios de esquema: crea `V24__descripcion.sql` (no edites versiones ya desplegadas). Desactivar: `FLYWAY_ENABLED=false` en `.env`.

En tests de integracion Flyway esta desactivado; se usa `schema-test.sql`.

### Migraciones incrementales (manual / legacy)

Los scripts en `src/main/resources/db/` extienden el esquema base hacia el SaaS multi-tenant. Equivalentes a V2–V23 en Flyway.

**Opcion A — MySQL CLI** (desde la carpeta `db/`):

```bash
cd src/main/resources/db
mysql -u root -p
```

```sql
SOURCE migracion_full.sql;
```

**Opcion B — MySQL Workbench** (un solo archivo):

Abre y ejecuta `src/main/resources/db/migracion_full_workbench.sql`.

Ambas opciones son **idempotentes**: se pueden reejecutar sin romper columnas ya existentes.

Orden de migraciones (`migracion_full.sql`):

1. Existencias en productos
2. Multiusuario (`owner_username`)
3. Tenant (`admin_owner`, `tenant_owner`)
4. Suscripciones y pagos
5. Categorias por tenant + tipo de negocio
6. Documentacion SUPER_ADMIN
7. Devoluciones
8. Soporte
9. Planes (`plan_codigo`)
10. Recuperación de contraseña (`tokens_recuperacion`)
11. Datos fiscales por defecto del negocio (`datos_fiscales_negocio`)
12. Preferencias del tenant (`preferencias_tenant`)
13. Mercado Pago (`migracion_mercadopago.sql`)
14. Renovacion automatica MP (`migracion_renovacion_automatica.sql`)
15. Logo por tenant (`migracion_tenant_logo.sql`)
16. Catalogo de clientes (`migracion_clientes.sql`)
17. Factura vinculada a cliente (`migracion_factura_cliente.sql`)
18. Movimientos de inventario (`migracion_movimientos_inventario.sql`)
19. Registro de correos de aviso de suscripcion (`migracion_suscripcion_correos_enviados.sql`)
20. Onboarding post-registro (`migracion_onboarding_tenant.sql`)
21. Aceptacion legal en registro (`migracion_aceptacion_legal.sql`)

---

## Despliegue en produccion (HTTPS + SMTP)

Guia detallada: **[deploy/DEPLOY.md](deploy/DEPLOY.md)**

```bash
cp .env.example .env
# Edita .env: DB_PASSWORD, APP_BASE_URL=https://..., SMTP_*, MERCADOPAGO_*
./deploy/scripts/certificado-local.sh ventas.local   # prueba local
docker compose up -d --build
```

Variables clave en `.env`:

| Variable | Uso |
|----------|-----|
| `APP_BASE_URL` | URL HTTPS publica (Mercado Pago, correos, recuperacion de contraseña) |
| `SMTP_HOST`, `SMTP_USER`, `SMTP_PASSWORD`, `MAIL_FROM` | Correo transaccional |
| `MERCADOPAGO_ACCESS_TOKEN`, `MERCADOPAGO_WEBHOOK_SECRET` | Pagos en linea (checklist: [deploy/CHECKLIST_MERCADOPAGO.md](deploy/CHECKLIST_MERCADOPAGO.md)) |

En Docker la app arranca con perfil **`prod`** (cookies seguras, cabeceras detras de Nginx, validacion al inicio).

---

## Configuracion

### Variables de entorno (`.env`)

Copia el archivo de ejemplo en la raiz del proyecto:

```bash
cp .env.example .env
```

Edita `.env` con tus credenciales:

```properties
DB_URL=jdbc:mysql://localhost:3306/java_curso?serverTimezone=America/Mexico_City
DB_USER=root
DB_PASSWORD=tu_password
```

Spring Boot carga `.env` automaticamente via `spring.config.import` en `application.properties`.

### Propiedades adicionales

En `src/main/resources/application.properties`:

| Propiedad | Descripcion | Default |
|-----------|-------------|---------|
| `suscripcion.meses.gratis` | Meses de prueba al registrarse | `1` |
| `plataforma.superadmins` | Usernames con acceso a `/plataforma` (separados por coma) | `admin` |
| `soporte.email` | Email visible en `/soporte` | `soporte@misistema.com` |
| `soporte.whatsapp` | Numero WhatsApp (opcional) | vacio |
| `soporte.horario` | Horario de atencion | L-V 9-18 |
| `inventario.stock.minimo` | Umbral de alerta de stock bajo (unidades) | `5` |
| `recuperacion.token.horas` | Validez del enlace de recuperación | `2` |
| `recuperacion.mail.from` | Remitente del correo de recuperación | `noreply@misistema.com` |
| `app.base-url` | URL pública HTTPS de la app (correos, webhooks Mercado Pago) | vacío (se deduce del request en local) |
| `mercadopago.access-token` | Access Token de Mercado Pago (MX) | vacío = pago manual |
| `mercadopago.webhook-secret` | Secret de firma de webhooks MP | vacío = no validar (solo dev) |
| `mercadopago.enabled` | Activar Checkout Pro (`true`/`false`) | auto si hay token |
| `suscripcion.aviso.dias` | Días antes del vencimiento para aviso in-app | `7` |
| `suscripcion.pago.pendiente.dias.manual` | Días antes de expirar pago manual PENDIENTE | `30` |
| `suscripcion.pago.pendiente.dias.mercadopago` | Días antes de expirar pago MP PENDIENTE (SPEI/OXXO) | `15` |
| `mercadopago.currency-id` | Moneda del cobro | `MXN` |
| `app.base-url` / `APP_BASE_URL` | URL publica HTTPS | vacío en local |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` | Correo SMTP | vacío = modo demo |
| `MAIL_FROM` | Remitente de correos | `noreply@misistema.com` |
| `spring.mail.host` | (alias de `SMTP_HOST`) | — |

Tambien puedes sobreescribirlas en `.env` usando el mismo nombre de propiedad.

---

## Ejecucion

### Con Maven

```bash
mvn spring-boot:run
```

La aplicacion queda disponible en: **http://localhost:8080**

### Con IntelliJ

1. Importa el proyecto como Maven.
2. Configura el JDK 21.
3. Crea `.env` en la raiz del proyecto.
4. Ejecuta `SistemaVentasApplication`.

### Empaquetado WAR

```bash
mvn clean package
```

Genera `target/sistema-ventas.war` para desplegar en un servidor Tomcat externo.

### Tests

```bash
mvn test
```

Pruebas unitarias en `src/test/java` (JUnit 5 + Mockito): suscripciones, limites de plan, devoluciones, ventas, reportes, inventario y exportacion CSV.

---

## Primer uso

### 1. Registrar una cuenta de negocio

Visita **http://localhost:8080/registro**, elige tipo de negocio y plan y acepta **Terminos** y **Privacidad** (version vigente en pantalla). Se crea un usuario **ADMIN** con un mes de prueba gratis. Al iniciar sesion, un asistente de 3 pasos guia categorias, primer producto y ventas (`/onboarding`). Documentos: `docs/TERMINOS_SERVICIO.md`, `docs/AVISO_PRIVACIDAD.md`.

### 2. Iniciar sesion

**http://localhost:8080/login**

Desde el panel podras acceder a ventas, inventario, categorias, vendedores y reportes.

**Guia para el dueño del negocio (operacion diaria):** [docs/GUIA_OPERATIVA_NEGOCIO.md](docs/GUIA_OPERATIVA_NEGOCIO.md) — entregable en PDF o impreso al dar de alta un cliente.

**Checklist piloto y alta de negocio:** [deploy/CHECKLIST_PILOTO.md](deploy/CHECKLIST_PILOTO.md) — despliegue, migraciones Fase 1, verificacion y backup.

```bash
chmod +x deploy/scripts/aplicar-migraciones-fase1.sh deploy/scripts/alta-negocio.sh
./deploy/scripts/alta-negocio.sh
```

### 3. Configurar SUPER_ADMIN (panel plataforma)

Opcion A — SQL:

```sql
USE java_curso;
SELECT id, username, rol FROM usuarios;
UPDATE usuarios SET rol = 'SUPER_ADMIN' WHERE id = 1;  -- usa tu id real
```

Opcion B — `application.properties`:

```properties
plataforma.superadmins=tu_usuario
```

Cierra sesion y vuelve a entrar. El SUPER_ADMIN es redirigido a **/plataforma**.

---

## Roles

| Rol | Acceso |
|-----|--------|
| **VENDEDOR** | Ventas, carrito, tickets, reportes, inventario (solo lectura) |
| **ADMIN** | Todo lo anterior + productos, categorias, clientes, ajustes de inventario, vendedores, suscripcion, soporte, reintento CFDI |
| **SUPER_ADMIN** | Panel `/plataforma`: clientes, pagos globales (confirmar/expirar/historial), soporte de todos los tenants |

---

## Modulos principales

- **Ventas** — catalogo, carrito, tickets, facturacion (PDF informativo; CFDI Facturama con reintento desde la vista de factura)
- **Inventario** — CRUD de productos con existencias; entradas, salidas y ajustes con historial; alertas de stock bajo y agotado; el stock se descuenta al vender y se reintegra en devoluciones
- **Categorias** — CRUD por tenant (solo ADMIN)
- **Clientes** — catalogo por tenant (ADMIN alta/edita; vendedores consultan); seleccion en carrito precarga RFC y datos fiscales al facturar
- **Devoluciones** — parciales o totales por ticket
- **Suscripciones** — planes Emprendedor ($149), Negocio ($249), Pro ($399); **Mercado Pago** (Checkout Pro, MXN) o pago manual (demo); **renovación automática mensual** (MP Preapproval); expiración de pagos pendientes
- **Reportes** — ventas por vendedor y periodo; totales netos restando devoluciones; exportacion CSV; resumen en inicio (ADMIN): hoy, semana, mes y top productos
- **Perfil** — datos de cuenta, email, tipo de negocio, uso del plan, datos fiscales por defecto (ADMIN), preferencias de stock (ADMIN), actividad del vendedor, resumen de suscripcion (ADMIN) y cambio de contraseña (`/perfil`)
- **Recuperación de acceso** — olvidé mi contraseña por email (`/recuperar`); modo demo sin SMTP

---

## Notas importantes

- **Pagos de suscripcion**: con `MERCADOPAGO_ACCESS_TOKEN` el ADMIN paga en línea en `/suscripcion` (tarjeta, SPEI, OXXO según MP) y el plan se activa por webhook. Sin token, el flujo manual sigue igual. Los pagos **PENDIENTE** expiran solos (15 días MP / 30 días manual). **Renovación automática**: activa cobro mensual con MP Preapproval en `/suscripcion`.
- **Acceso sin plan activo**: ventas, inventario, reportes y demas modulos quedan bloqueados hasta renovar; disponibles `/perfil`, `/suscripcion`, `/soporte`, `/admin/pagos` (ADMIN) y el inicio con aviso (`/?sinPlan=1` para vendedores).
- **Mi perfil** (`/perfil`): datos de cuenta, email, tipo de negocio (ADMIN), uso del plan, datos fiscales por defecto (ADMIN), preferencias de stock bajo por tenant (ADMIN), actividad mensual y tickets recientes (VENDEDOR), resumen de suscripcion (ADMIN) y cambio de contraseña. Los datos fiscales se precargan en el carrito al facturar.
- **Alertas de stock**: el umbral global se configura en `inventario.stock.minimo`; cada ADMIN puede personalizarlo en `/perfil` → Preferencias del negocio.
- **Contrasenas**: nuevas cuentas y cambios de password se guardan con **BCrypt** (`{bcrypt}`). Las contrasenas legacy en texto plano siguen funcionando hasta que el usuario cambie la contrasena. Recuperacion en `/recuperar` (requiere tabla `tokens_recuperacion` y opcionalmente SMTP).
- **Facturacion**: registra datos fiscales, folio interno y PDF descargable desde `/factura`; no genera XML/PDF CFDI ni timbrado SAT.
- No subas `.env` al repositorio (contiene credenciales).

---

## Estructura del proyecto

```
src/main/java/.../sistemaventas/
  config/       Seguridad, filtros, conexion JDBC
  web/          Controllers Spring MVC
  services/     Logica de negocio
  repositories/ Acceso JDBC a MySQL
  models/       Entidades y DTOs

src/main/webapp/WEB-INF/jsp/   Vistas JSP
src/main/resources/db/         SQL legacy + schema.sql
src/main/resources/db/migration/  Migraciones Flyway (V1..Vn)
```

---

## Mercado Pago (Mexico)

1. Crea una aplicación en [Mercado Pago Developers](https://www.mercadopago.com.mx/developers) y copia el **Access Token** de prueba (`TEST-...`) o producción.
2. En `.env`:
   ```properties
   MERCADOPAGO_ACCESS_TOKEN=TEST-...
   APP_BASE_URL=https://tu-dominio.com
   ```
3. Ejecuta `migracion_mercadopago.sql` (o `migracion_full.sql`) en MySQL.
4. En produccion la URL de notificacion debe ser **HTTPS** y accesible desde internet: `{APP_BASE_URL}/api/mercadopago/notificaciones`.
5. Copia el **secret de firma** del panel (Webhooks → Configurar) en `MERCADOPAGO_WEBHOOK_SECRET` — en produccion es obligatorio.
6. Usuarios de prueba: [cuentas de test MP](https://www.mercadopago.com.mx/developers/es/docs/checkout-pro/additional-content/test-users).

En local sin tunel (ngrok, etc.) el webhook no llegara; tras pagar en sandbox, la pagina `/suscripcion/pago-exitoso` intenta confirmar el pago como respaldo.

---

## Solucion de problemas

| Problema | Solucion |
|----------|----------|
| Error de conexion a MySQL | Verifica que MySQL este activo y que `.env` tenga `DB_PASSWORD` correcto |
| `Unknown column 'plan_codigo'` | Ejecuta `migracion_planes.sql` o `migracion_full.sql` |
| Acceso denegado a `/plataforma` | Asigna rol `SUPER_ADMIN` o agrega tu usuario en `plataforma.superadmins` |
| Plan vencido pero accede al sistema | Sin suscripcion activa solo `/perfil`, `/suscripcion`, `/soporte`, `/admin/pagos` (ADMIN) e inicio con aviso; el resto redirige a renovacion o `/?sinPlan=1` |
| Error al guardar datos fiscales en perfil | Ejecuta `migracion_datos_fiscales_negocio.sql` en bases existentes |
| Error al guardar preferencias en perfil | Ejecuta `migracion_preferencias_tenant.sql` en bases existentes |
| Mercado Pago no redirige / no activa plan | Revisa `APP_BASE_URL` HTTPS, token en `.env` y que corriste `migracion_mercadopago.sql` |
| Error al activar renovacion automatica | Ejecuta `migracion_renovacion_automatica.sql` en bases existentes |
| Pagina en blanco / error JSP | Confirma JDK 21 y que Tomcat embebido incluya Jasper (ya en `pom.xml`) |
