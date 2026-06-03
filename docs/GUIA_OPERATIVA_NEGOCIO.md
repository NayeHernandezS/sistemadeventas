# Guía operativa — Sistema de Ventas

Manual para el **dueño o administrador** del negocio y para **vendedores**. Describe el uso diario de la aplicación web (no la instalación técnica del servidor).

---

## 1. Acceso y roles

### Iniciar sesión

1. Abre la URL que te proporcionó quien instaló el sistema (ejemplo local: `http://localhost:8080/login`).
2. Ingresa tu **usuario** y **contraseña**.

### Roles

| Rol | Qué puede hacer |
|-----|-----------------|
| **Administrador (ADMIN)** | Todo: productos, categorías, clientes, vendedores, inventario, ajustes de stock, facturación, reportes, suscripción y soporte. |
| **Vendedor** | Vender, ver tickets, reportes, consultar inventario y catálogo de clientes. No puede crear productos ni cambiar stock. |

Si la suscripción del negocio venció, el vendedor solo verá un aviso; el administrador debe renovar en **Suscripción**.

---

## 2. Configuración inicial (solo ADMIN)

Haz esto una vez al empezar:

1. **Categorías** — Menú inicio → Categorías → crear las que uses (Bebidas, Abarrotes, etc.).
2. **Productos** — Inventario → Crear producto: nombre, precio, existencias, SKU y categoría.
3. **Vendedores** (opcional) — Mis vendedores → alta de cuentas con rol vendedor.
4. **Mi perfil** — Logo del negocio, umbral de stock bajo y, si facturas CFDI, **datos fiscales del emisor** (RFC, régimen, código postal).
5. **Clientes** (recomendado si facturas) — Clientes → registrar clientes frecuentes con RFC y datos fiscales.

---

## 3. Venta en mostrador

### Pasos

1. Inicio → **Módulo de ventas** (catálogo).
2. Pulsa **Agregar** en cada producto; se acumulan en el carrito.
3. **Ver carrito**:
   - Revisa cantidades y elimina líneas si hace falta.
   - Pulsa **Actualizar** si cambiaste cantidades.
4. **Finalizar venta y generar ticket**.

### Factura (opcional)

En el carrito, sección **Facturación**:

1. Marca **El cliente requiere factura**.
2. Opcional: elige un **Cliente del catálogo** para precargar RFC y datos.
3. Completa RFC, razón social y, si el sistema tiene timbrado CFDI activo, **código postal del receptor** (5 dígitos).
4. Finaliza la venta.

Tras finalizar, el ticket aparece en **Mis tickets**. Si hubo factura, desde ahí puedes ver el comprobante y descargar PDF.

---

## 4. Clientes del catálogo

- **Consulta:** Inicio → Clientes (vendedores y administradores).
- **Alta y edición:** solo ADMIN → Nuevo cliente.
- Campos útiles: nombre, RFC (único por negocio si se captura), razón social, correo, C.P. y uso CFDI (ej. `G03`).
- Al vender, en el carrito selecciona el cliente para no reescribir datos cada vez.

---

## 5. Inventario y stock

### Consulta

**Mi inventario** muestra existencias y alertas (agotado / stock bajo según el umbral configurado en perfil).

### Ajustes de stock (solo ADMIN)

Cuando recibes mercancía, hay merma o haces conteo físico:

1. Inventario → botón **Ajustar** en el producto.
2. Tipo de movimiento:
   - **Entrada:** suma unidades (ej. llegaron 20 piezas).
   - **Salida:** resta unidades (merma, uso interno); no permite dejar stock negativo.
   - **Ajuste:** fija la cantidad exacta en inventario (ej. conteo = 15).
3. Motivo opcional (recomendado: "Compra proveedor", "Conteo marzo").
4. **Historial de movimientos** — revisa los últimos 50 cambios con usuario y antes/después.

Las ventas y devoluciones también modifican el stock automáticamente.

---

## 6. Devoluciones

