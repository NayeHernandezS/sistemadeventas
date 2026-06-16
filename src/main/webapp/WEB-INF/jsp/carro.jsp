<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Carro de Compras</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <style>
        @media (max-width: 991.98px) {
            body.carro-pagina { padding-bottom: 5.5rem; }
            .carro-barra-movil {
                position: fixed;
                bottom: 0;
                left: 0;
                right: 0;
                z-index: 1030;
                background: #fff;
                border-top: 2px solid rgba(91, 42, 134, 0.2);
                box-shadow: 0 -4px 16px rgba(10, 10, 10, 0.12);
                padding: 0.65rem 0.75rem;
            }
        }
        @media (min-width: 992px) {
            .carro-barra-movil { display: none !important; }
        }
        .carro-resumen-total {
            font-size: 1.75rem;
            font-weight: 700;
            color: var(--color-morado, #5B2A86);
        }
        .carro-panel-factura-btn {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 0.75rem;
            width: 100%;
            border: none;
            background: transparent;
            padding: 0.85rem 1rem;
            text-align: left;
            color: inherit;
        }
        .carro-panel-factura-btn:hover,
        .carro-panel-factura-btn:focus {
            background: rgba(91, 42, 134, 0.06);
        }
        .carro-panel-factura-flecha {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 2rem;
            height: 2rem;
            border-radius: 50%;
            background: rgba(91, 42, 134, 0.1);
            color: var(--color-morado, #5B2A86);
            transition: transform 0.25s ease;
        }
        .carro-panel-factura-btn--abierto .carro-panel-factura-flecha {
            transform: rotate(180deg);
        }
    </style>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
</head>
<body class="carro-pagina">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 class="h3 mb-0">Carro de compras</h1>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/productos">
            <i class="bi bi-arrow-left"></i> Seguir vendiendo
        </a>
    </div>

    <c:if test="${not empty sessionScope.mensajeError}">
        <div class="alert alert-danger">${sessionScope.mensajeError}</div>
        <c:remove var="mensajeError" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.mensajeExito}">
        <div class="alert alert-success">${sessionScope.mensajeExito}</div>
        <c:remove var="mensajeExito" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.mensajeTicket}">
        <div class="alert alert-info">${sessionScope.mensajeTicket}</div>
        <c:remove var="mensajeTicket" scope="session"/>
    </c:if>

    <c:set var="escaneoOrigen" value="carro"/>
    <%@ include file="fragmentos/escaneo-codigo.jspf" %>

    <c:choose>
    <c:when test="${empty carro.items}">
        <div class="alert alert-warning">No hay productos en el carro.</div>
        <a class="btn btn-success btn-lg" href="${pageContext.request.contextPath}/productos">
            <i class="bi bi-cart-plus"></i> Ir al catalogo de ventas
        </a>
    </c:when>
    <c:otherwise>

    <div class="row g-4">
        <div class="col-lg-8">
            <form id="formCarro" name="formcarro" action="${pageContext.request.contextPath}/carro/actualizar" method="post">
                <%@ include file="csrf.jspf" %>
                <c:set var="buscadorTablaId" value="tablaProductosCarro"/>
                <c:set var="buscadorPlaceholder" value="Buscar producto en el carro..."/>
                <%@ include file="fragmentos/buscador-tabla.jspf" %>
                <div class="table-responsive">
                    <table id="tablaProductosCarro" class="table table-hover table-striped mb-0">
                        <thead>
                        <tr>
                            <th>Producto</th>
                            <th class="text-end">Precio</th>
                            <th class="text-center">Stock</th>
                            <th class="text-center" style="min-width: 5rem;">Cant.</th>
                            <th class="text-end">Importe</th>
                            <th class="text-center">Quitar</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${carro.items}" var="item">
                        <tr data-fila-busqueda="1"
                            data-buscar="${item.producto.nombre} ${item.producto.id} ${item.producto.sku}">
                            <td>
                                <span class="fw-semibold">${item.producto.nombre}</span>
                                <span class="text-muted small d-block">#${item.producto.id}</span>
                            </td>
                            <td class="text-end">$${item.producto.precio}</td>
                            <td class="text-center">${item.producto.existencias}</td>
                            <td class="text-center">
                                <input type="number" min="1" class="form-control form-control-sm text-center mx-auto"
                                       style="max-width: 4.5rem;" name="cant_${item.producto.id}" value="${item.cantidad}">
                            </td>
                            <td class="text-end">$${item.importe}</td>
                            <td class="text-center">
                                <input type="checkbox" class="form-check-input" value="${item.producto.id}" name="deleteProductos"
                                       aria-label="Quitar ${item.producto.nombre}">
                            </td>
                        </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <button type="submit" class="btn btn-outline-primary btn-sm mt-2">
                    <i class="bi bi-arrow-repeat"></i> Actualizar cantidades
                </button>
            </form>

            <form id="formFinalizar" class="mt-4" action="${pageContext.request.contextPath}/carro/finalizar" method="post">
                <%@ include file="csrf.jspf" %>
                <c:if test="${not empty clienteIdSeleccionado}">
                    <input type="hidden" name="clienteId" value="${clienteIdSeleccionado}">
                </c:if>
                <div class="mb-3">
                    <label class="form-label" for="nombreCliente">Nombre del cliente <span class="text-muted">(opcional)</span></label>
                    <input class="form-control" type="text" name="nombreCliente" id="nombreCliente" maxlength="200"
                           placeholder="Ej. Juan Perez" value="${nombreClientePrefill}">
                    <div class="form-text">Aparecera en el ticket. No es necesario si no lo conoces.</div>
                </div>
                <div class="card border-secondary-subtle">
                    <div class="card-header p-0 bg-light">
                        <button id="btnPanelFactura" type="button"
                                class="carro-panel-factura-btn ${not empty clienteIdSeleccionado ? 'carro-panel-factura-btn--abierto' : ''}"
                                data-bs-toggle="collapse" data-bs-target="#panelFactura"
                                aria-expanded="${not empty clienteIdSeleccionado ? 'true' : 'false'}"
                                aria-controls="panelFactura">
                            <span class="d-flex align-items-start gap-2">
                                <i class="bi bi-receipt-cutoff fs-5 text-primary mt-1" aria-hidden="true"></i>
                                <span>
                                    <span class="d-block fw-semibold">Panel de facturacion</span>
                                    <span class="d-block small text-muted">
                                        Opcional · pulsa aqui para desplegar cliente, RFC y CFDI
                                    </span>
                                </span>
                            </span>
                            <span class="carro-panel-factura-flecha" title="Desplegar u ocultar">
                                <i class="bi bi-chevron-down fs-5" aria-hidden="true"></i>
                            </span>
                        </button>
                    </div>
                    <div id="panelFactura" class="collapse ${not empty clienteIdSeleccionado ? 'show' : ''}">
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label" for="clienteCatalogo">Cliente del catalogo</label>
                                <select class="form-select" id="clienteCatalogo" name="clienteCatalogo"
                                        onchange="var v=this.value;var b='${pageContext.request.contextPath}/carro/ver';window.location.href=v?b+'?clienteId='+encodeURIComponent(v):b;">
                                    <option value="">— Sin cliente / usar datos de Mi perfil —</option>
                                    <c:forEach items="${clientes}" var="cl">
                                        <option value="${cl.id}"
                                                <c:if test="${clienteIdSeleccionado eq cl.id}">selected</c:if>>
                                            ${cl.nombre}<c:if test="${not empty cl.rfc}"> (${cl.rfc})</c:if>
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="form-text">
                                    Al elegir un cliente se precargan sus datos fiscales.
                                    <a href="${pageContext.request.contextPath}/clientes">Ver catalogo</a>
                                    <c:if test="${esAdmin}"> ·
                                        <a href="${pageContext.request.contextPath}/clientes/form">Nuevo cliente</a>
                                    </c:if>
                                </div>
                            </div>
                            <div class="form-check mb-3">
                                <input class="form-check-input" type="checkbox" name="requiereFactura" value="1" id="requiereFactura"
                                       <c:if test="${not empty clienteIdSeleccionado}">checked</c:if>>
                                <label class="form-check-label" for="requiereFactura">El cliente requiere factura</label>
                            </div>
                            <c:if test="${cfdiTimbradoDisponible}">
                                <div class="alert alert-info py-2 small mb-3">
                                    Timbrado CFDI activo: complete datos fiscales en
                                    <a href="${pageContext.request.contextPath}/perfil">Mi perfil</a>.
                                </div>
                            </c:if>
                            <c:if test="${prefillOrigen eq 'cliente'}">
                                <p class="small text-info">Datos precargados del cliente seleccionado.</p>
                            </c:if>
                            <c:if test="${prefillOrigen eq 'perfil'}">
                                <p class="small text-info">Datos precargados desde Mi perfil.</p>
                            </c:if>
                            <div class="row g-2">
                                <div class="col-md-4">
                                    <label class="form-label" for="rfcFactura">RFC</label>
                                    <input class="form-control" type="text" name="rfcFactura" id="rfcFactura" maxlength="13"
                                           value="${facturaDefaults.rfc}">
                                </div>
                                <div class="col-md-8">
                                    <label class="form-label" for="razonSocial">Razon social</label>
                                    <input class="form-control" type="text" name="razonSocial" id="razonSocial"
                                           value="${facturaDefaults.razonSocial}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="emailFactura">Correo</label>
                                    <input class="form-control" type="email" name="emailFactura" id="emailFactura"
                                           value="${facturaDefaults.email}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label" for="usoCfdi">Uso CFDI</label>
                                    <input class="form-control" type="text" name="usoCfdi" id="usoCfdi" maxlength="10"
                                           value="${facturaDefaults.usoCfdi}">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label" for="codigoPostalReceptor">C.P. receptor</label>
                                    <input class="form-control" type="text" name="codigoPostalReceptor" id="codigoPostalReceptor"
                                           maxlength="5" value="${facturaDefaults.codigoPostal}">
                                </div>
                                <div class="col-12">
                                    <label class="form-label" for="direccionFactura">Direccion</label>
                                    <input class="form-control" type="text" name="direccionFactura" id="direccionFactura"
                                           value="${facturaDefaults.direccion}">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>

        <div class="col-lg-4">
            <div class="card shadow-sm border-primary sticky-top" style="top: 1rem;">
                <div class="card-body">
                    <p class="text-muted small mb-1">Total del carro</p>
                    <p class="carro-resumen-total mb-3">$${carro.total}</p>
                    <p class="small text-muted mb-3">
                        ${fn:length(carro.items)} producto(s) · puedes cobrar sin abrir facturacion
                    </p>
                    <div class="d-grid gap-2">
                        <button type="submit" form="formFinalizar" class="btn btn-success btn-lg">
                            <i class="bi bi-check-circle-fill"></i> Finalizar venta
                        </button>
                        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/productos">
                            <i class="bi bi-plus-circle"></i> Seguir vendiendo
                        </a>
                        <button type="submit" form="formCarro" class="btn btn-outline-secondary btn-sm">
                            Actualizar cantidades
                        </button>
                    </div>
                    <hr class="my-3">
                    <div class="d-flex flex-wrap gap-2">
                        <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/inicio">
                            <i class="bi bi-house"></i> Inicio
                        </a>
                        <a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/tickets">
                            Ver tickets
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="carro-barra-movil d-lg-none">
        <div class="d-flex align-items-center justify-content-between gap-2 mb-2">
            <span class="small text-muted">Total</span>
            <span class="fw-bold fs-5 text-primary mb-0">$${carro.total}</span>
        </div>
        <div class="d-grid gap-2" style="grid-template-columns: 1fr 1fr;">
            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/productos">
                Seguir vendiendo
            </a>
            <button type="submit" form="formFinalizar" class="btn btn-success fw-semibold">
                Finalizar venta
            </button>
        </div>
    </div>

    </c:otherwise>
    </c:choose>
</div>
<script>
    (function () {
        var panel = document.getElementById('panelFactura');
        var btn = document.getElementById('btnPanelFactura');
        if (!panel || !btn) {
            return;
        }
        function sincronizarFlecha(abierto) {
            btn.classList.toggle('carro-panel-factura-btn--abierto', abierto);
            btn.setAttribute('aria-expanded', abierto ? 'true' : 'false');
        }
        panel.addEventListener('show.bs.collapse', function () { sincronizarFlecha(true); });
        panel.addEventListener('hide.bs.collapse', function () { sincronizarFlecha(false); });
    })();
</script>
<script src="${pageContext.request.contextPath}/js/buscador-tabla.js"></script>
<script src="${pageContext.request.contextPath}/js/escaneo-producto.js"></script>
<c:set var="ocultarNavMovil" value="true"/>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
