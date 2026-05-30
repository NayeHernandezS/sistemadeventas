<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
    <title>Listado de productos</title>
</head>
<body>
<div class="container">
<h1>Listado de productos</h1>
<c:if test="${not empty sessionScope.mensajeError}">
    <div class="alert alert-danger">${sessionScope.mensajeError}</div>
    <c:remove var="mensajeError" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.username}">
   <div class="alert alert-info">Hola ${sessionScope.username}, bienvenido!</div>
</c:if>

<c:choose>
<c:when test="${empty carro.items}">
<div class="alert alert-warning">Lo sentimos, no hay productos en el carro de compras.</div>
</c:when>
<c:otherwise>
<form name="formcarro" action="${pageContext.request.contextPath}/carro/actualizar" method="post">
<%@ include file="csrf.jspf" %>
<table class="table table-hover table-striped mb-3">
    <thead>
    <tr>
        <th>Id</th>
        <th>Nombre</th>
        <th>Precio</th>
        <th>Cantidad</th>
        <th>Importe</th>
        <th>Borrar</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${carro.items}" var="item">
    <tr>
        <td>${item.producto.id}</td>
        <td>${item.producto.nombre}</td>
        <td>${item.producto.precio}</td>
        <td><input type="text" size="4" name="cant_${item.producto.id}" value="${item.cantidad}" /></td>
        <td>${item.importe}</td>
        <td><input type="checkbox" value="${item.producto.id}" name="deleteProductos" /></td>
    </tr>
    </c:forEach>
    </tbody>
    <tfoot>
    <tr>
        <td colspan="4" class="text-end"><strong>Total carro:</strong></td>
        <td colspan="2"><strong>${carro.total}</strong></td>
    </tr>
    </tfoot>
</table>
<a class="btn btn-primary mb-3" href="javascript:document.formcarro.submit();">Actualizar carro</a>
</form>
</c:otherwise>
</c:choose>
<div class="my-2">
<a class="btn btn-sm btn-secondary" href="${pageContext.request.contextPath}/">Volver</a>
<a class="btn btn-sm btn-success" href="${pageContext.request.contextPath}/carro/ver">Ver carro</a>
</div>
<h2 class="h5 mt-3">Catálogo</h2>
<c:if test="${cantidadConAlerta > 0}">
    <div class="alert alert-warning py-2 small">
        ${cantidadConAlerta} producto(s) con stock bajo o agotado (alerta desde ${stockMinimo} unidades).
    </div>
</c:if>
<table class="table table-hover table-striped">
    <thead>
    <tr>
        <th>Id</th>
        <th>Nombre</th>
        <th>Tipo</th>
        <c:if test="${not empty sessionScope.username}">
        <th>Precio</th>
        <th>Existencias</th>
        <th>Agregar</th>
        </c:if>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${productos}" var="p">
    <tr class="${p.existencias == 0 ? 'table-danger' : (p.existencias <= stockMinimo ? 'table-warning' : '')}">
        <td>${p.id}</td>
        <td>${p.nombre}</td>
        <td>${p.categoria.nombre}</td>
        <c:if test="${not empty sessionScope.username}">
        <td>${p.precio}</td>
        <td>
            ${p.existencias}
            <c:if test="${p.existencias == 0}">
                <span class="badge bg-danger ms-1">Agotado</span>
            </c:if>
            <c:if test="${p.existencias > 0 && p.existencias <= stockMinimo}">
                <span class="badge bg-warning text-dark ms-1">Stock bajo</span>
            </c:if>
        </td>
        <td>
            <c:choose>
                <c:when test="${p.existencias > 0}">
                    <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/carro/agregar?id=${p.id}">Agregar al carro</a>
                </c:when>
                <c:otherwise>
                    <span class="text-muted small">Sin stock</span>
                </c:otherwise>
            </c:choose>
        </td>
        </c:if>
    </tr>
    </c:forEach>
    </tbody>
</table>
<p>${applicationScope.mensaje}</p>
<p>${requestScope.mensaje}</p>
</div>
</body>
</html>