1. Inicio → **Devoluciones**.
2. Busca el ticket por **folio** o id.
3. Indica cantidades a devolver (parcial o total).
4. Confirma; el stock de los productos devueltos se reintegra.

---

## 7. Reportes

1. Inicio → **Reportes**.
2. Filtra por fechas y, si eres ADMIN, por vendedor.
3. Revisa totales (las devoluciones se reflejan en los netos).
4. **Exportar CSV** para Excel o contabilidad simple.

---

## 8. Facturación y CFDI (México)

### Comprobante informativo (sin timbrado SAT)

Si el servidor no tiene Facturama configurado, el sistema guarda datos fiscales y genera un **PDF informativo** (no es CFDI válido ante el SAT).

### CFDI timbrado (con Facturama)

Requisitos (los configura quien administra el servidor + tú en perfil):

- Credenciales Facturama en el servidor.
- **CSD** del RFC emisor cargado en el panel de Facturama (sandbox o producción).
- En **Mi perfil** → datos fiscales del negocio completos.
- Al vender: C.P. del receptor y datos del cliente correctos.

### Si el timbrado falló (solo ADMIN)

1. Tickets → abrir ticket → ver **Factura**.
2. Lee el mensaje de error (ej. falta C.P., RFC inválido).
3. Corrige datos en el formulario **Reintentar timbrado CFDI**.
4. Pulsa **Reintentar timbrado**.
5. Si fue exitoso: descarga **CFDI (PDF)** y **XML**.

---

## 9. Vendedores y límites del plan

Cada plan limita cantidad de **vendedores** y **productos** (se muestra en el inicio del ADMIN).

- Crear vendedor: **Mis vendedores** → formulario.
- El vendedor solo ve su actividad en **Mi perfil** (tickets recientes, resumen del mes).

Si alcanzas el límite, el sistema pedirá mejorar el plan en **Suscripción**.

---

## 10. Suscripción y soporte (ADMIN)

- **Suscripción** — estado del plan, renovación (según cómo esté configurado el cobro en tu instalación).
- **Soporte** — envía solicitudes; verás correo/WhatsApp/horario de contacto configurados por la plataforma.
- **Estado de mis pagos** — historial de pagos de suscripción del negocio.

---

## 11. Resumen rápido por tarea diaria

| Tarea | Dónde |
|-------|--------|
| Vender | Módulo de ventas → Carrito → Finalizar |
| Ver ventas del día | Mis tickets / Reportes |
| Agregar producto | Inventario → Crear producto |
| Recibir mercancía | Inventario → Ajustar → Entrada |
| Corregir conteo | Inventario → Ajustar → Ajuste |
| Cliente nuevo | Clientes → Nuevo |
| Devolver mercancía | Devoluciones |
| Factura con error CFDI | Ticket → Factura → Reintentar timbrado |
| Cambiar logo / stock mínimo | Mi perfil |

---

## 12. Problemas frecuentes

| Situación | Qué hacer |
|-----------|-----------|
| No puedo vender | ADMIN: revisa **Suscripción** activa. |
| Stock no cuadra | Revisa **Historial de movimientos** y ventas/devoluciones del día. |
| No deja guardar cliente con RFC | Ese RFC ya existe en tu cuenta; edita el cliente existente. |
| CFDI error C.P. | Completa código postal del receptor (5 dígitos) y reintenta. |
| CFDI error emisor | Completa datos fiscales en **Mi perfil** y CSD en Facturama. |
| Vendedor no edita productos | Es normal; solo el ADMIN gestiona catálogo e inventario. |

---

## 13. Contacto

Para fallos del sistema, renovación o capacitación, usa **Soporte** dentro de la app o el canal que te dio quien te vendió/instaló el servicio.

---

*Documento para entregar al cliente del negocio. Puedes exportarlo a PDF desde tu editor o navegador. Versión alineada con catálogo de clientes, ajustes de inventario y reintento de timbrado CFDI.*
