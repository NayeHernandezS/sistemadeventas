<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Compras de ${cliente.nombre}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <div>
            <h1 class="h3 mb-1"><i class="bi bi-person-lines-fill"></i> Compras de ${cliente.nombre}</h1>
            <p class="text-muted mb-0 small">
                Tickets donde el nombre del cliente coincide con el registro del catalogo.
            </p>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/clientes">
            <i class="bi bi-arrow-left"></i> Volver a clientes
        </a>
    </div>

    <div class="card mb-3">
        <div class="card-body py-3">
            <div class="row g-2 small">
                <div class="col-md-4"><strong>RFC:</strong> <c:out value="${empty cliente.rfc ? '—' : cliente.rfc}"/></div>
                <div class="col-md-4"><strong>Correo:</strong> <c:out value="${empty cliente.email ? '—' : cliente.email}"/></div>
                <div class="col-md-4">
                    <a class="btn btn-sm btn-success"
                       href="${pageContext.request.contextPath}/carro/ver?clienteId=${cliente.id}">
                        <i class="bi bi-cart-plus"></i> Nueva venta para este cliente
                    </a>
                </div>
            </div>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty tickets}">
            <div class="alert alert-info">
                Aun no hay tickets con el nombre <strong>${cliente.nombre}</strong>.
                Al cobrar, escribe el mismo nombre en el carrito o entra desde aqui con el boton de nueva venta.
            </div>
        </c:when>
        <c:otherwise>
            <p class="text-muted small mb-2">${tickets.size()} compra(s) reciente(s)</p>
            <div class="table-responsive">
                <table class="table table-hover table-striped align-middle">
                    <thead>
                    <tr>
                        <th>Folio</th>
                        <th>Fecha</th>
                        <th>Vendedor</th>
                        <th class="text-end">Total</th>
                        <th>Estado</th>
                        <th class="text-end"></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${tickets}" var="ticket">
                        <tr>
                            <td><strong>${ticket.folio}</strong></td>
                            <td>${ticket.fechaVenta}</td>
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
                                <a class="btn btn-sm btn-primary"
                                   href="${pageContext.request.contextPath}/tickets/ver?id=${ticket.id}">
                                    Ver ticket
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <a class="btn btn-outline-primary btn-sm"
               href="${pageContext.request.contextPath}/tickets?q=${cliente.nombre}">
                Ver todos los tickets con este nombre
            </a>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
