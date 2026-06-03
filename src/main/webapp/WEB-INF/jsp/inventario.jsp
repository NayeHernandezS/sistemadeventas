<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Inventario</title>
</head>
<body>
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
            Para cambiar nombre, precio o categoria usa <strong>Editar</strong>.
        </div>
    </c:if>

    <c:if test="${soloLectura}">
        <div class="alert alert-info">
            Vista de solo lectura: puedes ver nombre, existencias y precio. No puedes agregar, editar ni eliminar productos.
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
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/">Volver</a>
            <c:if test="${esAdmin}">
                <a class="btn btn-primary ms-2" href="${pageContext.request.contextPath}/productos/form">Crear producto [+]</a>
                <a class="btn btn-outline-primary ms-2" href="${pageContext.request.contextPath}/categorias">Categorias</a>
                <a class="btn btn-outline-secondary ms-2" href="${pageContext.request.contextPath}/inventario/movimientos">
                    Historial de movimientos
                </a>
            </c:if>
        </div>
    </c:if>

    <table class="table table-hover table-striped">
        <thead>
        <tr>
            <c:if test="${esAdmin}">
                <th>ID</th>
            </c:if>
            <th>Nombre</th>
            <c:if test="${esAdmin}">
                <th>Categoria</th>
            </c:if>
            <th>Existencias</th>
            <th>Precio</th>
            <c:if test="${esAdmin}">
                <th title="Entrada, salida o conteo de existencias">Ajustar stock</th>
                <th>Editar</th>
                <th>Eliminar</th>
            </c:if>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${productos}" var="p">
            <tr class="${p.existencias == 0 ? 'table-danger' : (p.existencias <= stockMinimo ? 'table-warning' : '')}">
                <c:if test="${esAdmin}">
                    <td>${p.id}</td>
                </c:if>
                <td>${p.nombre}</td>
                <c:if test="${esAdmin}">
                    <td><c:out value="${empty p.categoria.nombre ? '—' : p.categoria.nombre}"/></td>
                </c:if>
                <td>
                    ${p.existencias}
                    <c:if test="${p.existencias == 0}">
                        <span class="badge bg-danger ms-1">Agotado</span>
                    </c:if>
                    <c:if test="${p.existencias > 0 && p.existencias <= stockMinimo}">
                        <span class="badge bg-warning text-dark ms-1">Stock bajo</span>
                    </c:if>
                </td>
                <td>$${p.precio}</td>
                <c:if test="${esAdmin}">
                    <td>
                        <c:if test="${p.id != null && p.id > 0}">
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/inventario/ajuste?id=${p.id}">Ajustar</a>
                        </c:if>
                    </td>
                    <td>
                        <a class="btn btn-sm btn-success"
                           href="${pageContext.request.contextPath}/productos/form?id=${p.id}">Editar</a>
                    </td>
                    <td>
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
</body>
</html>
