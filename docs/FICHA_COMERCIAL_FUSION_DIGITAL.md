# FUSION DIGITAL — Ficha comercial y elevator pitch

> Sistema de ventas SaaS multi-negocio · Spring Boot · Java 21 · MySQL · México

---

## Elevator pitch (30 segundos)

**FUSION DIGITAL** es un sistema web para que tiendas y negocios de servicios vendan, controlen inventario y emitan tickets desde el navegador. Cada negocio tiene su propia cuenta, puede agregar vendedores y arrancar en minutos con catálogo sugerido según su rubro. Sin instalar programas en cada caja: entras, vendes y ves tus reportes al instante.

---

## Elevator pitch (1 minuto)

Muchos negocios locales siguen anotando ventas en libreta o en hojas de Excel. Eso genera errores de inventario, poca visibilidad de quién vendió qué y mucho trabajo al cerrar el día.

**FUSION DIGITAL** resuelve eso con una plataforma en la nube pensada para **PYMES y emprendedores en México**: abarrotes, ferreterías, boutiques, salones de belleza, consultorías y más.

El dueño se registra, elige su tipo de negocio y en poco tiempo tiene catálogo, inventario, carrito de ventas, tickets, clientes, devoluciones y reportes. Si tiene empleados, les crea usuarios de vendedor con acceso limitado. Todo queda separado por cuenta: tu información no se mezcla con la de otros negocios.

El modelo es **suscripción mensual** con periodo de prueba, planes desde $149 MXN y pago en línea con Mercado Pago. Es simple, accesible y escalable conforme crece el negocio.

---

## ¿A quién va dirigido?

| Perfil | Quién es | Qué obtiene |
|--------|----------|-------------|
| **Administrador del negocio** | Dueño, socio o encargado | Control total: productos, inventario, vendedores, reportes, suscripción, logo y datos fiscales |
| **Vendedor** | Cajero o empleado de mostrador | Ventas diarias, tickets y consulta de inventario sin poder borrar catálogo |
| **Operador de plataforma** | Quien comercializa el SaaS | Panel global de cuentas, pagos y soporte (rol SUPER_ADMIN) |

**Rubros soportados:** abarrotes, ferretería, ropa, tecnología, papelería, farmacia, restaurante, belleza, regalos, servicios profesionales y otros.

---

## Problemas que resuelve

1. **“No sé cuánto tengo en bodega”** → Inventario con alertas de stock bajo y agotado.
2. **“Cada quien anota distinto”** → Un solo catálogo y tickets con folio para todo el equipo.
3. **“Al final del día no cuadra”** → Reportes por vendedor, periodo y exportación CSV.
4. **“Facturar es lento”** → Clientes guardados y datos fiscales que se precargan al cobrar.
5. **“Solo yo puedo usar la computadora”** → Varios vendedores en la misma cuenta, según el plan.
6. **“Somos un negocio de servicios”** → Catálogo de servicios y agenda de citas (belleza, consultoría, etc.).

---

## Propuesta de valor (cliente piloto)

### Por qué probar FUSION DIGITAL

- **Arranque rápido:** registro en línea, onboarding guiado y plantillas de productos por rubro.
- **Bajo costo de entrada:** 1 mes de prueba; planes desde **$149 MXN/mes** (Plan Emprendedor).
- **Sin infraestructura propia:** solo navegador e internet; la plataforma corre en la nube.
- **Crece contigo:** más vendedores y más productos al subir de plan (Negocio $249 · Pro $399).
- **Hecho para México:** precios en MXN, Mercado Pago, datos fiscales y soporte en español.

### Qué incluye hoy

- Ventas con carrito, tickets e impresión de comprobante
- Inventario con entradas, salidas, ajustes e historial
- Categorías, clientes y devoluciones parciales o totales
- Reportes y panel de resumen (ventas hoy, semana, mes)
- Perfil con logo del negocio, email, contraseña y preferencias
- Suscripción y pagos (manual o Mercado Pago)
- Agenda de servicios (rubros compatibles)
- Recuperación de contraseña por correo

### Limitaciones honestas (para la demo)

- La facturación es **informativa/PDF**; el timbrado CFDI ante el SAT es opcional y depende de integración externa (Facturama).
- Es una aplicación **web**, no app móvil nativa (funciona en celular vía navegador).
- Requiere **suscripción activa** para operar ventas e inventario.

---

## Planes (referencia)

| Plan | Precio/mes | Vendedores | Productos | Para quién |
|------|------------|------------|-----------|------------|
| **Emprendedor** | $149 MXN | 2 | 150 | Tienda pequeña que inicia |
| **Negocio** | $249 MXN | 5 | 500 | Más equipo e inventario |
| **Pro** | $399 MXN | 15 | 2,000 | Operación amplia |

*Precios orientativos según configuración del proyecto. Incluyen soporte por la operadora de la plataforma.*

---

## Guión sugerido para demo (10–15 min)

1. **Registro** — Mostrar alta de cuenta, tipo de negocio y aceptación legal.
2. **Onboarding** — Categorías y primer producto (o servicio) del rubro.
3. **Venta** — Catálogo → agregar al carro → cobrar → ver ticket.
4. **Inventario** — Stock antes/después; alerta si queda bajo el umbral.
5. **Reportes** — Ticket del día y resumen en el panel de inicio.
6. **Perfil (ADMIN)** — Logo, datos fiscales, uso del plan.
7. **Cierre** — Planes, prueba gratis y soporte.

**Frase de cierre:**  
*“En una tarde puedes dejar de anotar en libreta y tener control real de ventas e inventario, con tu equipo conectado a la misma información.”*

---

## Mensajes clave por audiencia

### Para el dueño del negocio
> “Tu negocio en un solo lugar: vendes, controlas existencias y ves cuánto vendiste hoy, sin depender de Excel.”

### Para el vendedor
> “Abres el catálogo, cobras y el ticket queda registrado. Tú no te preocupas por configurar nada.”

### Para clase / jurado técnico
> “SaaS multi-tenant con Spring Boot 3, Java 21, JSP, MySQL y Flyway: separación por tenant, roles, suscripciones, pagos con Mercado Pago y módulos de venta, inventario y reportes.”

### Para cliente piloto
> “Prueba un mes gratis, te ayudamos a cargar tu catálogo inicial y si te funciona, eliges el plan que se ajuste a tu tamaño.”

---

## Datos de contacto (completar al presentar)

| Campo | Valor |
|-------|-------|
| Producto | FUSION DIGITAL |
| Sitio / demo | `https://tu-dominio.com` o `http://localhost:8080` |
| Soporte | Configurar en `soporte.email` / WhatsApp en `.env` |
| Documentación operativa | [GUIA_OPERATIVA_NEGOCIO.md](./GUIA_OPERATIVA_NEGOCIO.md) |
| Checklist piloto | [../deploy/CHECKLIST_PILOTO.md](../deploy/CHECKLIST_PILOTO.md) |

---

*Documento generado para presentación comercial y académica del proyecto sistema-ventas.*
