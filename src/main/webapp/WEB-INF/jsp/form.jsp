<%@page contentType="text/html" pageEncoding="UTF-8" import="java.time.format.*"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Formulario productos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container">
<h1>Formulario productos</h1>
<form action="${pageContext.request.contextPath}/productos/form" method="post">
    <%@ include file="csrf.jspf" %>
    <div>
        <label for="nombre">Nombre</label>
        <div>
            <input type="text" name="nombre" id="nombre" value="${producto.nombre}">
        </div>
        <c:if test="${errores != null && errores.containsKey('nombre')}">
             <div style="color:red;">${errores.nombre}</div>
        </c:if>
    </div>

    <div>
        <label for="precio">Precio</label>
        <div>
            <input type="number" name="precio" id="precio" value="${producto.precio > 0? producto.precio: ""}">
        </div>
        <c:if test="${errores != null && not empty errores.precio}">
                     <div style="color:red;">${errores.precio}</div>
                </c:if>
    </div>

    <div>
        <label for="existencias">Existencias</label>
        <div>
            <input type="number" name="existencias" id="existencias" min="0"
                   value="${producto.existencias >= 0 ? producto.existencias : 0}">
        </div>
        <c:if test="${errores != null && not empty errores.existencias}">
            <div style="color:red;">${errores.existencias}</div>
        </c:if>
    </div>

    <div>
        <label for="sku">Sku</label>
        <div>
            <input type="text" name="sku" id="sku" value="${producto.sku}">
        </div>
        <c:if test="${errores != null && not empty errores.sku}">
             <div style="color:red;">${errores.sku}</div>
        </c:if>
    </div>

    <div>
        <label for="fecha_registro">Fecha Registro</label>
        <div>
            <input type="date" name="fecha_registro" id="fecha_registro" value="${producto.fechaRegistro != null? producto.fechaRegistro.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")): ""}">
        </div>
        <c:if test="${errores != null && not empty errores.fecha_registro}">
             <div style="color:red;">${errores.fecha_registro}</div>
        </c:if>
    </div>

    <div>
        <label for="categoria">Categoria</label>
        <div>
            <select name="categoria" id="categoria">
                <option value="">--- seleccionar ---</option>
                <c:forEach items="${categorias}" var="c">
                <option value="${c.id}" ${c.id.equals(producto.categoria.id)? "selected": ""}>${c.nombre}</option>
                </c:forEach>
            </select>
        </div>
        <c:if test="${errores != null && not empty errores.categoria}">
              <div style="color:red;">${errores.categoria}</div>
        </c:if>
    </div>

    <div><input type="submit" value="${producto.id!=null && producto.id>0? "Editar": "Crear"}"></div>
    <input type="hidden" name="id" value="${producto.id}">
</form>
</div>
</body>
</html>