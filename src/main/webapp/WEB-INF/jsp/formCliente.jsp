<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Cliente</title>
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="mb-3">
        <c:choose>
            <c:when test="${cliente.id != null && cliente.id > 0}">Editar cliente</c:when>
            <c:otherwise>Nuevo cliente</c:otherwise>
        </c:choose>
    </h1>

    <c:if test="${errores != null && not empty errores.general}">
        <div class="alert alert-danger">${errores.general}</div>
    </c:if>

    <p class="text-muted small">
        Los datos fiscales (RFC, razon social, codigo postal, uso CFDI) se podran usar al facturar en el carrito.
        El RFC es opcional; si lo capturas, debe ser unico en tu cuenta.
    </p>

    <form action="${pageContext.request.contextPath}/clientes/form" method="post">
        <%@ include file="csrf.jspf" %>
        <input type="hidden" name="id" value="${cliente.id != null ? cliente.id : 0}">

        <div class="row mb-3">
            <label for="nombre" class="col-form-label col-sm-2">Nombre *</label>
            <div class="col-sm-6">
                <input type="text" name="nombre" id="nombre" maxlength="200" required
                       value="${cliente.nombre}" class="form-control"
                       placeholder="Nombre de contacto o mostrador">
            </div>
        </div>

        <div class="row mb-3">
            <label for="rfc" class="col-form-label col-sm-2">RFC</label>
            <div class="col-sm-4">
                <input type="text" name="rfc" id="rfc" maxlength="13"
                       value="${cliente.rfc}" class="form-control" placeholder="12 o 13 caracteres">
            </div>
        </div>

        <div class="row mb-3">
            <label for="razon_social" class="col-form-label col-sm-2">Razon social</label>
            <div class="col-sm-6">
                <input type="text" name="razon_social" id="razon_social" maxlength="200"
                       value="${cliente.razonSocial}" class="form-control" placeholder="Nombre fiscal">
            </div>
        </div>

        <div class="row mb-3">
            <label for="email" class="col-form-label col-sm-2">Correo</label>
            <div class="col-sm-6">
                <input type="email" name="email" id="email" maxlength="150"
                       value="${cliente.email}" class="form-control">
            </div>
        </div>

        <div class="row mb-3">
            <label for="codigo_postal" class="col-form-label col-sm-2">C.P.</label>
            <div class="col-sm-3">
                <input type="text" name="codigo_postal" id="codigo_postal" maxlength="5"
                       value="${cliente.codigoPostal}" class="form-control" placeholder="5 digitos">
            </div>
        </div>

        <div class="row mb-3">
            <label for="uso_cfdi" class="col-form-label col-sm-2">Uso CFDI</label>
            <div class="col-sm-3">
                <input type="text" name="uso_cfdi" id="uso_cfdi" maxlength="10"
                       value="${cliente.usoCfdi}" class="form-control" placeholder="ej. G03">
            </div>
        </div>

        <button type="submit" class="btn btn-primary">Guardar</button>
        <a class="btn btn-secondary ms-2" href="${pageContext.request.contextPath}/clientes">Cancelar</a>
    </form>
</div>
</body>
</html>
