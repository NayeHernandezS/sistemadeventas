# Despliegue en produccion (HTTPS + SMTP)

Guia para publicar **Sistema de Ventas** con **Docker**, **Nginx + HTTPS** y **correo SMTP**.

---

## Resumen rapido

```bash
cp .env.example .env          # completa passwords, APP_BASE_URL, SMTP, Mercado Pago
./deploy/scripts/certificado-local.sh ventas.local   # prueba local HTTPS
# o certbot en servidor real (ver abajo)
docker compose up -d --build
```

Abre `https://tu-dominio/login` (o `https://ventas.local` en local con `/etc/hosts`).

---

## 1. Variables de entorno (`.env`)

Copia `.env.example` a `.env` y completa:

| Variable | Obligatorio prod | Descripcion |
|----------|------------------|-------------|
| `DB_PASSWORD` | Si | Contrasena MySQL del usuario `DB_USER` |
| `APP_BASE_URL` | Si | URL publica **HTTPS** sin barra final, ej. `https://ventas.tudominio.com` |
| `SMTP_HOST` | Recomendado | Servidor SMTP |
| `SMTP_USER` / `SMTP_PASSWORD` | Recomendado | Credenciales SMTP |
| `MAIL_FROM` | Recomendado | Remitente (debe estar autorizado en tu SMTP) |
| `MERCADOPAGO_ACCESS_TOKEN` | Si cobras online | Token produccion `APP_USR-...` |
| `MERCADOPAGO_WEBHOOK_SECRET` | Si cobras online | Firma de webhooks del panel MP |

### SMTP — ejemplos

