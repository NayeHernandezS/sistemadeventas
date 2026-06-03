# Checklist — Piloto y alta de un negocio

Lista de verificación para **publicar el sistema** y **dar de alta el primer cliente** (modelo: vender a negocios, un servidor, varios tenants).

Guía técnica completa: [DEPLOY.md](DEPLOY.md)  
Mercado Pago (cobro SaaS): [CHECKLIST_MERCADOPAGO.md](CHECKLIST_MERCADOPAGO.md)  
Correos de vencimiento: [CHECKLIST_CORREOS.md](CHECKLIST_CORREOS.md)  
Guía para el dueño del negocio: [../docs/GUIA_OPERATIVA_NEGOCIO.md](../docs/GUIA_OPERATIVA_NEGOCIO.md)

---

## A. Antes del piloto (servidor)

### Infraestructura

- [ ] Servidor o PC con Docker (o Java 21 + MySQL 8 + Nginx si sin Docker)
- [ ] Dominio o subdominio apuntando al servidor (producción) o `/etc/hosts` (prueba local)
- [ ] Puertos **80** y **443** abiertos (producción)
- [ ] Copia `.env.example` → `.env` en la raíz del proyecto
- [ ] `DB_PASSWORD` definido (no vacío en producción)
- [ ] `APP_BASE_URL` = URL **HTTPS** pública, sin barra final (ej. `https://ventas.tudominio.com`)

### Base de datos

**Base nueva (recomendado para primer piloto):**

```bash
cd src/main/resources/db
chmod +x reset-db-vacio.sh
./reset-db-vacio.sh
```

- [ ] `java_curso` creada con `schema.sql` (incluye clientes, movimientos, CFDI, etc.)

**Base existente del curso (ya tenía tablas antiguas):**

```bash
chmod +x deploy/scripts/aplicar-migraciones-fase1.sh
./deploy/scripts/aplicar-migraciones-fase1.sh
```

- [ ] Migraciones Fase 1 aplicadas sin error
- [ ] Tablas verificadas: `clientes`, `movimientos_inventario`, columna `facturas.cliente_id`

### Aplicación

```bash
docker compose up -d --build
# o: mvn spring-boot:run  (desarrollo)
```

- [ ] Contenedor `app` en ejecución (`docker compose ps`)
- [ ] Contenedor `db` healthy
- [ ] Nginx responde HTTPS → redirige a login
- [ ] `docker compose logs app` sin errores de conexión a MySQL

### Opcional según el negocio

| Función | Variables / acción |
|---------|-------------------|
| Correo (recuperación, avisos) | `SMTP_*`, `MAIL_FROM` en `.env` |
| Cobro suscripción en línea | `MERCADOPAGO_*`, migración MP, webhook HTTPS |
| CFDI timbrado | `CFDI_FACTURAMA_*`, CSD en panel Facturama, datos fiscales en Perfil |

Para el piloto B2B con cobro manual, **SMTP y Mercado Pago pueden quedar vacíos** al inicio.

### Respaldo

- [ ] Script o cron de backup documentado (ver sección **Backup** al final)
- [ ] Prueba de restauración del dump en otra máquina (opcional pero recomendado)

---

## B. Alta de un negocio (tenant)

Sustituye los valores de ejemplo.

| Dato | Ejemplo |
|------|---------|
| Nombre comercial | Tienda La Esquina |
| Usuario ADMIN | `tiendaesquina` |
| Email ADMIN | `admin@tiendaesquina.com` |
| Plan contratado | Emprendedor / Negocio / Pro |
| Tipo de negocio | abarrotes, ferreteria, etc. |

### Paso 1 — Crear cuenta ADMIN

- [ ] Abrir `{APP_BASE_URL}/registro`
- [ ] Completar formulario (usuario, email, contraseña, tipo de negocio, plan)
- [ ] Confirmar login en `{APP_BASE_URL}/login`
- [ ] Ver panel de inicio con resumen de plan (vendedores/productos)

**Alternativa:** tú creas la cuenta en una reunión y entregas usuario/contraseña temporal (que cambien en Mi perfil).

### Paso 2 — Configuración inicial (contigo o el cliente)

