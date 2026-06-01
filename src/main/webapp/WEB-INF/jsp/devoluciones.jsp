<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Devoluciones</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <h1 class="h3 mb-3"><i class="bi bi-arrow-return-left"></i> Devoluciones</h1>
    <p class="text-muted">Registra devoluciones de ventas. El inventario se reintegra automaticamente.</p>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <div class="card mb-4">
        <div class="card-body">
            <h2 class="h6">Nueva devolucion por folio de ticket</h2>
            <form method="get" action="${pageContext.request.contextPath}/devoluciones/nueva" class="row g-2 align-items-end">
                <div class="col-md-8">
                    <label class="form-label" for="folio">Folio del ticket (ej. TCK-...)</label>
                    <input type="text" class="form-control" id="folio" name="folio" placeholder="TCK-20260527164452-ABC123" required>
                </div>
                <div class="col-md-4">
                    <button type="submit" class="btn btn-primary w-100">Buscar ticket</button>
                </div>
            </form>
        </div>
    </div>

    <h2 class="h5">Historial de devoluciones</h2>
    <c:if test="${empty devoluciones}">
        <p class="text-muted">Aun no hay devoluciones registradas.</p>
    </c:if>

    <c:if test="${not empty devoluciones}">
        <div class="table-responsive">
            <table class="table table-striped align-middle">
                <thead>
                <tr>
                    <th>Folio dev.</th>
                    <th>Ticket</th>
                    <th>Fecha</th>
                    <th>Registro</th>
                    <th>Motivo</th>
                    <th>Total</th>
                    <th>Detalle</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${devoluciones}" var="d">
                    <tr>
                        <td><strong>${d.folio}</strong></td>
                        <td>${d.ticketFolio}</td>
                        <td>${d.fechaDevolucion}</td>
                        <td>${d.usernameRegistro}</td>
                        <td>${empty d.motivo ? '—' : d.motivo}</td>
                        <td>$${d.totalDevuelto}</td>
                        <td>
                            <ul class="small mb-0">
                                <c:forEach items="${d.items}" var="it">
                                    <li>${it.nombreProducto} x${it.cantidad} ($${it.importe})</li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/">Inicio</a>
    <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/tickets">Ver tickets</a>
</div>
</body>
</html>
