<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Confirmar pagos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<div class="container py-4">
    <h1 class="h3 mb-4">Pagos de suscripcion pendientes</h1>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>

    <a href="${pageContext.request.contextPath}/" class="btn btn-secondary mb-3">Volver</a>

    <c:if test="${empty pagosPendientes}">
        <p class="text-muted">No hay pagos pendientes.</p>
    </c:if>

    <c:if test="${not empty pagosPendientes}">
        <table class="table table-striped">
            <thead>
            <tr>
                <th>Usuario</th>
                <th>Meses</th>
                <th>Monto</th>
                <th>Solicitado</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${pagosPendientes}" var="p">
                <tr>
                    <td>${p.username}</td>
                    <td>${p.meses}</td>
                    <td>$<fmt:formatNumber value="${p.monto}" minFractionDigits="2"/></td>
                    <td>${p.fechaSolicitud}</td>
                    <td>
                        <form method="post" action="${pageContext.request.contextPath}/admin/pagos" class="d-inline">
                        <%@ include file="csrf.jspf" %>
                            <input type="hidden" name="pagoId" value="${p.id}">
                            <button type="submit" class="btn btn-sm btn-success">Confirmar pago</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>
</div>
</body>
</html>