| # | Tarea | Ruta |
|---|--------|------|
| 1 | Subir logo (opcional) | Mi perfil |
| 2 | Umbral stock bajo | Mi perfil → Preferencias |
| 3 | Crear 2–3 categorías | Categorías |
| 4 | Alta de 5–10 productos de prueba | Inventario |
| 5 | Un cliente de prueba con RFC (si facturan) | Clientes |
| 6 | Datos fiscales emisor (si CFDI) | Mi perfil |

- [ ] Categorías creadas
- [ ] Al menos un producto con existencias > 0
- [ ] Venta de prueba: catálogo → carrito → finalizar → ticket visible
- [ ] (Opcional) Venta con factura y ver estado CFDI en ticket → factura

### Paso 3 — Vendedor (opcional)

- [ ] ADMIN → Mis vendedores → crear usuario `vendedor1`
- [ ] Probar login como vendedor: solo ventas e inventario lectura
- [ ] Vendedor no puede entrar a Categorías ni Ajustar stock

### Paso 4 — Entregables al cliente

- [ ] PDF o impreso: [GUIA_OPERATIVA_NEGOCIO.md](../docs/GUIA_OPERATIVA_NEGOCIO.md)
- [ ] Credenciales por canal seguro (no por WhatsApp sin cifrar si es producción)
- [ ] URL de acceso y contacto de soporte (`soporte.email` en `.env`)
- [ ] Acuerdo comercial: plan, precio, soporte 30 días, qué incluye el setup

### Paso 5 — Suscripción del negocio (cobro manual)

Si **no** usas Mercado Pago aún:

- [ ] Activar suscripción manualmente en BD o flujo demo según tu operación
- [ ] Verificar que con plan activo el negocio accede a ventas e inventario
- [ ] Anotar fecha de vencimiento para contactar renovación

Con **Mercado Pago**: el ADMIN renueva en `/suscripcion`; verificar webhook en sandbox/producción.

---

## C. Verificación rápida (5 minutos)

Ejecuta como ADMIN del tenant piloto:

| Prueba | OK |
|--------|-----|
| Login / logout | ☐ |
| Crear producto | ☐ |
| Vender 1 producto (ticket generado) | ☐ |
| Inventario → Ajustar → Entrada (+5) | ☐ |
| Historial de movimientos muestra el ajuste | ☐ |
| Cliente en catálogo + precarga en carrito | ☐ |
| Reportes CSV descarga | ☐ |
| Devolución parcial reintegra stock | ☐ |

---

## D. Segundo negocio en el mismo servidor

No necesitas otra base ni otro Docker por tienda:

1. El nuevo negocio se registra en `/registro` (otro ADMIN = otro tenant).
2. Los datos quedan aislados por `tenant_owner` / `admin_owner`.
3. Mismo `.env`, misma app, misma BD `java_curso`.

- [ ] Probar que `tienda1` no ve productos de `tienda2`

---

## E. SUPER_ADMIN (tu operación de plataforma)

Solo si tú operas el SaaS:

- [ ] Usuario con rol `SUPER_ADMIN` o en `plataforma.superadmins`
- [ ] Acceso a `/plataforma` (clientes, pagos, soporte global)

---

## Backup (producción)

### Dump manual

```bash
# Con Docker
docker compose exec db mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" java_curso \
  > backup-java_curso-$(date +%Y%m%d).sql

# Local
mysqldump -u root -p java_curso > backup-java_curso-$(date +%Y%m%d).sql
```

### Restaurar (prueba)

```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS java_curso; CREATE DATABASE java_curso;"
mysql -u root -p java_curso < backup-java_curso-YYYYMMDD.sql
```

### Logos de tenants

```bash
# Docker: volumen app_uploads
docker compose exec app tar -czf - /app/uploads > backups-uploads-$(date +%Y%m%d).tar.gz
```

- [ ] Backup semanal programado (cron en el servidor)
- [ ] Copia fuera del servidor (otro disco / nube)

---

## Contacto y soporte

Configura en `.env` o `application.properties`:

```properties
soporte.email=tu@correo.com
soporte.whatsapp=521XXXXXXXXXX
soporte.horario=Lunes a viernes 9:00-18:00
```

El ADMIN del negocio lo ve en **Soporte**.

---

## Script de ayuda

```bash
chmod +x deploy/scripts/alta-negocio.sh
./deploy/scripts/alta-negocio.sh
```

Muestra resumen de pasos y comprueba que la URL de login responde.

---

*Checklist Fase 1 — alineado con catálogo de clientes, carrito, movimientos de inventario y reintento CFDI.*
