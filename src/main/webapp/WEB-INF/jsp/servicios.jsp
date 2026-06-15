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
    <title>Catalogo de servicios</title>
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 class="h3 mb-0"><i class="bi bi-scissors"></i> Catalogo de servicios</h1>
        <c:if test="${esAdmin}">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/productos/form?tipo_item=SERVICIO">
                <i class="bi bi-plus-lg"></i> Agregar servicio
            </a>
        </c:if>
    </div>

    <p class="text-muted small">
        Estos servicios aparecen en la agenda y en caja. No consumen inventario al cobrarse.
    </p>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <c:if test="${soloLectura}">
        <div class="alert alert-info py-2">
            Vista de solo lectura. Pide al administrador que agregue o edite servicios.
        </div>
    </c:if>

    <div class="mb-3">
        <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/inicio">Inicio</a>
        <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/agenda">Agenda</a>
        <c:if test="${esAdmin}">
            <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/crudprod">Inventario completo</a>
        </c:if>
    </div>

    <c:if test="${empty servicios}">
        <div class="alert alert-warning">
            Aun no tienes servicios registrados.
            <c:if test="${esAdmin}">
                <a href="${pageContext.request.contextPath}/productos/form?tipo_item=SERVICIO">Agrega el primero</a>
                o revisa las plantillas sugeridas segun tu rubro.
            </c:if>
        </div>
    </c:if>

    <c:if test="${not empty servicios}">
        <c:set var="buscadorTablaId" value="tablaServicios"/>
        <c:set var="buscadorPlaceholder" value="Buscar servicio por nombre o categoria..."/>
        <%@ include file="fragmentos/buscador-tabla.jspf" %>

        <div class="table-responsive">
            <table id="tablaServicios" class="table table-hover table-striped">
                <thead>
                <tr>
                    <c:if test="${esAdmin}"><th>ID</th></c:if>
                    <th>Servicio</th>
                    <th>Categoria</th>
                    <th>Precio</th>
                    <c:if test="${esAdmin}"><th>Acciones</th></c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${servicios}" var="s">
                    <tr data-fila-busqueda="1"
                        data-buscar="${s.nombre} ${s.categoria.nombre} ${s.id}">
                        <c:if test="${esAdmin}"><td>${s.id}</td></c:if>
                        <td>${s.nombre}</td>
                        <td><c:out value="${empty s.categoria.nombre ? '—' : s.categoria.nombre}"/></td>
                        <td>$${s.precio}</td>
                        <c:if test="${esAdmin}">
                            <td class="text-nowrap">
                                <a class="btn btn-sm btn-success"
                                   href="${pageContext.request.contextPath}/productos/form?id=${s.id}">Editar</a>
                                <a class="btn btn-sm btn-danger"
                                   onclick="return confirm('¿Eliminar este servicio?');"
                                   href="${pageContext.request.contextPath}/productos/eliminar?id=${s.id}&origen=servicios">Eliminar</a>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>
</div>
<script src="${pageContext.request.contextPath}/js/buscador-tabla.js"></script>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
