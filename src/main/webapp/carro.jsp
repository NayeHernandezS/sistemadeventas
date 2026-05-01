<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Carro de Compras</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
</head>
<body>
<div class="container">
<h1>Carro de Compras</h1>
<c:choose>
<c:when test="${carro.items.isEmpty()}">
<div class="alert alert-warning">Lo sentimos no hay productos en el carro de compras!</div
</c:when>
<c:otherwise>
<form name="formcarro" action="${pageContext.request.contextPath}/carro/actualizar" method="post">
<table class="table table-hover table-striped">
    <tr>
        <th>id</th>
        <th>nombre</th>
        <th>precio</th>
        <th>cantidad</th>
        <th>total</th>
        <th>borrar</th>
    </tr>
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
    <tr>
        <td colspan="5" style="text-align: right">Total:</td>
        <td>${carro.total}</td>
    </tr>
</table>
<a class="btn btn-primary" href="javascript:document.formcarro.submit();">Actualizar</a>
</form>
<form class="mt-3" action="${pageContext.request.contextPath}/carro/finalizar" method="post">
    <div class="card mb-3">
        <div class="card-header">Facturación (opcional)</div>
        <div class="card-body">
            <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" name="requiereFactura" value="1" id="requiereFactura">
                <label class="form-check-label" for="requiereFactura">El cliente requiere factura</label>
            </div>
            <p class="small text-muted">Si marca esta opción, complete RFC y razón social. El resto es opcional.</p>
            <div class="row g-2">
                <div class="col-md-4">
                    <label class="form-label" for="rfcFactura">RFC</label>
                    <input class="form-control" type="text" name="rfcFactura" id="rfcFactura" maxlength="13" placeholder="12 o 13 caracteres">
                </div>
                <div class="col-md-8">
                    <label class="form-label" for="razonSocial">Razón social o nombre</label>
                    <input class="form-control" type="text" name="razonSocial" id="razonSocial" placeholder="Nombre fiscal">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="emailFactura">Correo (opcional)</label>
                    <input class="form-control" type="email" name="emailFactura" id="emailFactura">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="usoCfdi">Uso CFDI (opcional)</label>
                    <input class="form-control" type="text" name="usoCfdi" id="usoCfdi" maxlength="10" placeholder="ej. G03">
                </div>
                <div class="col-12">
                    <label class="form-label" for="direccionFactura">Dirección (opcional)</label>
                    <input class="form-control" type="text" name="direccionFactura" id="direccionFactura">
                </div>
            </div>
        </div>
    </div>
    <button class="btn btn-success" type="submit">Finalizar venta y generar ticket</button>
</form>
</c:otherwise>
</c:choose>
<c:if test="${not empty sessionScope.mensajeTicket}">
    <div class="alert alert-info mt-3">${sessionScope.mensajeTicket}</div>
    <c:remove var="mensajeTicket" scope="session"/>
</c:if>
<div class="my-2">
<a class="btn btn-sm btn-secondary" href="${pageContext.request.contextPath}/index.jsp">volver</a>
<a class="btn btn-sm btn-success" href="${pageContext.request.contextPath}/productos">seguir vendiendo</a>
<a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/tickets">ver tickets</a>
<div>
</div>
</body>
</html>