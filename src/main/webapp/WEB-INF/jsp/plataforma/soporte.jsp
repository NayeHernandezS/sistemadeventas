<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Soporte - Plataforma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav.jspf" %>
<div class="container pb-5">
    <h1 class="h3 mb-3">Solicitudes de soporte</h1>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>

    <c:if test="${empty solicitudes}">
        <p class="text-muted">No hay solicitudes abiertas.</p>
    </c:if>

    <c:if test="${not empty solicitudes}">
        <c:forEach items="${solicitudes}" var="s">
            <div class="card mb-3">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <strong>${s.asunto}</strong>
                    <span class="badge bg-warning text-dark">${s.estado}</span>
                </div>
                <div class="card-body">
                    <p class="mb-1"><strong>Cuenta:</strong> ${s.tenantOwner}</p>
                    <p class="mb-1"><strong>Usuario:</strong> ${s.username}</p>
                    <p class="mb-1"><strong>Email:</strong>
                        <c:choose>
                            <c:when test="${not empty s.emailContacto}">
                                <a href="mailto:${s.emailContacto}">${s.emailContacto}</a>
                            </c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </p>
                    <p class="mb-1 text-muted small">${s.fechaSolicitud}</p>
                    <p class="mb-3">${s.mensaje}</p>
                    <form method="post" action="${pageContext.request.contextPath}/plataforma/soporte/atender">
                        <%@ include file="../csrf.jspf" %>
                        <input type="hidden" name="id" value="${s.id}">
                        <button type="submit" class="btn btn-sm btn-success">Marcar atendida</button>
                    </form>
                </div>
            </div>
        </c:forEach>
    </c:if>
</div>
</body>
</html>
