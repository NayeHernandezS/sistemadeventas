<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Inventario</title>
</head>
<body>
<div class="container py-4">
    <h1 class="mb-3">
        <c:choose>
            <c:when test="${esAdmin}">Inventario</c:when>
            <c:otherwise>Consulta de inventario</c:otherwise>
        </c:choose>
    </h1>

    <c:if test="${soloLectura}">
        <div class="alert alert-info">
            Vista de solo lectura: puedes ver nombre, existencias y precio. No puedes agregar, editar ni eliminar productos.
        </div>
    </c:if>

    <c:if test="${username.present}">
        <div class="mb-3">
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/">Volver</a>
            <c:if test="${esAdmin}">
                <a class="btn btn-primary ms-2" href="${pageContext.request.contextPath}/productos/form">Crear producto [+]</a>
            </c:if>
        </div>
    </c:if>

    <table class="table table-hover table-striped">
        <thead>
        <tr>
            <c:if test="${esAdmin}">
                <th>ID</th>
            </c:if>
            <th>Nombre</th>
            <c:if test="${esAdmin}">
                <th>Categoria</th>
            </c:if>
            <th>Existencias</th>
            <th>Precio</th>
            <c:if test="${esAdmin}">
                <th>Editar</th>
                <th>Eliminar</th>
            </c:if>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${productos}" var="p">
            <tr>
                <c:if test="${esAdmin}">
                    <td>${p.id}</td>
                </c:if>
                <td>${p.nombre}</td>
                <c:if test="${esAdmin}">
                    <td>${p.categoria.nombre}</td>
                </c:if>
                <td>${p.existencias}</td>
                <td>$${p.precio}</td>
                <c:if test="${esAdmin}">
                    <td>
                        <a class="btn btn-sm btn-success"
                           href="${pageContext.request.contextPath}/productos/form?id=${p.id}">Editar</a>
                    </td>
                    <td>
                        <a class="btn btn-sm btn-danger"
                           onclick="return confirm('¿Eliminar este producto?');"
                           href="${pageContext.request.contextPath}/productos/eliminar?id=${p.id}">Eliminar</a>
                    </td>
                </c:if>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
