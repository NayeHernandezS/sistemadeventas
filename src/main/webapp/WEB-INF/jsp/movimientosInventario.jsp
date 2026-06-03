<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Movimientos de inventario</title>
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="mb-3">Movimientos de inventario</h1>
    <p class="text-muted">Ultimos 50 registros de entradas, salidas y ajustes.</p>

    <div class="mb-3">
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/crudprod">Volver al inventario</a>
    </div>

    <c:choose>
        <c:when test="${empty movimientos}">
            <p class="text-muted">Aun no hay movimientos registrados.</p>
        </c:when>
        <c:otherwise>
            <table class="table table-hover table-striped table-sm">
                <thead>
                <tr>
                    <th>Fecha</th>
                    <th>Producto</th>
                    <th>Tipo</th>
                    <th class="text-end">Cant.</th>
                    <th class="text-end">Antes</th>
                    <th class="text-end">Despues</th>
                    <th>Usuario</th>
                    <th>Motivo</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${movimientos}" var="m">
                    <tr>
                        <td>${m.fecha.format(formatoFecha)}</td>
                        <td>${m.nombreProducto}</td>
                        <td>
                            <c:choose>
                                <c:when test="${m.tipo.name eq 'ENTRADA'}">
                                    <span class="badge bg-success">Entrada</span>
                                </c:when>
                                <c:when test="${m.tipo.name eq 'SALIDA'}">
                                    <span class="badge bg-warning text-dark">Salida</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-info text-dark">Ajuste</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end">${m.cantidad}</td>
                        <td class="text-end">${m.existenciasAntes}</td>
                        <td class="text-end"><strong>${m.existenciasDespues}</strong></td>
                        <td>${m.username}</td>
                        <td><c:out value="${empty m.motivo ? '—' : m.motivo}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
