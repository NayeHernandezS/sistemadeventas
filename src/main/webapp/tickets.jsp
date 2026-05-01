<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tickets de Venta</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
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
                    <div class="card-header">
                        <strong>Folio:</strong> ${ticket.folio}
                    </div>
                    <div class="card-body">
                        <p class="mb-1"><strong>Vendedor:</strong> ${ticket.usernameVendedor}</p>
                        <p class="mb-1"><strong>Fecha:</strong> ${ticket.fechaVenta}</p>
                        <p class="mb-3"><strong>Total:</strong> $${ticket.total}</p>
                        <p class="mb-2">
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/factura?folioTicket=${ticket.folio}">Ver / imprimir factura</a>
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
    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/index.jsp">Volver</a>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/productos">Nueva venta</a>
</div>
</body>
</html>