**Gmail / Google Workspace** (usa [contrasena de aplicacion](https://myaccount.google.com/apppasswords)):

```properties
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu@gmail.com
SMTP_PASSWORD=xxxx xxxx xxxx xxxx
SMTP_STARTTLS=true
SMTP_SSL=false
MAIL_FROM=tu@gmail.com
```

**SendGrid**:

```properties
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USER=apikey
SMTP_PASSWORD=SG.tu_api_key
MAIL_FROM=noreply@tudominio.com
```

**Puerto 465 (SSL implicito)**:

```properties
SMTP_PORT=465
SMTP_STARTTLS=false
SMTP_SSL=true
```

Sin `SMTP_HOST`, la app arranca pero la recuperacion de contraseña muestra el enlace en pantalla (modo demo).

---

## 2. HTTPS

### Opcion A — Prueba local (certificado autofirmado)

```bash
chmod +x deploy/scripts/certificado-local.sh
./deploy/scripts/certificado-local.sh ventas.local
```

Agrega a `/etc/hosts`:

```
127.0.0.1  ventas.local
```

En `.env`:

```properties
APP_BASE_URL=https://ventas.local
```

El navegador avisara que el certificado no es de confianza; acepta la excepcion para probar.

### Opcion B — Produccion (Let's Encrypt)

1. Apunta el DNS de `ventas.tudominio.com` a la IP del servidor.
2. Abre puertos **80** y **443**.
3. Deten nginx si esta usando el puerto 80:

```bash
docker compose stop nginx
```

4. Ejecuta certbot:

```bash
chmod +x deploy/scripts/certbot-inicial.sh
export DOMAIN=ventas.tudominio.com
export CERTBOT_EMAIL=admin@tudominio.com
./deploy/scripts/certbot-inicial.sh
```

5. En `.env`:

```properties
APP_BASE_URL=https://ventas.tudominio.com
```

6. Levanta de nuevo:

```bash
docker compose up -d
```

Renueva certificados cada ~90 dias (cron con `certbot renew`).

---

## 3. Docker Compose

```bash
docker compose up -d --build
```

Servicios:

| Servicio | Puerto | Funcion |
|----------|--------|---------|
| `db` | 3306 (interno) | MySQL 8 + schema inicial |
| `app` | 8080 (interno) | Spring Boot (perfil `prod`) |
| `nginx` | 80, 443 | HTTPS, proxy a la app |

Volúmenes persistentes:

- `mysql_data` — base de datos
- `app_uploads` — logos de tenant

Logs de arranque (perfil prod):

```bash
docker compose logs -f app
```

Deberias ver comprobaciones de `APP_BASE_URL`, SMTP y Mercado Pago.

---

## 4. Mercado Pago en produccion

Checklist detallado: **[CHECKLIST_MERCADOPAGO.md](CHECKLIST_MERCADOPAGO.md)**

```bash
chmod +x deploy/scripts/verificar-mercadopago.sh
./deploy/scripts/verificar-mercadopago.sh
```

1. Token de **produccion** en `MERCADOPAGO_ACCESS_TOKEN` (`APP_USR-...`).
2. `APP_BASE_URL=https://tu-dominio.com` (HTTPS real).
3. Webhook en [panel MP](https://www.mercadopago.com.mx/developers/panel/app):
   - URL: `https://tu-dominio.com/api/mercadopago/notificaciones`
   - Copia el **secret** a `MERCADOPAGO_WEBHOOK_SECRET`.
4. BD nueva: `schema.sql` en el contenedor `db` + **Flyway** al arrancar la app (V1–V23, ver [docs/FLYWAY.md](../docs/FLYWAY.md)).
5. Pagos MP pendientes expiran automaticamente a las **03:30** (America/Mexico_City); tambien puedes expirar desde `/plataforma/pagos`.

---

## 5. Migraciones en BD existente

**Recomendado:** arranca la app o ejecuta Flyway (aplica solo versiones pendientes en `flyway_schema_history`):

```bash
chmod +x deploy/scripts/flyway-migrate.sh
./deploy/scripts/flyway-migrate.sh
```

Documentacion: [docs/FLYWAY.md](../docs/FLYWAY.md).

**Legacy (manual):**

```bash
mysql -u root -p java_curso < src/main/resources/db/migracion_full.sql
```

---

## 6. Verificar SMTP

Checklist detallado: **[CHECKLIST_CORREOS.md](CHECKLIST_CORREOS.md)**

```bash
chmod +x deploy/scripts/verificar-smtp.sh
./deploy/scripts/verificar-smtp.sh
mysql -u root -p java_curso < src/main/resources/db/migracion_suscripcion_correos_enviados.sql
```

1. Arranca la app con SMTP configurado.
2. En `/recuperar`, solicita restablecer contraseña con un email registrado.
3. Debe llegar el correo (revisa spam).
4. Avisos de suscripcion: job **08:00** (Mexico) + boton **Enviar avisos ahora** en `/plataforma`.
5. Tabla `suscripcion_correos_enviados` evita correos duplicados por ciclo de vencimiento.

---

## 7. Despliegue sin Docker (VPS + JAR)

```bash
mvn -DskipTests package
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=...
export APP_BASE_URL=https://ventas.tudominio.com
export SMTP_HOST=...
java -jar target/sistema-ventas.war
```

Coloca Nginx delante con la misma config en `deploy/nginx/conf.d/app.conf` apuntando a `127.0.0.1:8080`.

---

## 8. Piloto y alta de un negocio

- Checklist operativo: [CHECKLIST_PILOTO.md](CHECKLIST_PILOTO.md)
- Script resumen: `./deploy/scripts/alta-negocio.sh`
- Migraciones Fase 1 en BD existente: `./deploy/scripts/aplicar-migraciones-fase1.sh`
- Guia para el cliente: [../docs/GUIA_OPERATIVA_NEGOCIO.md](../docs/GUIA_OPERATIVA_NEGOCIO.md)

---

## 9. Copias de seguridad

Respaldar periodicamente:

- Volumen MySQL (`mysql_data`) o dump: `mysqldump java_curso > backup.sql`
- Volumen `app_uploads` (logos)

---

## Solucion de problemas

| Problema | Solucion |
|----------|----------|
| Mercado Pago no redirige | `APP_BASE_URL` debe ser HTTPS publico |
| Webhook MP 401 | Revisa `MERCADOPAGO_WEBHOOK_SECRET` |
| Correo no llega | Verifica SMTP, `MAIL_FROM` autorizado, logs `docker compose logs app` |
| Cookie de sesion se pierde | Nginx debe enviar `X-Forwarded-Proto https` (ya incluido) |
| Error certificado nginx | Genera certs en `deploy/certs/` o ejecuta certbot |
| BD no conecta | Espera healthcheck de `db`; revisa `DB_PASSWORD` |
