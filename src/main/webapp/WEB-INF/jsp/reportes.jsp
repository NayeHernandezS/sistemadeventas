<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Reporte de Ventas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <h1 class="mb-4">Reporte de Ventas</h1>
    <p class="text-muted small">Los totales <strong>netos</strong> restan las devoluciones registradas por ticket.</p>

    <form class="row g-3 mb-4" method="get" action="${pageContext.request.contextPath}/reportes">
        <div class="col-md-3">
            <label class="form-label" for="fechaInicio">Fecha inicio</label>
            <input class="form-control" type="date" id="fechaInicio" name="fechaInicio" value="${reporte.fechaInicio}">
        </div>
        <div class="col-md-3">
            <label class="form-label" for="fechaFin">Fecha fin</label>
            <input class="form-control" type="date" id="fechaFin" name="fechaFin" value="${reporte.fechaFin}">
        </div>
        <div class="col-md-3">
            <label class="form-label" for="vendedor">Vendedor</label>
            <select class="form-select" id="vendedor" name="vendedor">
                <option value="">Todos</option>
                <c:forEach items="${reporte.vendedores}" var="ven">
                    <option value="${ven}" ${ven == reporte.vendedorSeleccionado ? 'selected' : ''}>${ven}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-3 d-flex align-items-end gap-2 flex-wrap">
            <button class="btn btn-primary" type="submit">Filtrar</button>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/reportes">Limpiar</a>
            <c:url var="exportUrl" value="/reportes/export">
                <c:param name="fechaInicio" value="${reporte.fechaInicio}"/>
                <c:param name="fechaFin" value="${reporte.fechaFin}"/>
                <c:param name="vendedor" value="${reporte.vendedorSeleccionado}"/>
            </c:url>
            <a class="btn btn-success" href="${exportUrl}">
                <i class="bi bi-download"></i> Exportar CSV
            </a>
        </div>
    </form>

    <div class="alert alert-info mb-4">
        <strong>Resultado de filtros:</strong>
        Tickets ${reporte.cantidadFiltrada} |
        Bruto $${reporte.totalFiltradoBruto} |
        Devuelto $${reporte.totalDevueltoFiltrado} |
        <strong>Neto $${reporte.totalFiltradoNeto}</strong>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">Hoy</h5>
                    <p class="mb-1"><strong>Tickets:</strong> ${reporte.cantidadDia}</p>
                    <p class="mb-1"><strong>Bruto:</strong> $${reporte.totalDiaBruto}</p>
                    <p class="mb-0"><strong>Neto:</strong> $${reporte.totalDiaNeto}</p>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">Semana actual</h5>
                    <p class="mb-1"><strong>Tickets:</strong> ${reporte.cantidadSemana}</p>
                    <p class="mb-1"><strong>Bruto:</strong> $${reporte.totalSemanaBruto}</p>
                    <p class="mb-0"><strong>Neto:</strong> $${reporte.totalSemanaNeto}</p>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">Mes actual</h5>
                    <p class="mb-1"><strong>Tickets:</strong> ${reporte.cantidadMes}</p>
                    <p class="mb-1"><strong>Bruto:</strong> $${reporte.totalMesBruto}</p>
                    <p class="mb-0"><strong>Neto:</strong> $${reporte.totalMesNeto}</p>
                </div>
            </div>
        </div>
    </div>

    <h3>Detalle del día</h3>
    <c:choose>
        <c:when test="${empty reporte.ticketsDia}">
            <div class="alert alert-warning">No hay ventas registradas hoy.</div>
        </c:when>
        <c:otherwise>
            <table class="table table-sm table-striped mb-4">
                <thead>
                <tr>
                    <th>Folio</th>
                    <th>Vendedor</th>
                    <th>Fecha</th>
                    <th>Bruto</th>
                    <th>Devuelto</th>
                    <th>Neto</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${reporte.ticketsDia}" var="ticket">
                    <tr>
                        <td>${ticket.folio}</td>
                        <td>${ticket.usernameVendedor}</td>
                        <td>${ticket.fechaVenta}</td>
                        <td>$${ticket.total}</td>
                        <td>$${reporte.totalDevuelto(ticket)}</td>
                        <td>$${reporte.totalNeto(ticket)}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <h3>Detalle de la semana</h3>
    <c:choose>
        <c:when test="${empty reporte.ticketsSemana}">
            <div class="alert alert-warning">No hay ventas registradas esta semana.</div>
        </c:when>
        <c:otherwise>
            <table class="table table-sm table-striped mb-4">
                <thead>
                <tr>
                    <th>Folio</th>
                    <th>Vendedor</th>
                    <th>Fecha</th>
                    <th>Bruto</th>
                    <th>Devuelto</th>
                    <th>Neto</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${reporte.ticketsSemana}" var="ticket">
                    <tr>
                        <td>${ticket.folio}</td>
                        <td>${ticket.usernameVendedor}</td>
                        <td>${ticket.fechaVenta}</td>
                        <td>$${ticket.total}</td>
                        <td>$${reporte.totalDevuelto(ticket)}</td>
                        <td>$${reporte.totalNeto(ticket)}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <h3>Detalle del mes</h3>
    <c:choose>
        <c:when test="${empty reporte.ticketsMes}">
            <div class="alert alert-warning">No hay ventas registradas este mes.</div>
        </c:when>
        <c:otherwise>
            <table class="table table-sm table-striped mb-4">
                <thead>
                <tr>
                    <th>Folio</th>
                    <th>Vendedor</th>
                    <th>Fecha</th>
                    <th>Bruto</th>
                    <th>Devuelto</th>
                    <th>Neto</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${reporte.ticketsMes}" var="ticket">
                    <tr>
                        <td>${ticket.folio}</td>
                        <td>${ticket.usernameVendedor}</td>
                        <td>${ticket.fechaVenta}</td>
                        <td>$${ticket.total}</td>
                        <td>$${reporte.totalDevuelto(ticket)}</td>
                        <td>$${reporte.totalNeto(ticket)}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/inicio">Volver</a>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/tickets">Ver tickets</a>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
