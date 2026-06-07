<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Configuracion inicial</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <style>
        .step-indicator .step { width: 2.5rem; height: 2.5rem; border-radius: 50%; display: inline-flex;
            align-items: center; justify-content: center; font-weight: 600; }
        .step-indicator .step.active { background: #5B2A86; color: #fff; }
        .step-indicator .step.done { background: #198754; color: #fff; }
        .step-indicator .step.pending { background: #e9ecef; color: #6c757d; }
    </style>
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="text-center mb-4 step-indicator">
                <span class="step ${paso >= 1 ? (paso == 1 ? 'active' : 'done') : 'pending'}">1</span>
                <span class="mx-2 text-muted">—</span>
                <span class="step ${paso >= 2 ? (paso == 2 ? 'active' : 'done') : 'pending'}">2</span>
                <span class="mx-2 text-muted">—</span>
                <span class="step ${paso >= 3 ? 'active' : 'pending'}">3</span>
            </div>

            <div class="card shadow border-0">
                <div class="card-body p-4 p-md-5">

                    <c:if test="${paso == 1}">
                        <h1 class="h3 mb-3"><i class="bi bi-rocket-takeoff text-primary"></i> Bienvenido, ${tenant}</h1>
                        <p class="text-muted">
                            Tu cuenta esta lista con <strong>1 mes de prueba</strong>.
                            Preparamos tu negocio segun el rubro <strong>${tipoNegocioEtiqueta}</strong>.
                        </p>
                        <ul class="list-group list-group-flush mb-4">
                            <li class="list-group-item">
                                <i class="bi bi-shop text-success"></i>
                                Tipo de negocio: <strong>${tipoNegocioEtiqueta}</strong>
                            </li>
                            <li class="list-group-item">
                                <i class="bi bi-tags text-success"></i>
                                Categorias para productos y servicios: <strong>${categorias.size()}</strong>
                            </li>
                            <c:choose>
                                <c:when test="${importaCatalogoProductos}">
                                    <li class="list-group-item">
                                        <i class="bi bi-box-seam text-success"></i>
                                        Catalogo inicial con productos frecuentes del rubro (precios sugeridos, editables en inventario).
                                    </li>
                                </c:when>
                                <c:otherwise>
                                    <li class="list-group-item">
                                        <i class="bi bi-briefcase text-success"></i>
                                        Servicios sugeridos del rubro listos para cobrar en caja (sin inventario).
                                    </li>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${!importaCatalogoProductos && !predominanServicios}">
                                <li class="list-group-item">
                                    <i class="bi bi-scissors text-info"></i>
                                    Tambien puedes registrar servicios (cortes, reparaciones, consultorias) en el paso 2.
                                </li>
                            </c:if>
                        </ul>
                        <p class="small text-muted mb-0">
                            Dudas: <a href="${pageContext.request.contextPath}/soporte">Soporte</a>.
                        </p>
                        <div class="d-flex flex-wrap gap-2 mt-3">
                            <a href="${pageContext.request.contextPath}/onboarding/producto" class="btn btn-primary">
                                <c:choose>
                                    <c:when test="${predominanServicios}">Siguiente: revisar o agregar servicio</c:when>
                                    <c:otherwise>Siguiente: primer articulo del catalogo</c:otherwise>
                                </c:choose>
                            </a>
                            <form method="post" action="${pageContext.request.contextPath}/onboarding/omitir" class="d-inline">
                                <%@ include file="csrf.jspf" %>
                                <button type="submit" class="btn btn-outline-secondary">Omitir por ahora</button>
                            </form>
                        </div>
                    </c:if>

                    <c:if test="${paso == 2}">
                        <h1 class="h3 mb-3"><i class="bi bi-grid"></i> Tu primer articulo del catalogo</h1>
                        <p class="text-muted">
                            Registra un <strong>producto</strong> (con stock) o un <strong>servicio</strong> (sin inventario) para probar una venta.
                        </p>

                        <c:if test="${not empty errores.general}">
                            <div class="alert alert-danger">${errores.general}</div>
                        </c:if>

                        <form method="post" action="${pageContext.request.contextPath}/onboarding/producto" class="row g-3" id="form-onboarding-articulo">
                            <%@ include file="csrf.jspf" %>

                            <div class="col-md-4">
                                <label class="form-label" for="tipo_item">Tipo</label>
                                <select class="form-select" id="tipo_item" name="tipo_item">
                                    <c:forEach items="${tiposItem}" var="tipo">
                                        <option value="${tipo.name}" ${tipoItem == tipo.name ? 'selected' : ''}>${tipo.etiqueta}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-8" id="tipo-servicio-group" style="display:none">
                                <label class="form-label" for="tipo_servicio_sugerido">Plantilla de servicio (${tipoNegocioEtiqueta})</label>
                                <select class="form-select" id="tipo_servicio_sugerido">
                                    <option value="">-- Opcional: elegir plantilla --</option>
                                    <c:forEach items="${sugerenciasServicio}" var="sug">
                                        <option value="${sug.nombre}" data-categoria="${sug.categoria}">${sug.nombre} (${sug.categoria})</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-8" id="nombre-group">
                                <label class="form-label" for="nombre">Nombre</label>
                                <input type="text" class="form-control" id="nombre" name="nombre"
                                       value="${nombre}" required
                                       placeholder="Ej. Corte caballero, Refresco 600ml...">
                                <c:if test="${not empty errores.nombre}">
                                    <div class="text-danger small">${errores.nombre}</div>
                                </c:if>
                            </div>

                            <div class="col-md-4" id="sku-group">
                                <label class="form-label" for="sku">SKU <span id="sku-hint" class="text-muted small"></span></label>
                                <input type="text" class="form-control" id="sku" name="sku" maxlength="10"
                                       value="${sku}" placeholder="SKU001">
                                <c:if test="${not empty errores.sku}">
                                    <div class="text-danger small">${errores.sku}</div>
                                </c:if>
                            </div>

                            <div class="col-md-4">
                                <label class="form-label" for="precio">Precio</label>
                                <input type="number" class="form-control" id="precio" name="precio" min="1"
                                       value="${precio}" required>
                                <c:if test="${not empty errores.precio}">
                                    <div class="text-danger small">${errores.precio}</div>
                                </c:if>
                            </div>

                            <div class="col-md-4" id="existencias-group">
                                <label class="form-label" for="existencias">Existencias</label>
                                <input type="number" class="form-control" id="existencias" name="existencias" min="0"
                                       value="${empty existencias ? 10 : existencias}">
                                <c:if test="${not empty errores.existencias}">
                                    <div class="text-danger small">${errores.existencias}</div>
                                </c:if>
                            </div>

                            <div class="col-md-4">
                                <label class="form-label" for="categoria">Categoria</label>
                                <select class="form-select" id="categoria" name="categoria" required>
                                    <option value="">Selecciona</option>
                                    <c:forEach items="${categorias}" var="c">
                                        <option value="${c.id}" data-nombre="${c.nombre}"
                                                ${categoriaId == c.id ? 'selected' : ''}>${c.nombre}</option>
                                    </c:forEach>
                                </select>
                                <c:if test="${not empty errores.categoria}">
                                    <div class="text-danger small">${errores.categoria}</div>
                                </c:if>
                            </div>

                            <div class="col-12 d-flex flex-wrap gap-2">
                                <button type="submit" class="btn btn-primary">Guardar y continuar</button>
                                <a href="${pageContext.request.contextPath}/onboarding" class="btn btn-outline-secondary">Atras</a>
                            </div>
                        </form>
                    </c:if>

                    <c:if test="${paso == 3}">
                        <h1 class="h3 mb-3 text-success"><i class="bi bi-check-circle"></i> Listo para vender</h1>
                        <p class="text-muted">
                            Ya tienes categorias y al menos un articulo en el catalogo. El siguiente paso es registrar tu primera venta en mostrador.
                        </p>
                        <div class="d-flex flex-wrap gap-2">
                            <form method="post" action="${pageContext.request.contextPath}/onboarding/completar">
                                <%@ include file="csrf.jspf" %>
                                <button type="submit" class="btn btn-primary btn-lg">Ir a ventas</button>
                            </form>
                            <a href="${pageContext.request.contextPath}/crudprod" class="btn btn-outline-primary">Ver catalogo</a>
                        </div>
                    </c:if>

                </div>
            </div>
        </div>
    </div>
</div>
<c:if test="${paso == 2}">
<script>
(function () {
    var tipo = document.getElementById('tipo_item');
    var existenciasGroup = document.getElementById('existencias-group');
    var existencias = document.getElementById('existencias');
    var sku = document.getElementById('sku');
    var skuHint = document.getElementById('sku-hint');
    var tipoServicioGroup = document.getElementById('tipo-servicio-group');
    var tipoServicioSugerido = document.getElementById('tipo_servicio_sugerido');
    var nombre = document.getElementById('nombre');
    var categoria = document.getElementById('categoria');

    function seleccionarCategoria(nombreCat) {
        if (!nombreCat) return;
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

    function actualizarTipo() {
        var esServicio = tipo.value === 'SERVICIO';
        existenciasGroup.style.display = esServicio ? 'none' : '';
        tipoServicioGroup.style.display = esServicio ? '' : 'none';
        sku.required = !esServicio;
        skuHint.textContent = esServicio ? '(opcional)' : '(requerido)';
        if (esServicio) {
            existencias.value = '0';
        }
    }

    tipo.addEventListener('change', actualizarTipo);
    tipoServicioSugerido.addEventListener('change', function () {
        var opt = tipoServicioSugerido.options[tipoServicioSugerido.selectedIndex];
        if (!opt || !opt.value) return;
        nombre.value = opt.value;
        seleccionarCategoria(opt.getAttribute('data-categoria'));
    });
    actualizarTipo();
})();
</script>
</c:if>
</body>
</html>
