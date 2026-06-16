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
    <title>Listado de productos</title>
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container catalogo-ventas-page">
<div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
    <h1 class="mb-0">Listado de productos</h1>
    <div class="d-flex flex-wrap gap-2">
        <a class="btn btn-sm btn-secondary" href="${pageContext.request.contextPath}/inicio">Volver</a>
        <a class="btn btn-sm btn-primary d-lg-none" href="${pageContext.request.contextPath}/productos/caja">
            <i class="bi bi-phone"></i> Caja rapida
        </a>
        <a class="btn btn-sm btn-outline-success" href="${pageContext.request.contextPath}/carro/ver">Ver carro completo</a>
    </div>
</div>

<c:if test="${not empty sessionScope.mensajeError}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        ${sessionScope.mensajeError}
        <button type="button" class="btn-close" aria-label="Cerrar"></button>
    </div>
    <c:remove var="mensajeError" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.mensajeExito}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        ${sessionScope.mensajeExito}
        <button type="button" class="btn-close" aria-label="Cerrar"></button>
    </div>
    <c:remove var="mensajeExito" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.username}">
    <div id="aviso-bienvenida" class="alert alert-info alert-dismissible fade show d-none" role="alert" data-username="${sessionScope.username}">
        Hola ${sessionScope.username}, bienvenido!
        <button type="button" class="btn-close" aria-label="Cerrar"></button>
    </div>
</c:if>

<div class="row catalogo-ventas-layout g-4">
    <div class="col-lg-8" id="catalogo">
        <h2 class="h5 mb-3">Catalogo</h2>
        <div id="catalogo-carro-feedback" class="alert d-none py-2 small mb-3" role="alert"></div>

        <c:if test="${cantidadConAlerta > 0}">
            <div class="alert alert-warning py-2 small mb-3">
                ${cantidadConAlerta} producto(s) con stock bajo o agotado (alerta desde ${stockMinimo} unidades).
            </div>
        </c:if>

        <c:set var="buscadorTablaId" value="tablaProductosCatalogo"/>
        <c:set var="buscadorPlaceholder" value="Buscar por nombre, SKU, codigo o ID..."/>
        <%@ include file="fragmentos/buscador-tabla.jspf" %>

        <div class="mb-3 d-flex flex-wrap align-items-center gap-2">
            <c:set var="filtrosTablaId" value="tablaProductosCatalogo"/>
            <c:set var="filtrosModo" value="catalogo"/>
            <c:set var="filtrosEsAdmin" value="${esAdmin}"/>
            <c:set var="filtrosLogueado" value="${logueado}"/>
            <%@ include file="fragmentos/filtros-columnas-tabla.jspf" %>
            <small class="text-muted">Oculta columnas para ver solo los datos que necesitas.</small>
        </div>

        <c:if test="${logueado}">
            <c:set var="escaneoOrigen" value="productos"/>
            <%@ include file="fragmentos/escaneo-codigo.jspf" %>
        </c:if>

        <div class="table-responsive catalogo-tabla-wrap">
        <table id="tablaProductosCatalogo" class="table table-hover table-striped">
            <thead>
            <tr>
                <th data-col="id">Id</th>
                <th data-col="nombre">Nombre</th>
                <th data-col="tipo">Tipo</th>
                <th data-col="categoria">Categoria</th>
                <c:if test="${logueado}">
                <th data-col="precio_venta">Precio</th>
                <th data-col="existencias">Existencias</th>
                <c:if test="${esAdmin}">
                    <th data-col="precio_compra">Precio compra</th>
                    <th data-col="porcentaje_ganancia" title="Porcentaje configurado al crear o editar">% Ganancia</th>
                    <th data-col="margen" title="Utilidad en pesos y porcentaje sobre el costo">Margen</th>
                </c:if>
                <th data-col="agregar">Agregar</th>
                </c:if>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${productos}" var="p">
            <tr data-fila-busqueda="1"
                data-buscar="${p.nombre} ${p.sku} ${p.categoria.nombre} ${p.id} ${p.tipoItem.etiqueta}"
                class="${p.esServicio ? '' : (p.existencias == 0 ? 'table-danger' : (p.existencias <= stockMinimo ? 'table-warning' : ''))}">
                <td data-col="id">${p.id}</td>
                <td data-col="nombre">${p.nombre}</td>
                <td data-col="tipo">
                    <c:choose>
                        <c:when test="${p.esServicio}">
                            <span class="badge bg-info text-dark">Servicio</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary">Producto</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td data-col="categoria">${p.categoria.nombre}</td>
                <c:if test="${logueado}">
                <td data-col="precio_venta">${p.precio}</td>
                <td data-col="existencias">
                    <c:choose>
                        <c:when test="${p.esServicio}">
                            <span class="text-muted">N/A</span>
                        </c:when>
                        <c:otherwise>
                            ${p.existencias}
                            <c:if test="${p.existencias == 0}">
                                <span class="badge bg-danger ms-1">Agotado</span>
                            </c:if>
                            <c:if test="${p.existencias > 0 && p.existencias <= stockMinimo}">
                                <span class="badge bg-warning text-dark ms-1">Stock bajo</span>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </td>
                <c:if test="${esAdmin}">
                    <td data-col="precio_compra">
                        <c:choose>
                            <c:when test="${p.esServicio}">
                                <span class="text-muted">N/A</span>
                            </c:when>
                            <c:when test="${p.precioCompra > 0}">
                                $${p.precioCompra}
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">—</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td data-col="porcentaje_ganancia">
                        <c:choose>
                            <c:when test="${p.esServicio}">
                                <span class="text-muted">N/A</span>
                            </c:when>
                            <c:when test="${p.porcentajeGanancia > 0}">
                                ${p.porcentajeGanancia}%
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">—</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td data-col="margen">
                        <c:choose>
                            <c:when test="${p.esServicio}">
                                <span class="text-muted">N/A</span>
                            </c:when>
                            <c:when test="${p.tieneMargenCalculable}">
                                <span class="${p.margen < 0 ? 'text-danger' : ''}">$${p.margen}</span>
                                <span class="badge ${p.margen < 0 ? 'bg-danger' : 'bg-success'} ms-1"
                                      title="Ganancia real sobre el costo">${p.margenPorcentaje}%</span>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">—</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </c:if>
                <td data-col="agregar">
                    <c:choose>
                        <c:when test="${p.esServicio || p.existencias > 0}">
                            <button type="button" class="btn btn-sm btn-primary btn-agregar-carro"
                                    data-producto-id="${p.id}"
                                    data-producto-nombre="${fn:escapeXml(p.nombre)}"
                                    data-producto-precio="${p.precio}">Agregar</button>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted small">Sin stock</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                </c:if>
            </tr>
            </c:forEach>
            </tbody>
        </table>
        </div>
    </div>

    <c:if test="${logueado}">
    <div class="col-lg-4">
        <%@ include file="fragmentos/panel-carro-catalogo.jspf" %>
    </div>
    </c:if>
</div>

<p>${applicationScope.mensaje}</p>
<p>${requestScope.mensaje}</p>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
<script src="${pageContext.request.contextPath}/js/catalogo-carro.js"></script>
<script src="${pageContext.request.contextPath}/js/buscador-tabla.js"></script>
<script src="${pageContext.request.contextPath}/js/filtros-columnas-tabla.js"></script>
<script src="${pageContext.request.contextPath}/js/escaneo-producto.js"></script>
<script src="${pageContext.request.contextPath}/js/aviso-bienvenida.js"></script>
</body>
</html>
