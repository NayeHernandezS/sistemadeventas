<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Receta: ${platillo.nombre}</title>
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="mb-2">Receta: ${platillo.nombre}</h1>
    <p class="text-muted mb-3">
        Categoria ${platillo.categoria.nombre} · Precio de venta <strong>$${platillo.precio}</strong>
    </p>

    <c:if test="${not empty sessionScope.mensajeError}">
        <div class="alert alert-danger">${sessionScope.mensajeError}</div>
        <c:remove var="mensajeError" scope="session"/>
    </c:if>

    <c:if test="${empty insumos}">
        <div class="alert alert-warning">
            Primero registra insumos en categorias <strong>Insumos</strong> o <strong>Desechables</strong>
            con su precio de compra en el inventario.
        </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/recetas/guardar" id="formReceta">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <input type="hidden" name="productoId" value="${platillo.id}"/>

        <div class="table-responsive mb-3">
            <table class="table align-middle" id="tablaLineas">
                <thead>
                <tr>
                    <th style="min-width:220px">Insumo</th>
                    <th style="width:120px">Cantidad</th>
                    <th style="width:110px">Unidad</th>
                    <th style="width:60px"></th>
                </tr>
                </thead>
                <tbody id="cuerpoLineas">
                <c:choose>
                    <c:when test="${not empty lineas}">
                        <c:forEach items="${lineas}" var="l">
                            <tr class="linea-receta">
                                <td>
                                    <select name="insumoId" class="form-select" required>
                                        <option value="">— Elegir —</option>
                                        <c:forEach items="${insumos}" var="ins">
                                            <option value="${ins.id}" ${ins.id eq l.insumoProductoId ? 'selected' : ''}>
                                                ${ins.nombre}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </td>
                                <td>
                                    <input type="number" name="cantidad" class="form-control" min="0.0001" step="0.0001"
                                           value="${l.cantidad}" required>
                                </td>
                                <td>
                                    <select name="unidad" class="form-select">
                                        <c:set var="unidades" value="pza,kg,g,l,ml"/>
                                        <c:forTokens items="${unidades}" delims="," var="u">
                                            <option value="${u}" ${u eq l.unidad ? 'selected' : ''}>${u}</option>
                                        </c:forTokens>
                                    </select>
                                </td>
                                <td>
                                    <button type="button" class="btn btn-sm btn-outline-danger btn-quitar" title="Quitar">
                                        <i class="bi bi-trash"></i>
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr class="linea-receta">
                            <td>
                                <select name="insumoId" class="form-select" required>
                                    <option value="">— Elegir —</option>
                                    <c:forEach items="${insumos}" var="ins">
                                        <option value="${ins.id}">${ins.nombre}</option>
                                    </c:forEach>
                                </select>
                            </td>
                            <td>
                                <input type="number" name="cantidad" class="form-control" min="0.0001" step="0.0001" value="1" required>
                            </td>
                            <td>
                                <select name="unidad" class="form-select">
                                    <option value="pza" selected>pza</option>
                                    <option value="kg">kg</option>
                                    <option value="g">g</option>
                                    <option value="l">l</option>
                                    <option value="ml">ml</option>
                                </select>
                            </td>
                            <td>
                                <button type="button" class="btn btn-sm btn-outline-danger btn-quitar" title="Quitar">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <div class="mb-4">
            <button type="button" class="btn btn-outline-primary" id="btnAgregarLinea" ${empty insumos ? 'disabled' : ''}>
                <i class="bi bi-plus-lg"></i> Agregar ingrediente
            </button>
        </div>

        <div class="d-flex flex-wrap gap-2">
            <button type="submit" class="btn btn-success" ${empty insumos ? 'disabled' : ''}>Guardar receta</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/recetas">Cancelar</a>
        </div>
    </form>
</div>

<template id="tplLinea">
    <tr class="linea-receta">
        <td>
            <select name="insumoId" class="form-select" required>
                <option value="">— Elegir —</option>
                <c:forEach items="${insumos}" var="ins">
                    <option value="${ins.id}">${ins.nombre}</option>
                </c:forEach>
            </select>
        </td>
        <td>
            <input type="number" name="cantidad" class="form-control" min="0.0001" step="0.0001" value="1" required>
        </td>
        <td>
            <select name="unidad" class="form-select">
                <option value="pza" selected>pza</option>
                <option value="kg">kg</option>
                <option value="g">g</option>
                <option value="l">l</option>
                <option value="ml">ml</option>
            </select>
        </td>
        <td>
            <button type="button" class="btn btn-sm btn-outline-danger btn-quitar" title="Quitar">
                <i class="bi bi-trash"></i>
            </button>
        </td>
    </tr>
</template>

<script>
(function () {
    const cuerpo = document.getElementById('cuerpoLineas');
    const tpl = document.getElementById('tplLinea');

    document.getElementById('btnAgregarLinea')?.addEventListener('click', function () {
        cuerpo.appendChild(tpl.content.cloneNode(true));
    });

    cuerpo.addEventListener('click', function (e) {
        const btn = e.target.closest('.btn-quitar');
        if (!btn) return;
        const filas = cuerpo.querySelectorAll('.linea-receta');
        if (filas.length <= 1) {
            alert('La receta debe tener al menos un ingrediente.');
            return;
        }
        btn.closest('tr').remove();
    });
})();
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
