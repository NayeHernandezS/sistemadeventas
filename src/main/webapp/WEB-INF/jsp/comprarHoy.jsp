<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Comprar hoy</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-4">
        <div>
            <h1 class="mb-1">Comprar hoy</h1>
            <p class="text-muted small mb-0">
                Productos agotados o con stock bajo (umbral: ${lista.stockMinimo} uds.).
                La cantidad sugerida lleva el inventario al doble del umbral.
            </p>
        </div>
        <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/crudprod">
            <i class="bi bi-box-seam"></i> Inventario
        </a>
    </div>

    <div class="d-flex flex-wrap gap-2 mb-4">
        <a class="btn btn-success" href="${pageContext.request.contextPath}/inventario/comprar-hoy/export">
            <i class="bi bi-download"></i> Exportar CSV
        </a>
        <a class="btn btn-outline-warning" href="${pageContext.request.contextPath}/crudprod?alerta=1">
            <i class="bi bi-funnel"></i> Ver solo con alerta
        </a>
    </div>

    <c:choose>
        <c:when test="${lista.totalProductos == 0}">
            <div class="alert alert-success">
                <i class="bi bi-check-circle"></i>
                No hay productos pendientes de reponer. Tu inventario esta en buen nivel.
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-3 mb-4">
                <div class="col-md-4">
                    <div class="card border-0 shadow-sm h-100">
                        <div class="card-body">
                            <p class="text-muted small mb-1">Productos</p>
                            <p class="h3 mb-0">${lista.totalProductos}</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card border-0 shadow-sm h-100">
                        <div class="card-body">
                            <p class="text-muted small mb-1">Unidades sugeridas</p>
                            <p class="h3 mb-0">${lista.totalUnidadesSugeridas}</p>
                        </div>
                    </div>
                </div>
                <c:if test="${esAdmin && lista.costoEstimadoTotal > 0}">
                <div class="col-md-4">
                    <div class="card border-0 shadow-sm h-100 border-warning border-2">
                        <div class="card-body">
                            <p class="text-muted small mb-1">Costo estimado de reposicion</p>
                            <p class="h3 mb-0 text-warning">$${lista.costoEstimadoTotal}</p>
                        </div>
                    </div>
                </div>
                </c:if>
            </div>

            <div class="table-responsive">
                <table class="table table-sm table-striped align-middle">
                    <thead>
                    <tr>
                        <th>Producto</th>
                        <th>SKU</th>
                        <th>Categoria</th>
                        <th class="text-end">Existencias</th>
                        <th>Alerta</th>
                        <th class="text-end">Comprar</th>
                        <th class="text-end">Vendidas 7d</th>
                        <c:if test="${esAdmin}">
                        <th class="text-end">Costo est.</th>
                        </c:if>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${lista.productos}" var="p">
                        <tr class="${p.agotado ? 'table-danger' : 'table-warning'}">
                            <td>${p.nombre}</td>
                            <td><code class="small">${empty p.sku ? '—' : p.sku}</code></td>
                            <td>${empty p.categoria ? '—' : p.categoria}</td>
                            <td class="text-end">${p.existencias}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.agotado}">
                                        <span class="badge bg-danger">Agotado</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-warning text-dark">Stock bajo</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end fw-semibold">${p.cantidadSugerida}</td>
                            <td class="text-end">${p.unidadesVendidas7d}</td>
                            <c:if test="${esAdmin}">
                            <td class="text-end">
                                <c:choose>
                                    <c:when test="${p.costoEstimadoReposicion > 0}">$${p.costoEstimadoReposicion}</c:when>
                                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                </c:choose>
                            </td>
                            </c:if>
                            <td class="text-end">
                                <c:if test="${esAdmin}">
                                <a class="btn btn-sm btn-outline-primary"
                                   href="${pageContext.request.contextPath}/inventario/ajuste?id=${p.productoId}">
                                    Entrada
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

    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/inicio">Volver</a>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
