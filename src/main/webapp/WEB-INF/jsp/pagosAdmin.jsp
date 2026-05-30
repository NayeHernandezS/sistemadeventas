<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Estado de pagos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<div class="container py-4">
    <h1 class="h3 mb-3">Estado de pagos de suscripcion</h1>

    <div class="alert alert-info">
        Las solicitudes quedan en estado <strong>PENDIENTE</strong> hasta que la plataforma confirme el pago recibido.
        No puedes confirmar tu propio pago desde aqui.
    </div>

    <a href="${pageContext.request.contextPath}/suscripcion" class="btn btn-outline-primary mb-3">Solicitar nuevo pago</a>
    <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mb-3 ms-2">Volver</a>

    <c:if test="${empty pagosPendientes}">
        <p class="text-muted">No tienes pagos pendientes de confirmacion.</p>
    </c:if>

    <c:if test="${not empty pagosPendientes}">
        <table class="table table-striped">
            <thead>
            <tr>
                <th>Plan</th>
                <th>Meses</th>
                <th>Monto</th>
                <th>Solicitado</th>
                <th>Estado</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${pagosPendientes}" var="p">
                <tr>
                    <td>${p.planCodigo}</td>
                    <td>${p.meses}</td>
                    <td>$<fmt:formatNumber value="${p.monto}" minFractionDigits="2"/></td>
                    <td>${p.fechaSolicitud}</td>
                    <td><span class="badge bg-warning text-dark">Pendiente de confirmacion</span></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>
</div>
</body>
</html>
