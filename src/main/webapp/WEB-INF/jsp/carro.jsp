<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Carro de Compras</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container">
<h1>Carro de Compras</h1>
<c:if test="${not empty sessionScope.mensajeError}">
    <div class="alert alert-danger">${sessionScope.mensajeError}</div>
    <c:remove var="mensajeError" scope="session"/>
</c:if>
<c:choose>
<c:when test="${empty carro.items}">
<div class="alert alert-warning">Lo sentimos, no hay productos en el carro de compras.</div>
</c:when>
<c:otherwise>
<form name="formcarro" action="${pageContext.request.contextPath}/carro/actualizar" method="post">
<%@ include file="csrf.jspf" %>
<table class="table table-hover table-striped">
    <thead>
    <tr>
        <th>Id</th>
        <th>Nombre</th>
        <th>Precio</th>
        <th>Existencias</th>
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
        <td>${item.producto.existencias}</td>
        <td><input type="number" min="1" size="4" name="cant_${item.producto.id}" value="${item.cantidad}" /></td>
        <td>${item.importe}</td>
        <td><input type="checkbox" value="${item.producto.id}" name="deleteProductos" /></td>
    </tr>
    </c:forEach>
    </tbody>
    <tfoot>
    <tr>
        <td colspan="5" class="text-end"><strong>Total:</strong></td>
        <td colspan="2"><strong>${carro.total}</strong></td>
    </tr>
    </tfoot>
</table>
<a class="btn btn-primary" href="javascript:document.formcarro.submit();">Actualizar</a>
</form>
<form class="mt-3" action="${pageContext.request.contextPath}/carro/finalizar" method="post">
<%@ include file="csrf.jspf" %>
    <div class="card mb-3">
        <div class="card-header">Facturación (opcional)</div>
        <div class="card-body">
            <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" name="requiereFactura" value="1" id="requiereFactura">
                <label class="form-check-label" for="requiereFactura">El cliente requiere factura</label>
            </div>
            <p class="small text-muted">Si marca esta opción, complete RFC y razón social. El resto es opcional.</p>
            <c:if test="${cfdiTimbradoDisponible}">
                <div class="alert alert-info py-2 small mb-3">
                    Timbrado CFDI activo: el admin debe tener RFC, régimen fiscal y código postal en
                    <a href="${pageContext.request.contextPath}/perfil">Mi perfil</a>.
                    Indique también el código postal del receptor.
                </div>
            </c:if>
            <c:if test="${not empty facturaDefaults}">
                <p class="small text-info">Datos precargados desde Mi perfil; puede editarlos para este ticket.</p>
            </c:if>
            <div class="row g-2">
                <div class="col-md-4">
                    <label class="form-label" for="rfcFactura">RFC</label>
                    <input class="form-control" type="text" name="rfcFactura" id="rfcFactura" maxlength="13"
                           placeholder="12 o 13 caracteres" value="${facturaDefaults.rfc}">
                </div>
                <div class="col-md-8">
                    <label class="form-label" for="razonSocial">Razón social o nombre</label>
                    <input class="form-control" type="text" name="razonSocial" id="razonSocial"
                           placeholder="Nombre fiscal" value="${facturaDefaults.razonSocial}">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="emailFactura">Correo (opcional)</label>
                    <input class="form-control" type="email" name="emailFactura" id="emailFactura"
                           value="${facturaDefaults.email}">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="usoCfdi">Uso CFDI (opcional)</label>
                    <input class="form-control" type="text" name="usoCfdi" id="usoCfdi" maxlength="10"
                           placeholder="ej. G03" value="${facturaDefaults.usoCfdi}">
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="codigoPostalReceptor">C.P. receptor<c:if test="${cfdiTimbradoDisponible}"> *</c:if></label>
                    <input class="form-control" type="text" name="codigoPostalReceptor" id="codigoPostalReceptor"
                           maxlength="5" placeholder="5 digitos"
                           value="${facturaDefaults.codigoPostal}">
                </div>
                <div class="col-12">
                    <label class="form-label" for="direccionFactura">Dirección (opcional)</label>
                    <input class="form-control" type="text" name="direccionFactura" id="direccionFactura"
                           value="${facturaDefaults.direccion}">
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
    <a class="btn btn-sm btn-secondary" href="${pageContext.request.contextPath}/">Volver</a>
    <a class="btn btn-sm btn-success" href="${pageContext.request.contextPath}/productos">Seguir vendiendo</a>
    <a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/tickets">Ver tickets</a>
</div>
</div>
</body>
</html>
