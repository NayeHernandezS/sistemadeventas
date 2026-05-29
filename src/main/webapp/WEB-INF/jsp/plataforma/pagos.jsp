<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Pagos - Plataforma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav.jspf" %>
<div class="container pb-5">
    <h1 class="h3 mb-3">Pagos de suscripcion (todos los clientes)</h1>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <c:if test="${empty pagosPendientes}">
        <p class="text-muted">No hay pagos pendientes de confirmacion.</p>
    </c:if>

    <c:if test="${not empty pagosPendientes}">
        <div class="table-responsive">
            <table class="table table-striped align-middle">
                <thead>
                <tr>
                    <th>Cliente</th>
                    <th>Plan</th>
                    <th>Meses</th>
                    <th>Monto</th>
                    <th>Solicitado</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${pagosPendientes}" var="p">
                    <tr>
                        <td><strong>${p.username}</strong></td>
                        <td>${p.planCodigo}</td>
                        <td>${p.meses}</td>
                        <td>$<fmt:formatNumber value="${p.monto}" minFractionDigits="2"/></td>
                        <td>${p.fechaSolicitud}</td>
                        <td>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/plataforma/pagos/confirmar"
                                  class="d-inline">
                                <%@ include file="../csrf.jspf" %>
                                <input type="hidden" name="pagoId" value="${p.id}">
                                <button type="submit" class="btn btn-sm btn-success">Confirmar pago</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>
</div>
</body>
</html>
