<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tickets de Venta</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<div class="container my-4">
    <h1>Tickets de Venta</h1>
    <c:choose>
        <c:when test="${empty tickets}">
            <div class="alert alert-warning">No hay tickets registrados.</div>
        </c:when>
        <c:otherwise>
            <c:forEach items="${tickets}" var="ticket">
                <div class="card mb-3">
                    <div class="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
                        <span><strong>Folio:</strong> ${ticket.folio}</span>
                        <c:choose>
                            <c:when test="${ticket.estado eq 'DEVUELTO_TOTAL'}">
                                <span class="badge bg-secondary">Devuelto total</span>
                            </c:when>
                            <c:when test="${ticket.estado eq 'DEVUELTO_PARCIAL'}">
                                <span class="badge bg-warning text-dark">Devolucion parcial</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-success">Activo</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="card-body">
                        <p class="mb-1"><strong>Vendedor:</strong> ${ticket.usernameVendedor}</p>
                        <p class="mb-1"><strong>Fecha:</strong> ${ticket.fechaVenta}</p>
                        <p class="mb-3"><strong>Total:</strong> $${ticket.total}</p>
                        <p class="mb-2 d-flex flex-wrap gap-2">
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/factura?folioTicket=${ticket.folio}">Ver / imprimir factura</a>
                            <c:if test="${ticket.estado ne 'DEVUELTO_TOTAL'}">
                            <a class="btn btn-sm btn-outline-warning" href="${pageContext.request.contextPath}/devoluciones/nueva?ticketId=${ticket.id}">
                                <i class="bi bi-arrow-return-left"></i> Devolucion
                            </a>
                            </c:if>
                        </p>
                        <table class="table table-sm table-striped">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Producto</th>
                                    <th>Precio</th>
                                    <th>Cantidad</th>
                                    <th>Importe</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${ticket.items}" var="item">
                                    <tr>
                                        <td>${item.productoId}</td>
                                        <td>${item.nombreProducto}</td>
                                        <td>$${item.precioUnitario}</td>
                                        <td>${item.cantidad}</td>
                                        <td>$${item.importe}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/">Volver</a>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/productos">Nueva venta</a>
</div>
</body>
</html>
