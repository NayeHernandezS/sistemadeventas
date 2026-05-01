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
<form action="${pageContext.request.contextPath}/usuarios/form" method="post">
    <div class="row mb-2">
        <label for="username" class="col-form-label col-sm-2">Username</label>
        <div class="col-sm-4">
            <input type="text" name="username" id="username" value="${usuario.username}" class="form-control">
        </div>
        <c:if test="${errores != null && errores.containsKey('username')}">
            <div style="color:red;">${errores.username}</div>
        </c:if>
    </div>

    <div class="row mb-2">
        <label for="password" class="col-form-label col-sm-2">Password</label>
        <div class="col-sm-4">
            <input type="password" name="password" id="password" class="form-control">
        </div>
        <c:if test="${errores != null && not empty errores.password}">
            <div style="color:red;">${errores.password}</div>
        </c:if>
    </div>

    <div class="row mb-2">
        <label for="email" class="col-form-label col-sm-2">Email</label>
        <div class="col-sm-4">
            <input type="text" name="email" id="email" value="${usuario.email}" class="form-control">
        </div>
        <c:if test="${errores != null && not empty errores.email}">
            <div style="color:red;">${errores.email}</div>
        </c:if>
    </div>

    <div class="row mb-2">
        <label for="rol" class="col-form-label col-sm-2">Rol</label>
        <div class="col-sm-4">
            <select name="rol" id="rol" class="form-select">
                <option value="">-- seleccionar --</option>
                <option value="ADMIN" ${usuario.rol == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                <option value="VENDEDOR" ${usuario.rol == 'VENDEDOR' ? 'selected' : ''}>VENDEDOR</option>
            </select>
        </div>
        <c:if test="${errores != null && not empty errores.rol}">
            <div style="color:red;">${errores.rol}</div>
        </c:if>
    </div>

    <div class="row mb-2">
        <div>
            <input class="btn btn-primary" type="submit" value="${usuario.id!=null && usuario.id>0? "Editar": "Crear"}">
        </div>
    </div>
    <input type="hidden" name="id" value="${usuario.id!=null && usuario.id>0? usuario.id: 0}">
</form>
</div>
</body>
</html>