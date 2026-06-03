# Checklist — Mercado Pago en produccion (Fase 2, Tarea 1)

Cobro en linea de **suscripciones del SaaS** (planes Emprendedor / Negocio / Empresa). No confundir con CFDI de ventas del negocio.

Guia de despliegue general: [DEPLOY.md](DEPLOY.md)

---

## A. Credenciales y entorno

- [ ] Cuenta [Mercado Pago Developers](https://www.mercadopago.com.mx/developers/panel/app) con aplicacion creada
- [ ] **Produccion:** `MERCADOPAGO_ACCESS_TOKEN` con prefijo `APP_USR-...` (no el placeholder del `.env.example`)
- [ ] **Pruebas:** token `TEST-...` solo en entorno de prueba
- [ ] `APP_BASE_URL=https://tu-dominio.com` (HTTPS real, sin barra final, sin `localhost`)
- [ ] `MERCADOPAGO_WEBHOOK_SECRET` copiado del panel → Webhooks → Configurar notificaciones
- [ ] Opcional: `MERCADOPAGO_ENABLED=false` para desactivar MP sin quitar el token

Verificacion rapida:

```bash
chmod +x deploy/scripts/verificar-mercadopago.sh
./deploy/scripts/verificar-mercadopago.sh
```

---

## B. Webhook (obligatorio en produccion)

- [ ] URL registrada en MP (eventos **Pagos** y, si usas renovacion automatica, **Planes y suscripciones**):

  `https://TU-DOMINIO/api/mercadopago/notificaciones`

- [ ] El endpoint responde **401** sin firma valida (prueba con curl sin headers)
- [ ] Nginx/proxy reenvia `POST` y `GET` a la app (ruta publica en `SecurityConfig`)
- [ ] Tras un pago de prueba, el pago pasa a **CONFIRMADO** en BD sin intervencion manual en `/plataforma`

---

## C. Flujo del tenant (ADMIN)

- [ ] En `/suscripcion`: boton **Pagar con Mercado Pago** redirige a Checkout Pro
- [ ] Retorno exitoso: `/suscripcion/pago-exitoso` activa o sincroniza el plan
- [ ] SPEI/OXXO: `/suscripcion/pago-pendiente` y activacion cuando MP confirme (webhook)
- [ ] Pago fallido: `/suscripcion/pago-fallido` con mensaje claro
- [ ] **Renovacion automatica** (opcional): `/suscripcion/auto-renovar` y cancelacion
- [ ] `/admin/pagos`: lista pendientes y sincronizacion manual al abrir la pagina

---

## D. Operacion de la plataforma (SUPER_ADMIN)

- [ ] Panel `/plataforma`: tarjeta **Mercado Pago** en verde = listo
- [ ] `/plataforma/pagos`: confirmar manual solo si el cliente pago por transferencia fuera de MP
- [ ] Boton **Expirar vencidos** o esperar job programado (`03:30` America/Mexico_City)
- [ ] Plazos: MP pendiente **15 dias** · manual **30 dias** (configurable en `application.properties`)

---

## E. Prueba de punta a punta (sandbox o produccion)

1. Usuario ADMIN con suscripcion por vencer.
2. Contratar 1 mes con Mercado Pago (tarjeta de prueba en sandbox).
3. Verificar en MySQL: `pagos_suscripcion.estado = 'CONFIRMADO'`, `mp_payment_id` lleno.
4. Verificar extension de `suscripciones.fecha_fin`.
5. Repetir notificacion webhook (simulador MP): no debe duplicar meses (idempotencia).
6. Abandonar checkout sin pagar: mensaje de checkout abandonado en `/suscripcion`.

---

## F. Troubleshooting

| Sintoma | Revisar |
|---------|---------|
| No redirige a MP | Token invalido, `APP_BASE_URL`, logs al crear preferencia |
| Pago hecho pero plan no activo | Webhook secret, URL HTTPS, firewall, `docker compose logs app` |
| 401 en webhook | `MERCADOPAGO_WEBHOOK_SECRET` distinto al del panel |
| Dos solicitudes PENDIENTE | Cancelar en `/admin/pagos` antes de nuevo checkout |
| MP en local sin webhook | Usar ngrok + `APP_BASE_URL` o confiar en sincronizacion al volver a `/suscripcion` |

---

## G. Referencia tecnica en el repo

| Componente | Ubicacion |
|------------|-----------|
| Webhook REST | `MercadoPagoWebhookController` |
| Checkout / sync | `MercadoPagoCheckoutService` |
| Confirmacion BD | `SuscripcionServiceImpl.confirmarPagoMercadoPago` |
| Expiracion automatica | `PagoSuscripcionExpiracionService` (`@Scheduled`) |
| Diagnostico | `MercadoPagoProduccionService` + panel `/plataforma` |
