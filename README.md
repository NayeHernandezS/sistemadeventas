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

### Esquema base

Este proyecto asume que ya existe la base `java_curso` con las tablas del curso de Java EE:

- `usuarios`, `productos`, `categorias`
- `tickets_venta`, `ticket_items`, `facturas`

Si partes de cero, crea la base y las tablas originales antes de ejecutar las migraciones.

```sql
CREATE DATABASE IF NOT EXISTS java_curso
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Migraciones incrementales

Los scripts en `src/main/resources/db/` extienden el esquema base hacia el SaaS multi-tenant.

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

Pruebas unitarias en `src/test/java` (JUnit 5 + Mockito): suscripciones, limites de plan y devoluciones.

---

## Primer uso

### 1. Registrar una cuenta de negocio

Visita **http://localhost:8080/registro**, elige tipo de negocio y plan. Se crea un usuario **ADMIN** con un mes de prueba gratis.

### 2. Iniciar sesion

**http://localhost:8080/login**

Desde el panel podras acceder a ventas, inventario, categorias, vendedores y reportes.

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
| **ADMIN** | Todo lo anterior + productos, categorias, vendedores, suscripcion, soporte |
| **SUPER_ADMIN** | Panel `/plataforma`: clientes, pagos globales, soporte de todos los tenants |

---

## Modulos principales

- **Ventas** — catalogo, carrito, tickets, facturacion basica (RFC, sin CFDI real)
- **Inventario** — CRUD de productos con existencias; el stock se descuenta al vender y se reintegra en devoluciones
- **Categorias** — CRUD por tenant (solo ADMIN)
- **Devoluciones** — parciales o totales por ticket
- **Suscripciones** — planes Emprendedor ($149), Negocio ($249), Pro ($399); pagos manuales (demo)
- **Reportes** — ventas por vendedor y periodo

---

## Notas importantes

- **Pagos de suscripcion**: el ADMIN solicita el pago en `/suscripcion`; solo un **SUPER_ADMIN** puede confirmarlo en `/plataforma/pagos`. El ADMIN consulta el estado en `/admin/pagos` (solo lectura).
- **Contrasenas**: nuevas cuentas y cambios de password se guardan con **BCrypt** (`{bcrypt}`). Las contrasenas legacy en texto plano siguen funcionando hasta que el usuario cambie la contrasena.
- **Facturacion**: registra datos fiscales y folio interno; no genera XML/PDF CFDI ni timbrado SAT.
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
src/main/resources/db/         Migraciones SQL
```

---

## Solucion de problemas

| Problema | Solucion |
|----------|----------|
| Error de conexion a MySQL | Verifica que MySQL este activo y que `.env` tenga `DB_PASSWORD` correcto |
| `Unknown column 'plan_codigo'` | Ejecuta `migracion_planes.sql` o `migracion_full.sql` |
| Acceso denegado a `/plataforma` | Asigna rol `SUPER_ADMIN` o agrega tu usuario en `plataforma.superadmins` |
| Plan vencido pero accede a devoluciones | Ejecuta la app actualizada; `/devoluciones` ya pasa por `SuscripcionFiltro` |
| Pagina en blanco / error JSP | Confirma JDK 21 y que Tomcat embebido incluya Jasper (ya en `pom.xml`) |
