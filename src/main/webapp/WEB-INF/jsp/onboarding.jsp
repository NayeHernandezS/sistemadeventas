<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
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
                <span class="step ${paso >= 3 ? (paso == 3 ? 'active' : 'done') : 'pending'}">3</span>
                <span class="mx-2 text-muted">—</span>
                <span class="step ${paso >= 4 ? 'active' : 'pending'}">4</span>
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
                            <c:choose>
                                <c:when test="${puedeSaltarPasoProducto}">
                                    <a href="${pageContext.request.contextPath}/onboarding/venta" class="btn btn-primary">
                                        Siguiente: practicar primera venta
                                    </a>
                                    <a href="${pageContext.request.contextPath}/onboarding/producto?agregar=1" class="btn btn-outline-primary">
                                        Agregar otro articulo
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/onboarding/producto" class="btn btn-primary">
                                        <c:choose>
                                            <c:when test="${predominanServicios}">Siguiente: agregar servicio</c:when>
                                            <c:otherwise>Siguiente: primer articulo</c:otherwise>
                                        </c:choose>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                            <form method="post" action="${pageContext.request.contextPath}/onboarding/omitir" class="d-inline">
                                <%@ include file="csrf.jspf" %>
                                <button type="submit" class="btn btn-outline-secondary">Omitir por ahora</button>
                            </form>
                        </div>
                    </c:if>

                    <c:if test="${paso == 2}">
                        <h1 class="h3 mb-3"><i class="bi bi-grid"></i> Tu catalogo</h1>
                        <c:if test="${puedeSaltarPasoProducto}">
                            <div class="alert alert-success py-2">
                                Ya tienes <strong>${totalProductos}</strong> articulo(s) en el catalogo
                                <c:if test="${importaCatalogoProductos}"> (plantilla del rubro cargada)</c:if>.
                            </div>
                        </c:if>
                        <p class="text-muted">
                            Agrega un <strong>producto</strong> (con stock) o un <strong>servicio</strong> (sin inventario) para probar una venta.
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
                                <input type="text" class="form-control" id="sku" name="sku" maxlength="13"
                                       value="${sku}" placeholder="7501234567890">
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
                                <c:if test="${puedeSaltarPasoProducto}">
                                    <a href="${pageContext.request.contextPath}/onboarding/producto/saltar" class="btn btn-outline-primary">
                                        Continuar sin agregar otro
                                    </a>
                                </c:if>
                                <a href="${pageContext.request.contextPath}/onboarding" class="btn btn-outline-secondary">Atras</a>
                            </div>
                        </form>
                    </c:if>

                    <c:if test="${paso == 3}">
                        <h1 class="h3 mb-3"><i class="bi bi-cart-check text-primary"></i> Tu primera venta</h1>
                        <p class="text-muted mb-4">
                            Elige un articulo y registra una venta de prueba. Veras el ticket como en el mostrador real.
                        </p>

                        <c:if test="${not empty errores.general}">
                            <div class="alert alert-danger">${errores.general}</div>
                        </c:if>

                        <c:choose>
                            <c:when test="${empty productosVenta}">
                                <div class="alert alert-warning">
                                    No hay articulos con precio en tu catalogo.
                                    <a href="${pageContext.request.contextPath}/onboarding/producto">Agrega uno primero</a>.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="list-group mb-4">
                                    <c:forEach items="${productosVenta}" var="p">
                                        <form method="post" action="${pageContext.request.contextPath}/onboarding/venta"
                                              class="list-group-item list-group-item-action d-flex flex-wrap justify-content-between align-items-center gap-2">
                                            <%@ include file="csrf.jspf" %>
                                            <input type="hidden" name="productoId" value="${p.id}">
                                            <div>
                                                <strong>${p.nombre}</strong>
                                                <span class="text-muted small ms-2">
                                                    <c:if test="${p.esServicio}">Servicio</c:if>
                                                    <c:if test="${!p.esServicio}">${p.existencias} en stock</c:if>
                                                </span>
                                            </div>
                                            <div class="d-flex align-items-center gap-3">
                                                <span class="fw-semibold">$${p.precio}</span>
                                                <button type="submit" class="btn btn-sm btn-primary">Cobrar 1 unidad</button>
                                            </div>
                                        </form>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <a href="${pageContext.request.contextPath}/onboarding/producto?agregar=1" class="btn btn-outline-secondary btn-sm">
                            Agregar otro articulo
                        </a>
                    </c:if>

                    <c:if test="${paso == 4}">
                        <h1 class="h3 mb-3 text-success"><i class="bi bi-check-circle"></i> Tu negocio ya puede cobrar</h1>
                        <p class="text-muted">
                            Registraste tu primera venta. Desde aqui puedes seguir cobrando en
                            <strong>Modulo de ventas</strong> o revisar el ticket en <strong>Mis tickets</strong>.
                        </p>

                        <div class="accordion mb-4" id="tutorialFacturacion">
                            <div class="accordion-item">
                                <h2 class="accordion-header">
                                    <button class="accordion-button" type="button" data-bs-toggle="collapse"
                                            data-bs-target="#pasoFiscal" aria-expanded="true">
                                        1. Datos fiscales de tu negocio (PDF y CFDI)
                                    </button>
                                </h2>
                                <div id="pasoFiscal" class="accordion-collapse collapse show" data-bs-parent="#tutorialFacturacion">
                                    <div class="accordion-body small">
                                        <p class="mb-2">En <strong>Mi perfil</strong> guarda el RFC, razon social, codigo postal y regimen fiscal
                                            <strong>de tu negocio</strong> (los de tu constancia del SAT).</p>
                                        <p class="mb-0 text-muted">Con solo esto ya puedes emitir comprobante PDF al cobrar.</p>
                                    </div>
                                </div>
                            </div>
                            <div class="accordion-item">
                                <h2 class="accordion-header">
                                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                            data-bs-target="#pasoFacturama">
                                        2. CFDI timbrado ante el SAT (opcional)
                                    </button>
                                </h2>
                                <div id="pasoFacturama" class="accordion-collapse collapse" data-bs-parent="#tutorialFacturacion">
                                    <div class="accordion-body small">
                                        <ol class="mb-0 ps-3">
                                            <li class="mb-2">Crea cuenta en
                                                <a href="https://apisandbox.facturama.mx/" target="_blank" rel="noopener">Facturama sandbox</a>
                                                (pruebas) o produccion.</li>
                                            <li class="mb-2">Completa el wizard y sube el <strong>CSD</strong> de tu RFC en el panel de Facturama.</li>
                                            <li class="mb-2">En <strong>Mi perfil → Timbrado CFDI</strong> pega usuario y contraseña API y activa timbrado.</li>
                                            <li>Al cobrar, marca factura e indica RFC y codigo postal del cliente.</li>
                                        </ol>
                                    </div>
                                </div>
                            </div>
                            <div class="accordion-item">
                                <h2 class="accordion-header">
                                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                            data-bs-target="#pasoClientes">
                                        3. Clientes frecuentes (recomendado)
                                    </button>
                                </h2>
                                <div id="pasoClientes" class="accordion-collapse collapse" data-bs-parent="#tutorialFacturacion">
                                    <div class="accordion-body small">
                                        En <strong>Clientes</strong> registra RFC, razon social y uso CFDI.
                                        Al cobrar, al elegir un cliente se precargan sus datos fiscales.
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex flex-wrap gap-2">
                            <form method="post" action="${pageContext.request.contextPath}/onboarding/completar">
                                <%@ include file="csrf.jspf" %>
                                <button type="submit" class="btn btn-primary btn-lg">Ir al panel</button>
                            </form>
                            <a href="${pageContext.request.contextPath}/productos" class="btn btn-outline-primary">Ir a ventas</a>
                            <a href="${pageContext.request.contextPath}/perfil" class="btn btn-outline-secondary btn-sm" target="_blank" rel="noopener">
                                Configurar facturacion
                            </a>
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
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
