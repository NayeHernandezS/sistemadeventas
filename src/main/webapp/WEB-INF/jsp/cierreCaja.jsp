<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Cierre de caja</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-4">
        <div>
            <h1 class="mb-1">Cierre de caja</h1>
            <p class="text-muted small mb-0">
                Resumen del día con totales netos (después de devoluciones).
                <c:if test="${not cierre.esAdmin}">
                    Solo ves tus ventas.
                </c:if>
            </p>
        </div>
        <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/reportes">
            <i class="bi bi-bar-chart"></i> Reportes
        </a>
    </div>

    <form class="row g-3 mb-4" method="get" action="${pageContext.request.contextPath}/reportes/cierre">
        <div class="col-md-4">
            <label class="form-label" for="fecha">Fecha</label>
            <input class="form-control" type="date" id="fecha" name="fecha" value="${cierre.fecha}">
        </div>
        <div class="col-md-8 d-flex align-items-end gap-2 flex-wrap">
            <button class="btn btn-primary" type="submit">Consultar</button>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/reportes/cierre">Hoy</a>
            <c:url var="exportUrl" value="/reportes/cierre/export">
                <c:param name="fecha" value="${cierre.fecha}"/>
            </c:url>
            <a class="btn btn-success" href="${exportUrl}">
                <i class="bi bi-download"></i> Exportar CSV
            </a>
        </div>
    </form>

    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="card h-100 border-0 shadow-sm">
                <div class="card-body">
                    <p class="text-muted small mb-1">Tickets</p>
                    <p class="h3 mb-0">${cierre.cantidadTickets}</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100 border-0 shadow-sm">
                <div class="card-body">
                    <p class="text-muted small mb-1">Bruto</p>
                    <p class="h3 mb-0">$${cierre.totalBruto}</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100 border-0 shadow-sm">
                <div class="card-body">
                    <p class="text-muted small mb-1">Devuelto</p>
                    <p class="h3 mb-0 text-danger">$${cierre.totalDevuelto}</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100 border-0 shadow-sm border-success border-2">
                <div class="card-body">
                    <p class="text-muted small mb-1">Neto del día</p>
                    <p class="h3 mb-0 text-success">$${cierre.totalNeto}</p>
                </div>
            </div>
        </div>
    </div>

    <div class="alert alert-light border mb-4">
        <strong>Comparación con ayer:</strong>
        ${cierre.cantidadTicketsAyer} tickets · $${cierre.totalNetoAyer} neto
        <c:choose>
            <c:when test="${cierre.diferenciaNetoAyer > 0}">
                · <span class="text-success">+$${cierre.diferenciaNetoAyer} vs ayer</span>
            </c:when>
            <c:when test="${cierre.diferenciaNetoAyer < 0}">
                · <span class="text-danger">$${cierre.diferenciaNetoAyer} vs ayer</span>
            </c:when>
            <c:otherwise>
                · <span class="text-muted">igual que ayer</span>
            </c:otherwise>
        </c:choose>
    </div>

    <c:if test="${cierre.esAdmin && not empty cierre.ventasPorVendedor}">
    <h3 class="h5">Ventas por vendedor</h3>
    <table class="table table-sm table-striped mb-4">
        <thead>
        <tr>
            <th>Vendedor</th>
            <th class="text-end">Tickets</th>
            <th class="text-end">Neto</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${cierre.ventasPorVendedor}" var="v">
            <tr>
                <td>${v.vendedor}</td>
                <td class="text-end">${v.cantidadTickets}</td>
                <td class="text-end">$${v.totalNeto}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    </c:if>

    <h3 class="h5">Top productos del día</h3>
    <c:choose>
        <c:when test="${empty cierre.topProductos}">
            <div class="alert alert-warning">Sin ventas de productos en esta fecha.</div>
        </c:when>
        <c:otherwise>
            <table class="table table-sm table-striped mb-4">
                <thead>
                <tr>
                    <th>Producto</th>
                    <th class="text-end">Unidades</th>
                    <th class="text-end">Importe</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${cierre.topProductos}" var="p">
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

    <h3 class="h5">Detalle de tickets</h3>
    <c:choose>
        <c:when test="${empty cierre.tickets}">
            <div class="alert alert-warning">No hay ventas registradas en esta fecha.</div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table table-sm table-striped mb-4">
                    <thead>
                    <tr>
                        <th>Folio</th>
                        <th>Vendedor</th>
                        <th>Fecha</th>
                        <th class="text-end">Bruto</th>
                        <th class="text-end">Devuelto</th>
                        <th class="text-end">Neto</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${cierre.tickets}" var="ticket">
                        <tr>
                            <td>
                                <a href="${pageContext.request.contextPath}/ticket?id=${ticket.id}">${ticket.folio}</a>
                            </td>
                            <td>${ticket.usernameVendedor}</td>
                            <td>${ticket.fechaVenta}</td>
                            <td class="text-end">$${ticket.total}</td>
                            <td class="text-end">$${cierre.totalDevuelto(ticket)}</td>
                            <td class="text-end">$${cierre.totalNeto(ticket)}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>

    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/inicio">Volver</a>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/tickets">Ver tickets</a>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
