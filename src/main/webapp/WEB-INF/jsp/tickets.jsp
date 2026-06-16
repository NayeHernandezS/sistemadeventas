<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Mis tickets</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 class="h3 mb-0"><i class="bi bi-ticket-detailed"></i> Tickets de venta</h1>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/productos">
            <i class="bi bi-cart-plus"></i> Nueva venta
        </a>
    </div>

    <c:choose>
        <c:when test="${empty tickets}">
            <div class="alert alert-warning">No hay tickets registrados.</div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table table-hover table-striped align-middle">
                    <thead>
                    <tr>
                        <th>Folio</th>
                        <th>Fecha</th>
                        <th>Cliente</th>
                        <th>Vendedor</th>
                        <th class="text-end">Total</th>
                        <th>Estado</th>
                        <th class="text-end">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${tickets}" var="ticket">
                        <tr>
                            <td><strong>${ticket.folio}</strong></td>
                            <td>${ticket.fechaVenta}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${ticket.tieneNombreCliente}">${ticket.nombreCliente}</c:when>
                                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>${ticket.usernameVendedor}</td>
                            <td class="text-end">$${ticket.total}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${ticket.estado eq 'DEVUELTO_TOTAL'}">
                                        <span class="badge bg-secondary">Devuelto total</span>
                                    </c:when>
                                    <c:when test="${ticket.estado eq 'DEVUELTO_PARCIAL'}">
                                        <span class="badge bg-warning text-dark">Dev. parcial</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-success">Vigente</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <div class="d-flex flex-wrap gap-1 justify-content-end">
                                    <a class="btn btn-sm btn-primary"
                                       href="${pageContext.request.contextPath}/tickets/ver?id=${ticket.id}">
                                        <i class="bi bi-eye"></i> Ver ticket
                                    </a>
                                    <a class="btn btn-sm btn-outline-primary"
                                       href="${pageContext.request.contextPath}/factura?folioTicket=${ticket.folio}">
                                        Factura
                                    </a>
                                    <c:if test="${ticket.estado ne 'DEVUELTO_TOTAL'}">
                                        <a class="btn btn-sm btn-outline-warning"
                                           href="${pageContext.request.contextPath}/devoluciones/nueva?ticketId=${ticket.id}">
                                            Devolucion
                                        </a>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>

    <a class="btn btn-secondary mt-2" href="${pageContext.request.contextPath}/inicio">Inicio</a>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
