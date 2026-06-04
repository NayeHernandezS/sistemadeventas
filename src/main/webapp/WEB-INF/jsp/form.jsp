<%@page contentType="text/html" pageEncoding="UTF-8" import="java.time.format.*"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Producto o servicio</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
<h1 class="h3 mb-3">Producto o servicio</h1>
<p class="text-muted small mb-0">
    Rubro de tu negocio: <strong>${tipoNegocioEtiqueta}</strong>.
    Los servicios se cobran en caja sin descontar inventario.
</p>

<form action="${pageContext.request.contextPath}/productos/form" method="post" class="row g-3 mt-2">
    <%@ include file="csrf.jspf" %>

    <div class="col-md-4">
        <label for="tipo_item" class="form-label">Tipo de item</label>
        <select name="tipo_item" id="tipo_item" class="form-select">
            <c:forEach items="${tiposItem}" var="tipo">
                <option value="${tipo.name}"
                        ${producto.tipoItem.name == tipo.name ? 'selected' : ''}>${tipo.etiqueta}</option>
            </c:forEach>
        </select>
    </div>

    <div class="col-md-8" id="tipo-servicio-group" style="display:none">
        <label for="tipo_servicio_sugerido" class="form-label">
            <i class="bi bi-scissors"></i> Tipo de servicio (segun tu rubro)
        </label>
        <select id="tipo_servicio_sugerido" class="form-select">
            <option value="">-- Elige una plantilla o escribe el nombre --</option>
            <c:forEach items="${sugerenciasServicio}" var="sug">
                <option value="${sug.nombre}" data-categoria="${sug.categoria}">${sug.nombre} (${sug.categoria})</option>
            </c:forEach>
        </select>
        <div class="form-text">Al elegir una opcion se llenan nombre y categoria. Puedes editarlos despues.</div>
    </div>

    <div class="col-md-8" id="nombre-group">
        <label for="nombre" class="form-label">Nombre</label>
        <input type="text" name="nombre" id="nombre" class="form-control" value="${producto.nombre}"
               placeholder="Ej. Corte caballero, Reparacion de laptop...">
        <c:if test="${errores != null && errores.containsKey('nombre')}">
             <div class="text-danger small">${errores.nombre}</div>
        </c:if>
    </div>

    <div class="col-md-4">
        <label for="precio" class="form-label">Precio</label>
        <input type="number" name="precio" id="precio" class="form-control"
               value="${producto.precio > 0 ? producto.precio : ''}">
        <c:if test="${errores != null && not empty errores.precio}">
            <div class="text-danger small">${errores.precio}</div>
        </c:if>
    </div>

    <div class="col-md-4" id="existencias-group">
        <label for="existencias" class="form-label">Existencias</label>
        <input type="number" name="existencias" id="existencias" min="0" class="form-control"
               value="${producto.existencias >= 0 ? producto.existencias : 0}">
        <c:if test="${errores != null && not empty errores.existencias}">
            <div class="text-danger small">${errores.existencias}</div>
        </c:if>
    </div>

    <div class="col-md-4" id="sku-group">
        <label for="sku" class="form-label">SKU <span id="sku-opcional" class="text-muted small">(opcional en servicios)</span></label>
        <input type="text" name="sku" id="sku" class="form-control" value="${producto.sku}">
        <c:if test="${errores != null && not empty errores.sku}">
             <div class="text-danger small">${errores.sku}</div>
        </c:if>
    </div>

    <div class="col-md-4">
        <label for="fecha_registro" class="form-label">Fecha registro</label>
        <input type="date" name="fecha_registro" id="fecha_registro" class="form-control"
               value="${producto.fechaRegistro != null ? producto.fechaRegistro.format(DateTimeFormatter.ofPattern('yyyy-MM-dd')) : ''}">
        <c:if test="${errores != null && not empty errores.fecha_registro}">
             <div class="text-danger small">${errores.fecha_registro}</div>
        </c:if>
    </div>

    <div class="col-md-4">
        <label for="categoria" class="form-label">Categoria</label>
        <select name="categoria" id="categoria" class="form-select">
            <option value="">--- seleccionar ---</option>
            <c:forEach items="${categorias}" var="c">
            <option value="${c.id}" data-nombre="${c.nombre}"
                    ${c.id.equals(producto.categoria.id) ? 'selected' : ''}>${c.nombre}</option>
            </c:forEach>
        </select>
        <c:if test="${errores != null && not empty errores.categoria}">
              <div class="text-danger small">${errores.categoria}</div>
        </c:if>
    </div>

    <div class="col-12">
        <button type="submit" class="btn btn-primary">${producto.id != null && producto.id > 0 ? 'Guardar cambios' : 'Crear'}</button>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/crudprod">Cancelar</a>
    </div>
    <input type="hidden" name="id" value="${producto.id}">
</form>
</div>
<script>
(function () {
    var tipo = document.getElementById('tipo_item');
    var existenciasGroup = document.getElementById('existencias-group');
    var existencias = document.getElementById('existencias');
    var tipoServicioGroup = document.getElementById('tipo-servicio-group');
    var tipoServicioSugerido = document.getElementById('tipo_servicio_sugerido');
    var nombre = document.getElementById('nombre');
    var categoria = document.getElementById('categoria');

    function seleccionarCategoriaPorNombre(nombreCat) {
        if (!nombreCat) {
            return;
        }
        var objetivo = nombreCat.trim().toLowerCase();
        for (var i = 0; i < categoria.options.length; i++) {
            var opt = categoria.options[i];
            var texto = (opt.getAttribute('data-nombre') || opt.textContent || '').trim().toLowerCase();
            if (texto === objetivo) {
                categoria.selectedIndex = i;
                return;
            }
        }
    }

    function aplicarSugerenciaServicio() {
        var opt = tipoServicioSugerido.options[tipoServicioSugerido.selectedIndex];
        if (!opt || !opt.value) {
            return;
        }
        nombre.value = opt.value;
        seleccionarCategoriaPorNombre(opt.getAttribute('data-categoria'));
    }

    function actualizarTipoItem() {
        var esServicio = tipo.value === 'SERVICIO';
        existenciasGroup.style.display = esServicio ? 'none' : '';
        tipoServicioGroup.style.display = esServicio ? '' : 'none';
        if (esServicio) {
            existencias.value = '0';
        }
    }

    tipo.addEventListener('change', actualizarTipoItem);
    tipoServicioSugerido.addEventListener('change', aplicarSugerenciaServicio);
    actualizarTipoItem();
})();
</script>
</body>
</html>
