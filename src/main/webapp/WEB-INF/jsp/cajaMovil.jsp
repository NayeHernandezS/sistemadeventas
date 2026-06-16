<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Caja rapida</title>
</head>
<body class="app-con-nav-movil caja-movil-page">
<%@ include file="fragmentos/nav-tenant.jspf" %>

<div id="panel-carro-catalogo" class="visually-hidden" aria-hidden="true"
     data-context-path="${pageContext.request.contextPath}">
    <span data-carro-total>${carro.total}</span>
    <span data-carro-cantidad>${carro.items.size()}</span>
</div>

<div class="container-fluid caja-movil-wrap px-3 py-3">
    <div class="d-flex align-items-center justify-content-between gap-2 mb-3">
        <div>
            <h1 class="h4 mb-0">Caja rapida</h1>
            <p class="text-muted small mb-0">Toca o escanea para agregar</p>
        </div>
        <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/productos">
            Catalogo completo
        </a>
    </div>

    <c:if test="${not empty sessionScope.mensajeError}">
        <div class="alert alert-danger py-2 small">${sessionScope.mensajeError}</div>
        <c:remove var="mensajeError" scope="session"/>
    </c:if>

    <div id="catalogo-carro-feedback" class="alert d-none py-2 small mb-2" role="alert"></div>

    <c:if test="${logueado}">
        <div class="caja-movil-escaneo mb-3">
            <c:set var="escaneoOrigen" value="caja"/>
            <%@ include file="fragmentos/escaneo-codigo.jspf" %>
        </div>
    </c:if>

    <div class="mb-3">
        <label class="visually-hidden" for="caja-buscar">Buscar producto</label>
        <input type="search" id="caja-buscar" class="form-control form-control-lg"
               placeholder="Buscar nombre o codigo..." autocomplete="off" enterkeyhint="search">
    </div>

    <div id="caja-productos" class="caja-productos-grid">
        <c:forEach items="${productos}" var="p">
            <c:set var="disponible" value="${p.esServicio || p.existencias > 0}"/>
            <article class="caja-producto-card${disponible ? '' : ' caja-producto-card--agotado'}"
                     data-caja-producto
                     data-buscar="${fn:toLowerCase(p.nombre)} ${fn:toLowerCase(p.sku)} ${p.id}">
                <div class="caja-producto-card__body">
                    <p class="caja-producto-card__nombre mb-1">${p.nombre}</p>
                    <p class="caja-producto-card__meta text-muted small mb-2">
                        <c:choose>
                            <c:when test="${p.esServicio}">Servicio</c:when>
                            <c:when test="${p.existencias == 0}">Agotado</c:when>
                            <c:otherwise>${p.existencias} en stock</c:otherwise>
                        </c:choose>
                    </p>
                    <div class="d-flex align-items-center justify-content-between gap-2">
                        <span class="caja-producto-card__precio">$${p.precio}</span>
                        <c:choose>
                            <c:when test="${disponible && logueado}">
                                <button type="button" class="btn btn-primary btn-lg caja-btn-agregar btn-agregar-carro"
                                        data-producto-id="${p.id}"
                                        data-producto-nombre="${fn:escapeXml(p.nombre)}"
                                        data-producto-precio="${p.precio}"
                                        data-origen="caja"
                                        aria-label="Agregar ${fn:escapeXml(p.nombre)}">
                                    <i class="bi bi-plus-lg"></i>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary">No disponible</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </article>
        </c:forEach>
    </div>

    <p id="caja-sin-resultados" class="text-muted text-center small py-4 d-none">
        No hay productos que coincidan con la busqueda.
    </p>
</div>

<div id="caja-barra-cobro" class="caja-barra-cobro d-lg-none no-print">
    <div class="d-flex align-items-center justify-content-between gap-2 mb-2 px-1">
        <div>
            <span class="small text-muted d-block">Carro</span>
            <span class="fw-semibold"><span data-caja-cantidad>${carro.items.size()}</span> articulos</span>
        </div>
        <div class="text-end">
            <span class="small text-muted d-block">Total</span>
            <span class="fw-bold fs-5 text-primary mb-0">$<span data-caja-total>${carro.total}</span></span>
        </div>
    </div>
    <a class="btn btn-success btn-lg w-100 fw-semibold${empty carro.items ? ' disabled' : ''}"
       data-caja-cobrar
       href="${pageContext.request.contextPath}/carro/ver"
       aria-disabled="${empty carro.items ? 'true' : 'false'}">
        <i class="bi bi-cash-coin"></i> Ir a cobrar
    </a>
</div>

<%@ include file="fragmentos/foot-app.jspf" %>
<script src="${pageContext.request.contextPath}/js/catalogo-carro.js"></script>
<script src="${pageContext.request.contextPath}/js/caja-movil.js"></script>
<script src="${pageContext.request.contextPath}/js/escaneo-producto.js"></script>
</body>
</html>
