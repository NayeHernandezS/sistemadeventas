<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
    <title>Listado de productos</title>
</head>
<body>
<div class="container">
<h1>Listado de productos</h1>
<c:if test="${username.present}">
   <div class="alert alert-info">Hola ${username.get()}, bienvenido!</div>
   <a class="btn btn-primary" href="${pageContext.request.contextPath}/productos/form">crear [+]</a></p>
</c:if>
<table class="table table-hover table-striped">
    <tr>
        <th>id</th>
        <th>nombre</th>
        <th>tipo</th>
        <c:if test="${username.present}">
        <th>precio</th>
        <th>editar</th>
        <th>eliminar</th>
        </c:if>
    </tr>
    <c:forEach items="${productos}" var="p">
    <tr>
        <td>${p.id}</td>
        <td>${p.nombre}</td>
        <td>${p.categoria.nombre}</td>
        <c:if test="${username.present}">
        <td>${p.precio}</td>
        <td><a class="btn btn-sm btn-success" href="${pageContext.request.contextPath}/productos/form?id=${p.id}">editar</a></td>
        <td><a class="btn btn-sm btn-danger" onclick="return confirm('esta seguro que desea eliminar?');"
        href="${pageContext.request.contextPath}/productos/eliminar?id=${p.id}">eliminar</a></td>
        </c:if>
    </tr>
    </c:forEach>
</table>
<p>${applicationScope.mensaje}</p>
<p>${requestScope.mensaje}</p>
</div>
</body>
</html>