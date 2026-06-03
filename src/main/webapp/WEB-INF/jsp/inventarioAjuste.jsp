<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Ajuste de inventario</title>
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="mb-3">Ajuste de inventario</h1>

    <p class="text-muted">
        Producto: <strong>${producto.nombre}</strong> · Existencias actuales: <strong>${producto.existencias}</strong>
    </p>

    <c:if test="${errores != null && not empty errores.general}">
        <div class="alert alert-danger">${errores.general}</div>
    </c:if>
    <c:if test="${errores != null && not empty errores.tipo}">
        <div class="alert alert-danger">${errores.tipo}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/inventario/ajuste" method="post" class="card card-body">
        <%@ include file="csrf.jspf" %>
        <input type="hidden" name="productoId" value="${producto.id}">

        <div class="mb-3">
            <label class="form-label" for="tipo">Tipo de movimiento</label>
            <select class="form-select" name="tipo" id="tipo" required>
                <option value="">— Seleccione —</option>
                <c:forEach items="${tiposMovimiento}" var="t">
                    <option value="${t.name}" ${tipoSeleccionado eq t.name ? 'selected' : ''}>${t.etiqueta}</option>
                </c:forEach>
            </select>
        </div>

        <div class="mb-3">
            <label class="form-label" for="cantidad" id="labelCantidad">Cantidad</label>
            <input type="number" class="form-control" name="cantidad" id="cantidad" min="0" required
                   value="${cantidadIngresada}">
            <div class="form-text" id="ayudaCantidad">
                <strong>Entrada:</strong> unidades a sumar.
                <strong>Salida:</strong> unidades a restar (merma, uso interno).
                <strong>Ajuste:</strong> cantidad final deseada en inventario.
            </div>
        </div>

        <div class="mb-3">
            <label class="form-label" for="motivo">Motivo (opcional)</label>
            <input type="text" class="form-control" name="motivo" id="motivo" maxlength="255"
                   placeholder="Ej. compra a proveedor, conteo fisico" value="${motivoIngresado}">
        </div>

        <button type="submit" class="btn btn-primary">Registrar movimiento</button>
        <a class="btn btn-secondary ms-2" href="${pageContext.request.contextPath}/crudprod">Cancelar</a>
    </form>
</div>
<script>
    (function () {
        const tipo = document.getElementById('tipo');
        const label = document.getElementById('labelCantidad');
        const ayuda = document.getElementById('ayudaCantidad');
        const textos = {
            ENTRADA: ['Unidades a agregar', 'Suma al stock actual.'],
            SALIDA: ['Unidades a retirar', 'Resta del stock actual. No puede quedar negativo.'],
            AJUSTE: ['Cantidad final en inventario', 'Fija las existencias al numero indicado (conteo, correccion).']
        };
        function actualizar() {
            const t = textos[tipo.value];
            if (t) {
                label.textContent = t[0];
                ayuda.textContent = t[1];
            } else {
                label.textContent = 'Cantidad';
            }
        }
        tipo.addEventListener('change', actualizar);
        actualizar();
    })();
</script>
</body>
</html>
