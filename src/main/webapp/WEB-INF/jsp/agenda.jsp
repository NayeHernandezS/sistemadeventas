<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.time.format.DateTimeFormatter" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="fmtHora" value="<%=DateTimeFormatter.ofPattern(\"HH:mm\")%>"/>
<c:set var="fmtDia" value="<%=DateTimeFormatter.ofPattern(\"EEEE d 'de' MMMM yyyy\", java.util.Locale.forLanguageTag(\"es-MX\"))%>"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Agenda de servicios</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 class="h3 mb-0"><i class="bi bi-calendar-event"></i> Agenda de servicios</h1>
        <div class="d-flex flex-wrap gap-2">
            <c:if test="${sessionScope.rol eq 'ADMIN'}">
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/productos/servicios">
                    <i class="bi bi-scissors"></i> Servicios
                </a>
                <a class="btn btn-outline-success" href="${pageContext.request.contextPath}/productos/form?tipo_item=SERVICIO">
                    <i class="bi bi-plus-lg"></i> Agregar servicio
                </a>
            </c:if>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/agenda/nueva?fecha=${fecha}">
                <i class="bi bi-plus-lg"></i> Nueva cita
            </a>
        </div>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <div class="card mb-4">
        <div class="card-body d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div class="btn-group">
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/agenda?fecha=${fechaAnterior}">
                    <i class="bi bi-chevron-left"></i> Anterior
                </a>
                <a class="btn btn-outline-secondary ${esHoy ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/agenda">Hoy</a>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/agenda?fecha=${fechaSiguiente}">
                    Siguiente <i class="bi bi-chevron-right"></i>
                </a>
            </div>
            <form method="get" action="${pageContext.request.contextPath}/agenda" class="d-flex gap-2 align-items-center">
                <label for="fecha" class="form-label mb-0 small text-muted">Dia</label>
                <input type="date" class="form-control" id="fecha" name="fecha" value="${fecha}">
                <button type="submit" class="btn btn-secondary">Ir</button>
            </form>
            <p class="mb-0 fw-semibold">${fecha.format(fmtDia)}</p>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty citas}">
            <p class="text-muted">No hay citas programadas para este dia.
                <a href="${pageContext.request.contextPath}/agenda/nueva?fecha=${fecha}">Agendar una</a>
            </p>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table table-striped align-middle">
                    <thead>
                    <tr>
                        <th>Hora</th>
                        <th>Servicio</th>
                        <th>Cliente</th>
                        <th>Duracion</th>
                        <th>Estado</th>
                        <th>Notas</th>
                        <th class="text-end">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${citas}" var="c">
                        <tr>
                            <td>
                                ${c.fechaHora.format(fmtHora)}
                                <span class="text-muted small">— ${c.fechaHoraFin.format(fmtHora)}</span>
                            </td>
                            <td>${c.servicioNombre}</td>
                            <td><c:out value="${empty c.clienteNombre ? '—' : c.clienteNombre}"/></td>
                            <td>${c.duracionMinutos} min</td>
                            <td>
                                <c:choose>
                                    <c:when test="${c.estado.name eq 'CONFIRMADA'}">
                                        <span class="badge text-bg-primary">${c.estado.etiqueta}</span>
                                    </c:when>
                                    <c:when test="${c.estado.name eq 'COMPLETADA'}">
                                        <span class="badge text-bg-success">${c.estado.etiqueta}</span>
                                    </c:when>
                                    <c:when test="${c.estado.name eq 'CANCELADA'}">
                                        <span class="badge text-bg-secondary">${c.estado.etiqueta}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge text-bg-warning text-dark">${c.estado.etiqueta}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="small"><c:out value="${empty c.notas ? '—' : c.notas}"/></td>
                            <td class="text-end">
                                <div class="d-flex flex-wrap gap-1 justify-content-end">
                                    <c:if test="${c.editable}">
                                        <a class="btn btn-sm btn-outline-primary"
                                           href="${pageContext.request.contextPath}/agenda/editar?id=${c.id}">Editar</a>
                                        <form method="post" action="${pageContext.request.contextPath}/agenda/confirmar" class="d-inline">
                                            <%@ include file="csrf.jspf" %>
                                            <input type="hidden" name="id" value="${c.id}">
                                            <input type="hidden" name="fecha" value="${fecha}">
                                            <button type="submit" class="btn btn-sm btn-outline-info">Confirmar</button>
                                        </form>
                                    </c:if>
                                    <c:if test="${c.estado.name ne 'CANCELADA' && c.estado.name ne 'COMPLETADA'}">
                                        <form method="post" action="${pageContext.request.contextPath}/agenda/completar" class="d-inline">
                                            <%@ include file="csrf.jspf" %>
                                            <input type="hidden" name="id" value="${c.id}">
                                            <input type="hidden" name="fecha" value="${fecha}">
                                            <button type="submit" class="btn btn-sm btn-success"
                                                    title="Marca completada y abre el carrito con el servicio">
                                                Cobrar
                                            </button>
                                        </form>
                                        <form method="post" action="${pageContext.request.contextPath}/agenda/cancelar" class="d-inline"
                                              onsubmit="return confirm('¿Cancelar esta cita?');">
                                            <%@ include file="csrf.jspf" %>
                                            <input type="hidden" name="id" value="${c.id}">
                                            <input type="hidden" name="fecha" value="${fecha}">
                                            <button type="submit" class="btn btn-sm btn-outline-danger">Cancelar</button>
                                        </form>
                                    </c:if>
                                    <c:if test="${c.estado.name eq 'COMPLETADA' && c.ticketId != null}">
                                        <a class="small text-muted align-self-center"
                                           href="${pageContext.request.contextPath}/tickets/ver?id=${c.ticketId}">
                                            Ticket #${c.ticketId}
                                        </a>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>

    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/inicio">Inicio</a>
    <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/crudprod">Catalogo</a>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
