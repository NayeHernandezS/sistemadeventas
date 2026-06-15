<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Soporte</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4" style="max-width: 720px;">
    <h1 class="h3 mb-3"><i class="bi bi-headset"></i> Soporte</h1>
    <p class="text-muted">Contacta al equipo del sistema para ayuda con tu cuenta, suscripcion o el uso de la app.</p>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <div class="card mb-4">
        <div class="card-body">
            <h2 class="h6">Contacto directo</h2>
            <p class="mb-1"><i class="bi bi-envelope"></i>
                <a href="mailto:${soporteEmail}">${soporteEmail}</a>
            </p>
            <c:if test="${not empty soporteWhatsapp}">
                <p class="mb-1"><i class="bi bi-whatsapp text-success"></i> ${soporteWhatsapp}</p>
            </c:if>
            <p class="mb-0 text-muted small"><i class="bi bi-clock"></i> ${soporteHorario}</p>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header">Enviar solicitud</div>
        <div class="card-body">
            <form method="post" action="${pageContext.request.contextPath}/soporte">
                <%@ include file="csrf.jspf" %>
                <div class="mb-3">
                    <label for="email" class="form-label">Correo de respuesta</label>
                    <input type="email" class="form-control" id="email" name="email"
                           value="${email}" required>
                </div>
                <div class="mb-3">
                    <label for="asunto" class="form-label">Asunto</label>
                    <input type="text" class="form-control" id="asunto" name="asunto" maxlength="120"
                           value="${asunto}" required
                           placeholder="Ej. Renovar suscripcion, error al vender">
                </div>
                <div class="mb-3">
                    <label for="mensaje" class="form-label">Mensaje</label>
                    <textarea class="form-control" id="mensaje" name="mensaje" rows="4" required
                              placeholder="Describe tu problema o duda">${mensaje}</textarea>
                </div>
                <button type="submit" class="btn btn-primary">Enviar solicitud</button>
            </form>
        </div>
    </div>

    <c:if test="${not empty historial}">
        <h2 class="h6">Tus solicitudes anteriores</h2>
        <ul class="list-group mb-3">
            <c:forEach items="${historial}" var="h">
                <li class="list-group-item">
                    <div class="d-flex justify-content-between">
                        <strong>${h.asunto}</strong>
                        <span class="badge ${h.estado eq 'ABIERTA' ? 'bg-warning text-dark' : 'bg-secondary'}">
                            ${h.estado}
                        </span>
                    </div>
                    <div class="small text-muted">${h.fechaSolicitud}</div>
                    <div class="small mt-1">${h.mensaje}</div>
                </li>
            </c:forEach>
        </ul>
    </c:if>

    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/inicio">Volver al panel</a>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
