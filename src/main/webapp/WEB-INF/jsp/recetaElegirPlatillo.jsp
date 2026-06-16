<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Elegir platillo</title>
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="mb-3">Elegir platillo</h1>
    <p class="text-muted">Selecciona el platillo para armar o editar su receta.</p>

    <a class="btn btn-secondary mb-3" href="${pageContext.request.contextPath}/recetas">Volver</a>

    <c:choose>
        <c:when test="${empty platillosDisponibles}">
            <div class="alert alert-info">No hay platillos disponibles. Crea productos en Comida, Bebidas o Postres.</div>
        </c:when>
        <c:otherwise>
            <div class="list-group">
                <c:forEach items="${platillosDisponibles}" var="pl">
                    <a class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
                       href="${pageContext.request.contextPath}/recetas/form?productoId=${pl.id}">
                        <span>${pl.nombre} <small class="text-muted">(${pl.categoria.nombre})</small></span>
                        <span class="badge text-bg-primary">$${pl.precio}</span>
                    </a>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
