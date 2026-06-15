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
    <title>Categoria</title>
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="mb-3">
        <c:choose>
            <c:when test="${categoria.id != null && categoria.id > 0}">Editar categoria</c:when>
            <c:otherwise>Nueva categoria</c:otherwise>
        </c:choose>
    </h1>

    <form action="${pageContext.request.contextPath}/categorias/form" method="post">
        <%@ include file="csrf.jspf" %>
        <input type="hidden" name="id" value="${categoria.id != null ? categoria.id : 0}">

        <div class="row mb-3">
            <label for="nombre" class="col-form-label col-sm-2">Nombre</label>
            <div class="col-sm-6">
                <input type="text" name="nombre" id="nombre" maxlength="100"
                       value="${categoria.nombre}" class="form-control" required>
            </div>
            <c:if test="${errores != null && not empty errores.nombre}">
                <div class="col-sm-4 text-danger">${errores.nombre}</div>
            </c:if>
        </div>

        <button type="submit" class="btn btn-primary">Guardar</button>
        <a class="btn btn-secondary ms-2" href="${pageContext.request.contextPath}/categorias">Cancelar</a>
    </form>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
