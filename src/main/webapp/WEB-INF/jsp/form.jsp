<%@page contentType="text/html" pageEncoding="UTF-8" import="java.time.format.*"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Producto o servicio</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
</head>
<body class="app-con-nav-movil">
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
        <label for="precio" class="form-label">Precio de venta</label>
        <input type="number" name="precio" id="precio" min="1" class="form-control"
               value="${producto.precio > 0 ? producto.precio : ''}">
        <div class="form-text">Precio publico que ven clientes y vendedores.</div>
        <c:if test="${errores != null && not empty errores.precio}">
            <div class="text-danger small">${errores.precio}</div>
        </c:if>
    </div>

    <div class="col-md-4" id="precio-compra-group">
        <label for="precio_compra" class="form-label">Precio de compra</label>
        <input type="number" name="precio_compra" id="precio_compra" min="0" class="form-control"
               value="${producto.precioCompra > 0 ? producto.precioCompra : ''}">
        <div class="form-text">Costo interno; solo visible para el administrador en inventario.</div>
        <c:if test="${errores != null && not empty errores.precio_compra}">
            <div class="text-danger small">${errores.precio_compra}</div>
        </c:if>
    </div>

    <div class="col-md-4" id="porcentaje-ganancia-group">
        <label for="porcentaje_ganancia" class="form-label">Porcentaje de ganancia (%)</label>
        <input type="number" name="porcentaje_ganancia" id="porcentaje_ganancia" min="0" max="999" step="1"
               class="form-control"
               value="${producto.porcentajeGanancia > 0 ? producto.porcentajeGanancia : ''}">
        <div class="form-text">Ganancia deseada sobre el precio de compra. Ej: 30 = 30% de utilidad.</div>
        <c:if test="${errores != null && not empty errores.porcentaje_ganancia}">
            <div class="text-danger small">${errores.porcentaje_ganancia}</div>
        </c:if>
    </div>

    <div class="col-md-8" id="calcular-precio-group">
        <div class="form-check mt-4">
            <input class="form-check-input" type="checkbox" name="calcular_precio_venta" id="calcular_precio_venta" checked>
            <label class="form-check-label" for="calcular_precio_venta">
                Calcular precio de venta automaticamente (compra + % ganancia)
            </label>
        </div>
        <div class="d-flex flex-wrap align-items-center gap-2 mt-2">
            <button type="button" class="btn btn-sm btn-outline-primary" id="btn-aplicar-precio">
                Aplicar precio sugerido ahora
            </button>
            <small class="text-muted" id="precio-sugerido-texto"></small>
        </div>
    </div>

    <div class="col-md-3" id="existencias-group">
        <label for="existencias_cantidad" class="form-label">Existencias</label>
        <input type="number" name="existencias_cantidad" id="existencias_cantidad" min="0" step="0.001"
               class="form-control" value="${producto.existenciasCantidadDisplay}">
        <c:if test="${errores != null && not empty errores.existencias}">
            <div class="text-danger small">${errores.existencias}</div>
        </c:if>
    </div>

    <div class="col-md-3" id="unidad-medida-group">
        <label for="unidad_medida" class="form-label">Unidad</label>
        <select name="unidad_medida" id="unidad_medida" class="form-select">
            <c:forEach items="${unidadesMedida}" var="u">
                <option value="${u}" ${producto.unidadMedida eq u ? 'selected' : ''}>${u}</option>
            </c:forEach>
        </select>
        <div class="form-text" id="unidad-medida-ayuda">
            <c:if test="${esRestaurante}">En insumos usa kg, g o pza segun compres en el mercado.</c:if>
        </div>
    </div>

    <div class="col-md-4" id="sku-group">
        <label for="sku" class="form-label">SKU / codigo de barras
            <span id="sku-opcional" class="text-muted small">(opcional en servicios)</span></label>
        <input type="text" name="sku" id="sku" class="form-control" maxlength="13"
               inputmode="numeric" value="${producto.sku}"
               placeholder="Ej. 7501234567890">
        <div class="form-text">Hasta 13 caracteres (EAN-13). Se usa para escanear en caja.</div>
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
        <c:choose>
            <c:when test="${producto.esServicio}">
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/productos/servicios">Cancelar</a>
            </c:when>
            <c:otherwise>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/crudprod">Cancelar</a>
            </c:otherwise>
        </c:choose>
    </div>
    <input type="hidden" name="id" value="${producto.id}">
</form>
</div>
<script>
(function () {
    var tipo = document.getElementById('tipo_item');
    var existenciasGroup = document.getElementById('existencias-group');
    var precioCompraGroup = document.getElementById('precio-compra-group');
    var porcentajeGananciaGroup = document.getElementById('porcentaje-ganancia-group');
    var calcularPrecioGroup = document.getElementById('calcular-precio-group');
    var existenciasCantidad = document.getElementById('existencias_cantidad');
    var unidadMedidaGroup = document.getElementById('unidad-medida-group');
    var precio = document.getElementById('precio');
    var precioCompra = document.getElementById('precio_compra');
    var porcentajeGanancia = document.getElementById('porcentaje_ganancia');
    var calcularPrecioVenta = document.getElementById('calcular_precio_venta');
    var precioSugeridoTexto = document.getElementById('precio-sugerido-texto');
    var btnAplicarPrecio = document.getElementById('btn-aplicar-precio');
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

    function calcularPrecioSugerido() {
        var compra = parseInt(precioCompra.value, 10) || 0;
        var pct = parseInt(porcentajeGanancia.value, 10) || 0;
        if (compra <= 0 || pct <= 0) {
            return 0;
        }
        return Math.round(compra * (1 + pct / 100));
    }

    function actualizarPrecioSugerido() {
        var sugerido = calcularPrecioSugerido();
        if (sugerido > 0) {
            precioSugeridoTexto.textContent = 'Precio sugerido: $' + sugerido;
        } else {
            precioSugeridoTexto.textContent = 'Indica precio de compra y porcentaje para calcular.';
        }
    }

    function aplicarPrecioSugerido() {
        var sugerido = calcularPrecioSugerido();
        if (sugerido > 0) {
            precio.value = String(sugerido);
        }
    }

    function actualizarTipoItem() {
        var esServicio = tipo.value === 'SERVICIO';
        existenciasGroup.style.display = esServicio ? 'none' : '';
        unidadMedidaGroup.style.display = esServicio ? 'none' : '';
        precioCompraGroup.style.display = esServicio ? 'none' : '';
        porcentajeGananciaGroup.style.display = esServicio ? 'none' : '';
        calcularPrecioGroup.style.display = esServicio ? 'none' : '';
        tipoServicioGroup.style.display = esServicio ? '' : 'none';
        if (esServicio) {
            existenciasCantidad.value = '0';
        } else {
            actualizarPrecioSugerido();
        }
    }

    function recalcularSiActivo() {
        actualizarPrecioSugerido();
        if (calcularPrecioVenta.checked) {
            aplicarPrecioSugerido();
        }
    }

    tipo.addEventListener('change', actualizarTipoItem);
    tipoServicioSugerido.addEventListener('change', aplicarSugerenciaServicio);
    precioCompra.addEventListener('input', recalcularSiActivo);
    porcentajeGanancia.addEventListener('input', recalcularSiActivo);
    calcularPrecioVenta.addEventListener('change', recalcularSiActivo);
    btnAplicarPrecio.addEventListener('click', aplicarPrecioSugerido);
    actualizarTipoItem();
})();
</script>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
