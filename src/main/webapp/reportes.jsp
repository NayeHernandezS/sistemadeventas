<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Reporte de Ventas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
</head>
<body>
<div class="container my-4">
    <h1 class="mb-4">Reporte de Ventas</h1>

    <form class="row g-3 mb-4" method="get" action="${pageContext.request.contextPath}/reportes">
        <div class="col-md-3">
            <label class="form-label" for="fechaInicio">Fecha inicio</label>
            <input class="form-control" type="date" id="fechaInicio" name="fechaInicio" value="${fechaInicio}">
        </div>
        <div class="col-md-3">
            <label class="form-label" for="fechaFin">Fecha fin</label>
            <input class="form-control" type="date" id="fechaFin" name="fechaFin" value="${fechaFin}">
        </div>
        <div class="col-md-3">
            <label class="form-label" for="vendedor">Vendedor</label>
            <select class="form-select" id="vendedor" name="vendedor">
                <option value="">Todos</option>
                <c:forEach items="${vendedores}" var="ven">
                    <option value="${ven}" ${ven == vendedorSeleccionado ? 'selected' : ''}>${ven}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-3 d-flex align-items-end gap-2">
            <button class="btn btn-primary" type="submit">Filtrar</button>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/reportes">Limpiar</a>
        </div>
    </form>

    <div class="alert alert-info mb-4">
        <strong>Resultado de filtros:</strong>
        Tickets ${cantidadFiltrada} | Total $${totalFiltrado}
    </div>

    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">Hoy</h5>
                    <p class="mb-1"><strong>Tickets:</strong> ${cantidadDia}</p>
                    <p class="mb-0"><strong>Total:</strong> $${totalDia}</p>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">Semana actual</h5>
                    <p class="mb-1"><strong>Tickets:</strong> ${cantidadSemana}</p>
                    <p class="mb-0"><strong>Total:</strong> $${totalSemana}</p>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">Mes actual</h5>
                    <p class="mb-1"><strong>Tickets:</strong> ${cantidadMes}</p>
                    <p class="mb-0"><strong>Total:</strong> $${totalMes}</p>
                </div>
            </div>
        </div>
    </div>

    <h3>Detalle del día</h3>
    <c:choose>
        <c:when test="${empty ticketsDia}">
            <div class="alert alert-warning">No hay ventas registradas hoy.</div>
        </c:when>
        <c:otherwise>
            <table class="table table-sm table-striped mb-4">
                <thead>
                <tr>
                    <th>Folio</th>
                    <th>Vendedor</th>
                    <th>Fecha</th>
                    <th>Total</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${ticketsDia}" var="ticket">
                    <tr>
                        <td>${ticket.folio}</td>
                        <td>${ticket.usernameVendedor}</td>
                        <td>${ticket.fechaVenta}</td>
                        <td>$${ticket.total}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <h3>Detalle de la semana</h3>
    <c:choose>
        <c:when test="${empty ticketsSemana}">
            <div class="alert alert-warning">No hay ventas registradas esta semana.</div>
        </c:when>
        <c:otherwise>
            <table class="table table-sm table-striped mb-4">
                <thead>
                <tr>
                    <th>Folio</th>
                    <th>Vendedor</th>
                    <th>Fecha</th>
                    <th>Total</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${ticketsSemana}" var="ticket">
                    <tr>
                        <td>${ticket.folio}</td>
                        <td>${ticket.usernameVendedor}</td>
                        <td>${ticket.fechaVenta}</td>
                        <td>$${ticket.total}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <h3>Detalle del mes</h3>
    <c:choose>
        <c:when test="${empty ticketsMes}">
            <div class="alert alert-warning">No hay ventas registradas este mes.</div>
        </c:when>
        <c:otherwise>
            <table class="table table-sm table-striped mb-4">
                <thead>
                <tr>
                    <th>Folio</th>
                    <th>Vendedor</th>
                    <th>Fecha</th>
                    <th>Total</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${ticketsMes}" var="ticket">
                    <tr>
                        <td>${ticket.folio}</td>
                        <td>${ticket.usernameVendedor}</td>
                        <td>${ticket.fechaVenta}</td>
                        <td>$${ticket.total}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/index.jsp">Volver</a>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/tickets">Ver tickets</a>
</div>
</body>
</html>
