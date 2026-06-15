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
    <title>Inventario</title>
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="mb-3">
        <c:choose>
            <c:when test="${esAdmin}">Inventario</c:when>
            <c:otherwise>Consulta de inventario</c:otherwise>
        </c:choose>
    </h1>

    <c:if test="${not empty sessionScope.mensajeExito}">
        <div class="alert alert-success">${sessionScope.mensajeExito}</div>
        <c:remove var="mensajeExito" scope="session"/>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <c:if test="${esAdmin}">
        <div class="alert alert-info py-2 small mb-3">
            <strong>Ajustar</strong> registra entradas de mercancia, salidas (merma o uso) o corrige el conteo fisico.
            Para cambiar nombre, precios o categoria usa <strong>Editar</strong>.
            El <strong>precio de compra</strong>, el <strong>% de ganancia</strong> y el <strong>margen</strong> son datos internos: solo tu y otros administradores los ven en esta tabla.
        </div>
    </c:if>

    <c:if test="${soloLectura}">
        <div class="alert alert-info">
            Vista de solo lectura: puedes ver nombre, existencias y precio de venta.
            El precio de compra, el porcentaje de ganancia y el margen son informacion interna y solo la ve el administrador.
            No puedes agregar, editar ni eliminar productos.
        </div>
    </c:if>

    <c:if test="${cantidadConAlerta > 0}">
        <div class="alert alert-warning">
            <i class="bi bi-exclamation-triangle"></i>
            <strong>Alertas de inventario</strong> (umbral: ${stockMinimo} unidades):
            <c:if test="${cantidadAgotados > 0}">
                ${cantidadAgotados} agotado(s)
            </c:if>
            <c:if test="${cantidadAgotados > 0 && cantidadStockBajo > 0}"> · </c:if>
            <c:if test="${cantidadStockBajo > 0}">
                ${cantidadStockBajo} con stock bajo
            </c:if>
        </div>
    </c:if>

    <c:if test="${cantidadConAlerta == 0 && not empty productos}">
        <div class="alert alert-success py-2">
            <i class="bi bi-check-circle"></i> Todos los productos tienen stock suficiente.
        </div>
    </c:if>

    <c:if test="${logueado}">
        <div class="mb-3">
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/inicio">Volver</a>
            <c:if test="${esAdmin}">
                <a class="btn btn-primary ms-2" href="${pageContext.request.contextPath}/productos/form">Crear producto o servicio</a>
                <c:if test="${mostrarOpcionServicios}">
                    <a class="btn btn-outline-info ms-2" href="${pageContext.request.contextPath}/productos/servicios">
                        <i class="bi bi-scissors"></i> Catalogo de servicios
                    </a>
                    <a class="btn btn-outline-success ms-2" href="${pageContext.request.contextPath}/productos/form?tipo_item=SERVICIO">
                        <i class="bi bi-plus-lg"></i> Agregar servicio
                    </a>
                </c:if>
                <a class="btn btn-outline-primary ms-2" href="${pageContext.request.contextPath}/categorias">Categorias</a>
                <a class="btn btn-outline-secondary ms-2" href="${pageContext.request.contextPath}/inventario/movimientos">
                    Historial de movimientos
                </a>
            </c:if>
        </div>
    </c:if>

    <c:set var="buscadorTablaId" value="tablaProductosInventario"/>
    <c:set var="buscadorPlaceholder" value="Buscar por nombre, SKU, categoria o ID..."/>
    <%@ include file="fragmentos/buscador-tabla.jspf" %>

    <div class="mb-3 d-flex flex-wrap align-items-center gap-2">
        <c:set var="filtrosTablaId" value="tablaProductosInventario"/>
        <c:set var="filtrosEsAdmin" value="${esAdmin}"/>
        <%@ include file="fragmentos/filtros-columnas-tabla.jspf" %>
        <small class="text-muted">Oculta columnas para ver solo los datos que necesitas (ej. nombre y precio de venta).</small>
    </div>

    <div class="table-responsive">
    <table id="tablaProductosInventario" class="table table-hover table-striped">
        <thead>
        <tr>
            <c:if test="${esAdmin}">
                <th data-col="id">ID</th>
            </c:if>
            <th data-col="nombre">Nombre</th>
            <th data-col="tipo">Tipo</th>
            <c:if test="${esAdmin}">
                <th data-col="categoria">Categoria</th>
            </c:if>
            <th data-col="existencias">Existencias</th>
            <th data-col="precio_venta">Precio venta</th>
            <c:if test="${esAdmin}">
                <th data-col="precio_compra">Precio compra</th>
                <th data-col="porcentaje_ganancia" title="Porcentaje configurado al crear o editar el producto">% Ganancia meta</th>
                <th data-col="margen" title="Utilidad en pesos y porcentaje real sobre el costo">Margen</th>
                <th data-col="ajustar" title="Entrada, salida o conteo de existencias">Ajustar stock</th>
                <th data-col="editar">Editar</th>
                <th data-col="eliminar">Eliminar</th>
            </c:if>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${productos}" var="p">
            <tr data-fila-busqueda="1"
                data-buscar="${p.nombre} ${p.sku} ${p.categoria.nombre} ${p.id} ${p.tipoItem.etiqueta}"
                class="${p.esServicio ? '' : (p.existencias == 0 ? 'table-danger' : (p.existencias <= stockMinimo ? 'table-warning' : ''))}">
                <c:if test="${esAdmin}">
                    <td data-col="id">${p.id}</td>
                </c:if>
                <td data-col="nombre">${p.nombre}</td>
                <td data-col="tipo">
                    <c:choose>
                        <c:when test="${p.esServicio}">
                            <span class="badge bg-info text-dark">Servicio</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-secondary">Producto</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <c:if test="${esAdmin}">
                    <td data-col="categoria"><c:out value="${empty p.categoria.nombre ? '—' : p.categoria.nombre}"/></td>
                </c:if>
                <td data-col="existencias">
                    <c:choose>
                        <c:when test="${p.esServicio}">
                            <span class="text-muted">N/A</span>
                        </c:when>
                        <c:otherwise>
                            ${p.existencias}
                            <c:if test="${p.existencias == 0}">
                                <span class="badge bg-danger ms-1">Agotado</span>
                            </c:if>
                            <c:if test="${p.existencias > 0 && p.existencias <= stockMinimo}">
                                <span class="badge bg-warning text-dark ms-1">Stock bajo</span>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td data-col="precio_venta">$${p.precio}</td>
                <c:if test="${esAdmin}">
                    <td data-col="precio_compra">
                        <c:choose>
                            <c:when test="${p.esServicio}">
                                <span class="text-muted">N/A</span>
                            </c:when>
                            <c:when test="${p.precioCompra > 0}">
                                $${p.precioCompra}
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">—</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td data-col="porcentaje_ganancia">
                        <c:choose>
                            <c:when test="${p.esServicio}">
                                <span class="text-muted">N/A</span>
                            </c:when>
                            <c:when test="${p.porcentajeGanancia > 0}">
                                ${p.porcentajeGanancia}%
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">—</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td data-col="margen">
                        <c:choose>
                            <c:when test="${p.esServicio}">
                                <span class="text-muted">N/A</span>
                            </c:when>
                            <c:when test="${p.tieneMargenCalculable}">
                                <span class="${p.margen < 0 ? 'text-danger' : ''}">$${p.margen}</span>
                                <span class="badge ${p.margen < 0 ? 'bg-danger' : 'bg-success'} ms-1"
                                      title="Ganancia real sobre el costo">${p.margenPorcentaje}%</span>
                                <c:if test="${p.porcentajeGanancia > 0 && p.margenPorcentaje != p.porcentajeGanancia}">
                                    <span class="badge bg-warning text-dark ms-1"
                                          title="La meta era ${p.porcentajeGanancia}% pero el precio de venta actual da otro margen">≠ meta</span>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">—</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td data-col="ajustar">
                        <c:if test="${!p.esServicio && p.id != null && p.id > 0}">
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/inventario/ajuste?id=${p.id}">Ajustar</a>
                        </c:if>
                        <c:if test="${p.esServicio}">
                            <span class="text-muted small">—</span>
                        </c:if>
                    </td>
                    <td data-col="editar">
                        <a class="btn btn-sm btn-success"
                           href="${pageContext.request.contextPath}/productos/form?id=${p.id}">Editar</a>
                    </td>
                    <td data-col="eliminar">
                        <a class="btn btn-sm btn-danger"
                           onclick="return confirm('¿Eliminar este producto?');"
                           href="${pageContext.request.contextPath}/productos/eliminar?id=${p.id}">Eliminar</a>
                    </td>
                </c:if>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
<script src="${pageContext.request.contextPath}/js/buscador-tabla.js"></script>
<script src="${pageContext.request.contextPath}/js/filtros-columnas-tabla.js"></script>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
