# Checklist — Correo transaccional (Fase 2, Tarea 2)

Avisos automaticos de **vencimiento de suscripcion** del SaaS (no correos de ventas del negocio).

---

## A. SMTP en `.env`

- [ ] `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` configurados
- [ ] `MAIL_FROM` autorizado en tu proveedor (Gmail, SendGrid, SES, etc.)
- [ ] `APP_BASE_URL` con HTTPS (enlaces en correos)

```bash
chmod +x deploy/scripts/verificar-smtp.sh
./deploy/scripts/verificar-smtp.sh
```

---

## B. Migracion de base de datos

- [ ] Tabla `suscripcion_correos_enviados` creada (evita reenvios duplicados)

```bash
mysql -u root -p java_curso < src/main/resources/db/migracion_suscripcion_correos_enviados.sql
```

O ejecutar `migracion_full.sql` (entrada 19).

---

## C. Que envia el sistema

| Momento | Tipo registro | Destinatario |
|---------|---------------|--------------|
| 7 dias antes | `AVISO_7` | Email del ADMIN del tenant |
| 3 dias antes | `AVISO_3` | Idem |
| 1 dia antes | `AVISO_1` | Idem |
| Dia del vencimiento | `AVISO_0` | Idem |
| Dia despues de vencer | `VENCIDO` | Idem (acceso limitado) |

- Job programado: **08:00** `America/Mexico_City` (`suscripcion.aviso.cron`)
- Aviso **in-app** en paralelo (`suscripcion.aviso.dias`, default 7)

Si el tenant **renueva** y cambia `fecha_fin`, puede recibir nuevos avisos para el nuevo ciclo (clave unica por `fecha_vencimiento_ref`).

---

## D. Prueba manual

1. Usuario ADMIN con email real en `usuarios.email`.
2. Ajustar en MySQL `suscripciones.fecha_fin` a dentro de 7 dias (solo prueba).
3. En `/plataforma` → **Enviar avisos ahora** (SUPER_ADMIN).
4. Revisar bandeja y tabla `suscripcion_correos_enviados`.
5. Repetir el boton: debe enviar **0** correos nuevos (idempotencia).

---

## E. Sin SMTP

- La app sigue funcionando; avisos solo en pantalla (inicio, suscripcion).
- Recuperacion de contraseña muestra enlace en pantalla (modo demo).

---

## F. Propiedades opcionales

| Propiedad | Default | Uso |
|-----------|---------|-----|
| `suscripcion.aviso.dias` | `7` | Umbral in-app y maximo dia de aviso por correo |
| `suscripcion.aviso.cron` | `0 0 8 * * *` | Horario del job |
| `MAIL_FROM` | — | Remitente (tambien recuperacion de contraseña) |
