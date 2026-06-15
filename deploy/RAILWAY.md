# Despliegue en Railway

Guia para publicar **Fusion Digital / Sistema de Ventas** en [Railway](https://railway.app) con MySQL.

El error **"Application failed to respond"** casi siempre significa que el contenedor no escucha en el puerto que Railway espera, o que la app **no arranco** (base de datos, Flyway, variables faltantes).

---

## Arquitectura en Railway

```
Internet → Railway (HTTPS) → tu servicio Java (puerto $PORT) → MySQL (plugin Railway)
```

No uses `docker-compose` en Railway: despliega solo el **Dockerfile** de la raiz y agrega **MySQL** como servicio aparte.

---

## Paso 1 — Crear proyecto

1. **New Project** → **Deploy from GitHub repo** → selecciona `sistemadeventas`.
2. Railway detecta el `Dockerfile` y construye la imagen.

---

## Paso 2 — Agregar MySQL

1. En el proyecto: **+ New** → **Database** → **MySQL**.
2. Abre el servicio MySQL → pestaña **Variables** o **Connect**.
3. En el servicio **de la app** (no en MySQL), agrega referencias a las variables del plugin.

Variables minimas en el servicio **app**:

| Variable | Valor en Railway |
|----------|------------------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true&useSSL=false` |
| `DB_USER` | `${{MySQL.MYSQLUSER}}` |
| `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
| `FLYWAY_ENABLED` | `true` |
| `APP_BASE_URL` | `https://${{RAILWAY_PUBLIC_DOMAIN}}` |

> Sustituye `MySQL` por el **nombre exacto** de tu servicio MySQL en Railway si es distinto (ej. `MySQL-abc1`).

Alternativa sin `DB_URL` manual: la app ya lee `MYSQLHOST`, `MYSQLPORT`, etc. si las **referencias** del plugin estan en el servicio app (Railway las inyecta al vincular servicios).

---

## Paso 3 — Dominio publico

1. Servicio app → **Settings** → **Networking** → **Generate Domain**.
2. Obtendras algo como `https://sistemadeventas-production-xxxx.up.railway.app`.
3. Confirma que `APP_BASE_URL` sea esa URL con `https://` (sin barra final).

---

## Paso 4 — Redeploy

1. **Deploy** → espera build verde.
2. Abre **Deployments** → ultimo deploy → **View logs**.

### Logs que indican exito

```
Started SistemaVentasApplication
Perfil prod activo — comprobando configuracion de despliegue…
Flyway: version actual …
```

### Prueba

Abre: `https://TU-DOMINIO.up.railway.app/login`

---

## Si sigue "Application failed to respond"

Revisa los logs del deploy en este orden:

### 1. Puerto (muy comun)

La app debe usar `server.port=${PORT}` (ya configurado en `application-prod.properties`).

Si cambiaste el Dockerfile, vuelve a desplegar.

### 2. Base de datos

Errores tipicos en logs:

```
Communications link failure
Access denied for user
FlywayException
```

**Solucion:** verifica `DB_URL`, `DB_USER`, `DB_PASSWORD` y que el servicio MySQL este **Running** y **vinculado** al servicio app.

### 3. Flyway / migraciones

Si Flyway falla, Spring no termina de arrancar.

- Revisa que `FLYWAY_ENABLED=true` y la BD este vacia o compatible.
- Primera vez: Flyway crea tablas desde `db/migration/`.

### 4. Tiempo de arranque

El primer deploy puede tardar 2–3 minutos (Maven en build + Flyway).

En Railway → Settings → aumenta **Healthcheck Timeout** si existe la opcion.

### 5. Memoria

Java 21 + Spring + JSP necesita al menos **512 MB** RAM. En plan gratuito, sube a 1 GB si el contenedor se mata (OOM en logs).

---

## Variables opcionales (despues del primer arranque)

| Variable | Para que |
|----------|----------|
| `SMTP_HOST`, `SMTP_USER`, `SMTP_PASSWORD`, `MAIL_FROM` | Correos |
| `MERCADOPAGO_ACCESS_TOKEN`, `MERCADOPAGO_WEBHOOK_SECRET` | Pagos |
| `APP_UPLOADS_DIR` | `/app/uploads` (persistencia: volumen Railway) |

---

## PWA / app movil en Railway

1. La URL publica HTTPS de Railway es la que instalas en el movil.
2. `APP_BASE_URL` debe coincidir con ese dominio.
3. No hace falta otro despliegue para la app movil: es la misma URL.

---

## Checklist rapido

- [ ] Servicio MySQL creado y en ejecucion
- [ ] Variables `DB_*` referenciadas en el servicio app
- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `APP_BASE_URL=https://....up.railway.app`
- [ ] Dominio publico generado en Networking
- [ ] Logs muestran `Started SistemaVentasApplication`
- [ ] `/login` abre en el navegador

---

## Soporte

Si tras revisar logs el error persiste, copia las **ultimas 50 lineas** del deploy (donde aparece `ERROR` o `Exception`) para diagnosticar el caso concreto.
