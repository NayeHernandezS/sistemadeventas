<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Estado de pagos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="h3 mb-3">Estado de pagos de suscripcion</h1>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <div class="alert alert-info">
        Si abandonaste el checkout de Mercado Pago o fue un intento fallido, puedes
        <strong>cancelar</strong> la solicitud pendiente y volver a pagar en Suscripcion.
        Las solicitudes no canceladas expiran solas (15 dias MP / 30 dias manual).
    </div>

    <a href="${pageContext.request.contextPath}/suscripcion" class="btn btn-outline-primary mb-3">Ir a Suscripcion</a>
    <a href="${pageContext.request.contextPath}/inicio" class="btn btn-secondary mb-3 ms-2">Volver</a>

    <c:if test="${empty pagosPendientes}">
        <p class="text-muted">No tienes pagos pendientes. Puedes solicitar uno nuevo en Suscripcion.</p>
    </c:if>

    <c:if test="${not empty pagosPendientes}">
        <table class="table table-striped align-middle">
            <thead>
            <tr>
                <th>Plan</th>
                <th>Meses</th>
                <th>Monto</th>
                <th>Canal</th>
                <th>Solicitado</th>
                <th>Estado</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${pagosPendientes}" var="p">
                <tr>
                    <td>${p.planCodigo}</td>
                    <td>${p.meses}</td>
                    <td>$<fmt:formatNumber value="${p.monto}" minFractionDigits="2"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${p.canal eq 'MERCADOPAGO'}">Mercado Pago</c:when>
                            <c:otherwise>Manual</c:otherwise>
                        </c:choose>
                    </td>
                    <td>${p.fechaSolicitud}</td>
                    <td><span class="badge bg-warning text-dark">Pendiente</span></td>
                    <td>
                        <form method="post" action="${pageContext.request.contextPath}/admin/pagos/cancelar">
                            <%@ include file="csrf.jspf" %>
                            <input type="hidden" name="pagoId" value="${p.id}">
                            <button type="submit" class="btn btn-sm btn-outline-secondary"
                                    onclick="return confirm('Cancelar esta solicitud? Podras iniciar un pago nuevo.');">
                                Cancelar
                            </button>
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
