<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Registrar devolucion</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <h1 class="h3 mb-3">Devolucion — ticket ${ticket.folio}</h1>

    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <div class="card mb-3">
        <div class="card-body">
            <p class="mb-1"><strong>Vendedor:</strong> ${ticket.usernameVendedor}</p>
            <p class="mb-1"><strong>Fecha venta:</strong> ${ticket.fechaVenta}</p>
            <p class="mb-0"><strong>Total venta:</strong> $${ticket.total}</p>
            <p class="mb-0"><strong>Estado:</strong> ${ticket.estado}</p>
        </div>
    </div>

    <form method="post" action="${pageContext.request.contextPath}/devoluciones/registrar">
        <%@ include file="csrf.jspf" %>
        <input type="hidden" name="ticketId" value="${ticket.id}">

        <div class="mb-3">
            <label for="motivo" class="form-label">Motivo (opcional)</label>
            <input type="text" class="form-control" id="motivo" name="motivo" maxlength="255"
                   value="${motivo}" placeholder="Ej. producto defectuoso, cambio de talla">
        </div>

        <table class="table table-bordered">
            <thead>
            <tr>
                <th>Producto</th>
                <th>Precio</th>
                <th>Vendido</th>
                <th>Ya devuelto</th>
                <th>Devolver ahora</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${lineas}" var="l">
                <tr>
                    <td>${l.nombreProducto}</td>
                    <td>$${l.precioUnitario}</td>
                    <td>${l.cantidadVendida}</td>
                    <td>${l.cantidadYaDevuelta}</td>
                    <td>
                        <input type="number" class="form-control form-control-sm"
                               name="cant_${l.productoId}" min="0" max="${l.cantidadDisponible}"
                               value="0" style="max-width:5rem">
                        <span class="small text-muted">max ${l.cantidadDisponible}</span>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <button type="submit" class="btn btn-primary">Registrar devolucion</button>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/devoluciones">Cancelar</a>
    </form>
</div>
</body>
</html>
