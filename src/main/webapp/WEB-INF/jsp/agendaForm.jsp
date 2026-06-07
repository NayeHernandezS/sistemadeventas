<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.time.format.DateTimeFormatter" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="fmtFechaHora" value="<%=DateTimeFormatter.ofPattern(\"yyyy-MM-dd'T'HH:mm\")%>"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Cita de servicio</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <h1 class="h3 mb-3">
        <c:choose>
            <c:when test="${cita.id != null && cita.id > 0}">Editar cita</c:when>
            <c:otherwise>Nueva cita</c:otherwise>
        </c:choose>
    </h1>

    <c:if test="${sinServicios}">
        <div class="alert alert-warning">
            No tienes servicios en el catalogo para agendar.
            <c:if test="${sessionScope.rol eq 'ADMIN'}">
                <a href="${pageContext.request.contextPath}/productos/form?tipo_item=SERVICIO">Agrega un servicio</a>
                o ve al <a href="${pageContext.request.contextPath}/productos/servicios">catalogo de servicios</a>.
            </c:if>
            <c:if test="${sessionScope.rol ne 'ADMIN'}">
                Pide al administrador que registre servicios en el catalogo.
            </c:if>
        </div>
    </c:if>

    <c:if test="${errores != null && not empty errores.general}">
        <div class="alert alert-danger">${errores.general}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/agenda/guardar" class="card card-body shadow-sm">
        <%@ include file="csrf.jspf" %>
        <input type="hidden" name="id" value="${cita.id != null ? cita.id : 0}">

        <div class="mb-3">
            <label for="productoId" class="form-label">Servicio *</label>
            <select class="form-select" id="productoId" name="productoId" required ${sinServicios ? 'disabled' : ''}>
                <option value="">— Selecciona —</option>
                <c:forEach items="${servicios}" var="s">
                    <option value="${s.id}" ${cita.productoId == s.id ? 'selected' : ''}>
                        ${s.nombre} — $${s.precio}
                    </option>
                </c:forEach>
            </select>
            <c:if test="${errores != null && not empty errores.productoId}">
                <div class="text-danger small">${errores.productoId}</div>
            </c:if>
            <c:if test="${sessionScope.rol eq 'ADMIN'}">
                <div class="form-text">
                    <a href="${pageContext.request.contextPath}/productos/form?tipo_item=SERVICIO">Agregar servicio</a>
                    ·
                    <a href="${pageContext.request.contextPath}/productos/servicios">Ver catalogo</a>
                </div>
            </c:if>
        </div>

        <div class="mb-3">
            <label for="clienteId" class="form-label">Cliente (opcional)</label>
            <select class="form-select" id="clienteId" name="clienteId">
                <option value="0">— Sin cliente —</option>
                <c:forEach items="${clientes}" var="cl">
                    <option value="${cl.id}" ${cita.clienteId == cl.id ? 'selected' : ''}>${cl.nombre}</option>
                </c:forEach>
            </select>
            <c:if test="${errores != null && not empty errores.clienteId}">
                <div class="text-danger small">${errores.clienteId}</div>
            </c:if>
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-6">
                <label for="fechaHora" class="form-label">Fecha y hora *</label>
                <input type="datetime-local" class="form-control" id="fechaHora" name="fechaHora" required
                       value="${cita.fechaHora != null ? cita.fechaHora.format(fmtFechaHora) : ''}">
                <c:if test="${errores != null && not empty errores.fechaHora}">
                    <div class="text-danger small">${errores.fechaHora}</div>
                </c:if>
            </div>
            <div class="col-md-6">
                <label for="duracionMinutos" class="form-label">Duracion (minutos) *</label>
                <input type="number" class="form-control" id="duracionMinutos" name="duracionMinutos"
                       min="5" step="5" value="${cita.duracionMinutos > 0 ? cita.duracionMinutos : 30}">
                <c:if test="${errores != null && not empty errores.duracionMinutos}">
                    <div class="text-danger small">${errores.duracionMinutos}</div>
                </c:if>
            </div>
        </div>

        <c:if test="${cita.id != null && cita.id > 0}">
        <div class="mb-3">
            <label for="estado" class="form-label">Estado</label>
            <select class="form-select" id="estado" name="estado">
                <c:forEach items="${estadosCita}" var="e">
                    <c:if test="${e.name ne 'COMPLETADA' && e.name ne 'CANCELADA'}">
                        <option value="${e.name}" ${cita.estado.name eq e.name ? 'selected' : ''}>${e.etiqueta}</option>
                    </c:if>
                </c:forEach>
            </select>
        </div>
        </c:if>

        <div class="mb-3">
            <label for="notas" class="form-label">Notas</label>
            <textarea class="form-control" id="notas" name="notas" rows="2" maxlength="500"
                      placeholder="Indicaciones, recordatorios...">${cita.notas}</textarea>
        </div>

        <div class="d-flex gap-2">
            <button type="submit" class="btn btn-primary" ${sinServicios ? 'disabled' : ''}>Guardar</button>
            <a class="btn btn-secondary"
               href="${pageContext.request.contextPath}/agenda?fecha=${fechaAgenda}">Volver a agenda</a>
        </div>
    </form>
</div>
</body>
</html>
