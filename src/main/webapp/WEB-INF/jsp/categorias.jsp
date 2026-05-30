<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Categorias</title>
</head>
<body>
<div class="container py-4">
    <h1 class="mb-3">Categorias de productos</h1>

    <c:if test="${not empty sessionScope.mensajeError}">
        <div class="alert alert-danger">${sessionScope.mensajeError}</div>
        <c:remove var="mensajeError" scope="session"/>
    </c:if>

    <div class="mb-3">
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/">Volver</a>
        <a class="btn btn-outline-secondary ms-2" href="${pageContext.request.contextPath}/crudprod">Inventario</a>
        <a class="btn btn-primary ms-2" href="${pageContext.request.contextPath}/categorias/form">Nueva categoria [+]</a>
    </div>

    <c:choose>
        <c:when test="${empty categorias}">
            <p class="text-muted">No hay categorias. Crea una para clasificar tus productos.</p>
        </c:when>
        <c:otherwise>
            <table class="table table-hover table-striped">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Nombre</th>
                    <th>Editar</th>
                    <th>Eliminar</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${categorias}" var="c">
                    <tr>
                        <td>${c.id}</td>
                        <td>${c.nombre}</td>
                        <td>
                            <a class="btn btn-sm btn-success"
                               href="${pageContext.request.contextPath}/categorias/form?id=${c.id}">Editar</a>
                        </td>
                        <td>
                            <a class="btn btn-sm btn-danger"
                               onclick="return confirm('¿Eliminar esta categoria? Solo es posible si no tiene productos.');"
                               href="${pageContext.request.contextPath}/categorias/eliminar?id=${c.id}">Eliminar</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
