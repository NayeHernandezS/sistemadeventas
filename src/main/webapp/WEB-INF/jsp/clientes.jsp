<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <title>Clientes</title>
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-4">
    <h1 class="mb-3">Clientes</h1>

    <c:if test="${not esAdmin}">
        <div class="alert alert-info">
            Vista de consulta. Solo el administrador puede dar de alta o editar clientes.
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.mensajeError}">
        <div class="alert alert-danger">${sessionScope.mensajeError}</div>
        <c:remove var="mensajeError" scope="session"/>
    </c:if>

    <div class="mb-3">
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/">Volver</a>
        <c:if test="${esAdmin}">
            <a class="btn btn-primary ms-2" href="${pageContext.request.contextPath}/clientes/form">Nuevo cliente [+]</a>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${empty clientes}">
            <p class="text-muted">No hay clientes registrados.
                <c:if test="${esAdmin}">Crea uno para reutilizar datos al facturar.</c:if>
            </p>
        </c:when>
        <c:otherwise>
            <table class="table table-hover table-striped">
                <thead>
                <tr>
                    <th>Nombre</th>
                    <th>RFC</th>
                    <th>Razon social</th>
                    <th>Correo</th>
                    <th>C.P.</th>
                    <th>Uso CFDI</th>
                    <c:if test="${esAdmin}">
                        <th></th>
                        <th></th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${clientes}" var="cl">
                    <tr>
                        <td>${cl.nombre}</td>
                        <td><c:out value="${empty cl.rfc ? '—' : cl.rfc}"/></td>
                        <td><c:out value="${empty cl.razonSocial ? '—' : cl.razonSocial}"/></td>
                        <td><c:out value="${empty cl.email ? '—' : cl.email}"/></td>
                        <td><c:out value="${empty cl.codigoPostal ? '—' : cl.codigoPostal}"/></td>
                        <td><c:out value="${empty cl.usoCfdi ? '—' : cl.usoCfdi}"/></td>
                        <c:if test="${esAdmin}">
                            <td>
                                <a class="btn btn-sm btn-success"
                                   href="${pageContext.request.contextPath}/clientes/form?id=${cl.id}">Editar</a>
                            </td>
                            <td>
                                <a class="btn btn-sm btn-danger"
                                   onclick="return confirm('¿Desactivar este cliente? Ya no aparecera en el listado.');"
                                   href="${pageContext.request.contextPath}/clientes/eliminar?id=${cl.id}">Eliminar</a>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>
