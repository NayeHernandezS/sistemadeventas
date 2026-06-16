<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Recetas y costos</title>
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="mb-2"><i class="bi bi-journal-richtext"></i> Recetas y costos</h1>
    <p class="text-muted mb-4">
        Arma la receta de cada platillo con insumos de tu inventario. El costo usa el
        <strong>precio de compra</strong> del insumo (o su precio de venta si no tiene costo).
    </p>

    <c:if test="${not empty sessionScope.mensajeError}">
        <div class="alert alert-danger">${sessionScope.mensajeError}</div>
        <c:remove var="mensajeError" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.mensajeExito}">
        <div class="alert alert-success">${sessionScope.mensajeExito}</div>
        <c:remove var="mensajeExito" scope="session"/>
    </c:if>

    <div class="mb-3">
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/inicio">Volver</a>
        <a class="btn btn-outline-secondary ms-2" href="${pageContext.request.contextPath}/crudprod">Inventario</a>
        <a class="btn btn-primary ms-2" href="${pageContext.request.contextPath}/recetas/form">
            <i class="bi bi-plus-lg"></i> Nueva o editar receta
        </a>
    </div>

    <c:choose>
        <c:when test="${empty platillos}">
            <div class="alert alert-info">
                No hay platillos en categorias Comida, Bebidas o Postres.
                Agrega productos en esas categorias desde tu inventario.
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table table-hover table-striped align-middle">
                    <thead>
                    <tr>
                        <th>Platillo</th>
                        <th>Categoria</th>
                        <th class="text-end">Precio venta</th>
                        <th class="text-end">Costo receta</th>
                        <th class="text-end">Margen</th>
                        <th class="text-center">Ingredientes</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${platillos}" var="p">
                        <tr>
                            <td>${p.nombre}</td>
                            <td><span class="badge text-bg-secondary">${p.categoria}</span></td>
                            <td class="text-end">$${p.precioVenta}</td>
                            <td class="text-end">
                                <c:choose>
                                    <c:when test="${p.tieneReceta}">$${p.costoReceta}</c:when>
                                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <c:choose>
                                    <c:when test="${p.tieneCostoCalculable}">
                                        $${p.margenPesos}
                                        <small class="text-muted">(${p.margenPorcentaje}%)</small>
                                    </c:when>
                                    <c:when test="${p.tieneReceta}">
                                        <span class="text-warning">Revisa costos</span>
                                    </c:when>
                                    <c:otherwise><span class="text-muted">Sin receta</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-center">${p.cantidadLineas}</td>
                            <td class="text-nowrap">
                                <a class="btn btn-sm btn-success"
                                   href="${pageContext.request.contextPath}/recetas/form?productoId=${p.productoId}">
                                    Editar
                                </a>
                                <c:if test="${p.tieneReceta}">
                                    <a class="btn btn-sm btn-outline-danger ms-1"
                                       onclick="return confirm('¿Eliminar la receta de este platillo?');"
                                       href="${pageContext.request.contextPath}/recetas/eliminar?productoId=${p.productoId}">
                                        Quitar
                                    </a>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
