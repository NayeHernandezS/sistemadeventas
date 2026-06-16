<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Panel — FUSION DIGITAL</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estiloindex.css">
</head>
<body class="app-con-nav-movil">

<%@ include file="fragmentos/nav-tenant.jspf" %>

<main class="container my-5">
    <div class="text-center mb-5">
        <div class="logo-slot logo-slot--hero mx-auto mb-3">
            <c:set var="logoCssClass" value="logo-tenant-img--hero" scope="request"/>
            <%@ include file="fragmentos/logo-tenant.jspf" %>
        </div>
        <h2 class="hero-title mb-2">Panel de ventas</h2>
        <p class="text-muted">Gestiona tu catalogo, ventas y reportes desde tu cuenta.</p>
        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success mt-3 mx-auto text-start" style="max-width: 520px;">
                ${mensajeExito}
            </div>
        </c:if>
        <c:if test="${not empty sessionScope.mensajeError}">
            <div class="alert alert-warning mt-3 mx-auto text-start" style="max-width: 520px;">
                ${sessionScope.mensajeError}
            </div>
            <c:remove var="mensajeError" scope="session"/>
        </c:if>
        <c:if test="${param.sinPlan eq '1' && sessionScope.rol ne 'ADMIN'}">
            <div class="alert alert-warning mt-3 mx-auto" style="max-width: 520px;">
                La suscripcion del negocio no esta activa. Pide al administrador de tu cuenta que renueve el plan.
            </div>
        </c:if>
        <c:if test="${sessionScope.rol eq 'ADMIN'}">
        <c:if test="${not empty avisoSuscripcion}">
            <div class="alert alert-${avisoSuscripcion.nivel} mt-3 mx-auto text-start" style="max-width: 520px;">
                <i class="bi bi-exclamation-triangle"></i> ${avisoSuscripcion.mensaje}
                <a href="${pageContext.request.contextPath}/suscripcion" class="alert-link ms-1">Renovar ahora</a>
            </div>
        </c:if>
        <div class="alert alert-info mt-3 mx-auto text-start" style="max-width: 520px;">
            <strong>Plan:</strong> ${planNombre}<br>
            <span class="small">Vendedores: ${vendedoresUsados} / ${vendedoresMax} ·
            Productos: ${productosUsados} / ${productosMax}</span>
        </div>
        <a href="${pageContext.request.contextPath}/suscripcion" class="btn btn-outline-primary btn-sm mt-2">
            Ver planes y suscripcion
        </a>
        <c:if test="${cantidadConAlerta > 0}">
        <div class="alert alert-warning mt-3 mx-auto text-start" style="max-width: 520px;">
            <i class="bi bi-exclamation-triangle"></i>
            <strong>Inventario:</strong>
            ${cantidadConAlerta} producto(s) requieren atencion
            (<c:if test="${cantidadAgotados > 0}">${cantidadAgotados} agotado(s)</c:if><c:if test="${cantidadAgotados > 0 && cantidadStockBajo > 0}">, </c:if><c:if test="${cantidadStockBajo > 0}">${cantidadStockBajo} stock bajo</c:if>).
            Umbral: ${stockMinimo} uds.
            <a href="${pageContext.request.contextPath}/inventario/comprar-hoy" class="alert-link">Ver lista comprar hoy</a>
        </div>
        <c:if test="${not empty listaCompraPreview && listaCompraPreview.totalProductos > 0}">
        <div class="card border-0 shadow-sm mt-3 mx-auto text-start" style="max-width: 520px;">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <h6 class="mb-0"><i class="bi bi-cart-check"></i> Comprar hoy</h6>
                    <a href="${pageContext.request.contextPath}/inventario/comprar-hoy" class="small">Ver todos (${listaCompraPreview.totalProductos})</a>
                </div>
                <ul class="list-group list-group-flush">
                    <c:forEach items="${listaCompraPreview.productos}" var="p">
                        <li class="list-group-item px-0 d-flex justify-content-between align-items-center">
                            <span>
                                <c:if test="${p.agotado}"><span class="badge bg-danger me-1">Agotado</span></c:if>
                                ${p.nombre}
                            </span>
                            <span class="text-muted small">+${p.cantidadSugerida} uds.</span>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
        </c:if>
        </c:if>
        </c:if>
    </div>

    <c:if test="${sessionScope.rol eq 'ADMIN' && mostrarChecklistActivacion}">
    <div class="row justify-content-center mb-4">
        <div class="col-lg-10">
            <div class="card border-0 shadow-sm border-start border-primary border-4">
                <div class="card-body text-start">
                    <h5 class="mb-3"><i class="bi bi-list-check"></i> Activa tu negocio</h5>
                    <ul class="list-unstyled mb-3">
                        <li class="mb-2">
                            <i class="bi bi-check-circle-fill text-success"></i>
                            Cuenta creada
                        </li>
                        <li class="mb-2">
                            <c:choose>
                                <c:when test="${activacionNegocio.catalogoListo}">
                                    <i class="bi bi-check-circle-fill text-success"></i>
                                    Catalogo listo (${activacionNegocio.totalProductos} articulos)
                                </c:when>
                                <c:otherwise>
                                    <i class="bi bi-circle text-muted"></i>
                                    Agrega productos al catalogo
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <li class="mb-0">
                            <c:choose>
                                <c:when test="${activacionNegocio.primeraVentaRegistrada}">
                                    <i class="bi bi-check-circle-fill text-success"></i>
                                    Primera venta registrada
                                </c:when>
                                <c:otherwise>
                                    <i class="bi bi-circle text-primary"></i>
                                    <strong>Pendiente:</strong> registra tu primera venta
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </ul>
                    <c:if test="${not activacionNegocio.primeraVentaRegistrada}">
                    <a href="${pageContext.request.contextPath}/productos/caja" class="btn btn-primary btn-sm">
                        <i class="bi bi-cart-plus"></i> Ir a cobrar
                    </a>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
    </c:if>

    <c:if test="${sessionScope.rol eq 'ADMIN' && not empty panelNegocio}">
    <div class="row g-3 mb-4 justify-content-center">
        <div class="col-lg-10">
            <h5 class="text-start mb-3"><i class="bi bi-speedometer2"></i> Resumen del negocio</h5>
            <div class="row g-3">
                <div class="col-md-4">
                    <div class="card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <p class="text-muted small mb-1">Ventas hoy</p>
                            <p class="h4 mb-0">${panelNegocio.ticketsHoy} <span class="fs-6 text-muted">tickets</span></p>
                            <p class="mb-0 text-success fw-semibold">$${panelNegocio.netoHoy} neto</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <p class="text-muted small mb-1">Esta semana</p>
                            <p class="h4 mb-0">${panelNegocio.ticketsSemana} <span class="fs-6 text-muted">tickets</span></p>
                            <p class="mb-0 text-success fw-semibold">$${panelNegocio.netoSemana} neto</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card h-100 border-0 shadow-sm">
                        <div class="card-body">
                            <p class="text-muted small mb-1">Este mes</p>
                            <p class="h4 mb-0">${panelNegocio.ticketsMes} <span class="fs-6 text-muted">tickets</span></p>
                            <p class="mb-0 text-success fw-semibold">$${panelNegocio.netoMes} neto</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="card border-0 shadow-sm mt-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <p class="text-muted small mb-0">Top productos (ultimos 7 dias)</p>
                        <a href="${pageContext.request.contextPath}/reportes" class="small">Ver reportes</a>
                    </div>
                    <c:choose>
                        <c:when test="${empty panelNegocio.topProductosSemana}">
                            <p class="text-muted small mb-0">Sin ventas en el periodo.</p>
                        </c:when>
                        <c:otherwise>
                            <table class="table table-sm table-striped mb-0">
                                <thead>
                                <tr>
                                    <th>Producto</th>
                                    <th class="text-end">Unidades</th>
                                    <th class="text-end">Importe</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${panelNegocio.topProductosSemana}" var="p">
                                    <tr>
                                        <td>${p.nombreProducto}</td>
                                        <td class="text-end">${p.unidadesVendidas}</td>
                                        <td class="text-end">$${p.importeTotal}</td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
    </c:if>

    <div class="row g-4 justify-content-center text-center">

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-primary border-3">
                <div class="icon-circle"><i class="bi bi-phone"></i></div>
                <a href="${pageContext.request.contextPath}/productos/caja" class="menu-link stretched-link">Caja rapida</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-cart-plus"></i></div>
                <a href="${pageContext.request.contextPath}/productos" class="menu-link stretched-link">Modulo de ventas</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-warning border-3">
                <div class="icon-circle"><i class="bi bi-cart-check"></i></div>
                <a href="${pageContext.request.contextPath}/inventario/comprar-hoy" class="menu-link stretched-link">Comprar hoy</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-box-seam"></i></div>
                <a href="${pageContext.request.contextPath}/crudprod" class="menu-link stretched-link">Mi inventario</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-ticket-detailed"></i></div>
                <a href="${pageContext.request.contextPath}/tickets" class="menu-link stretched-link">Mis tickets</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-arrow-return-left"></i></div>
                <a href="${pageContext.request.contextPath}/devoluciones" class="menu-link stretched-link">Devoluciones</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-calendar2-check"></i></div>
                <a href="${pageContext.request.contextPath}/reportes" class="menu-link stretched-link">Reportes</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-success border-3">
                <div class="icon-circle"><i class="bi bi-cash-coin"></i></div>
                <a href="${pageContext.request.contextPath}/reportes/cierre" class="menu-link stretched-link">Cierre del día</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-person-vcard"></i></div>
                <a href="${pageContext.request.contextPath}/clientes" class="menu-link stretched-link">Clientes</a>
            </div>
        </div>

        <c:if test="${mostrarAgendaServicios}">
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-info border-3">
                <div class="icon-circle"><i class="bi bi-calendar-event"></i></div>
                <a href="${pageContext.request.contextPath}/agenda" class="menu-link stretched-link">Agenda de servicios</a>
            </div>
        </div>
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-info border-3">
                <div class="icon-circle"><i class="bi bi-scissors"></i></div>
                <a href="${pageContext.request.contextPath}/productos/servicios" class="menu-link stretched-link">Catalogo de servicios</a>
            </div>
        </div>
        </c:if>

        <c:if test="${mostrarRecetasRestaurante}">
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-danger border-3">
                <div class="icon-circle"><i class="bi bi-journal-richtext"></i></div>
                <a href="${pageContext.request.contextPath}/recetas" class="menu-link stretched-link">Recetas y costos</a>
            </div>
        </div>
        </c:if>

        <c:if test="${sessionScope.rol eq 'ADMIN'}">
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-success border-3">
                <div class="icon-circle"><i class="bi bi-tags"></i></div>
                <a href="${pageContext.request.contextPath}/categorias" class="menu-link stretched-link">Categorias</a>
            </div>
        </div>
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-warning border-3">
                <div class="icon-circle"><i class="bi bi-people"></i></div>
                <a href="${pageContext.request.contextPath}/usuarios" class="menu-link stretched-link">Mis vendedores</a>
            </div>
        </div>
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-primary border-3">
                <div class="icon-circle"><i class="bi bi-credit-card"></i></div>
                <a href="${pageContext.request.contextPath}/admin/pagos" class="menu-link stretched-link">Estado de mis pagos</a>
            </div>
        </div>
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-info border-3">
                <div class="icon-circle"><i class="bi bi-headset"></i></div>
                <a href="${pageContext.request.contextPath}/soporte" class="menu-link stretched-link">Soporte</a>
            </div>
        </div>
        </c:if>

    </div>
</main>

<footer>
    <div class="container py-4 text-center">
        <p class="small mb-0 text-secondary">&copy; 2026 FUSION DIGITAL. Cada cuenta gestiona su propio negocio.</p>
    </div>
</footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
