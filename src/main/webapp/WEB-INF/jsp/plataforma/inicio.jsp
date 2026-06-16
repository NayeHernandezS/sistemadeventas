<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel plataforma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav.jspf" %>
<div class="container pb-5">
    <h1 class="h3 mb-4">Administracion del SaaS</h1>
    <p class="text-muted">Gestiona las cuentas de tus clientes, confirma pagos y extiende suscripciones.</p>

    <div class="row g-4 mt-2">
        <div class="col-md-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h2 class="h5"><i class="bi bi-people text-primary"></i> Clientes</h2>
                    <p class="display-6 mb-3">${totalClientes}</p>
                    <a href="${pageContext.request.contextPath}/plataforma/clientes" class="btn btn-primary btn-sm">
                        Ver cuentas
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h2 class="h5"><i class="bi bi-credit-card text-warning"></i> Pagos pendientes</h2>
                    <p class="display-6 mb-1">${pagosPendientes}</p>
                    <p class="small text-muted mb-3">${pagosExpirados} expirado(s) en historial</p>
                    <a href="${pageContext.request.contextPath}/plataforma/pagos" class="btn btn-outline-primary btn-sm">
                        Revisar pagos
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h2 class="h5"><i class="bi bi-headset text-info"></i> Soporte abierto</h2>
                    <p class="display-6 mb-3">${soporteAbiertas}</p>
                    <a href="${pageContext.request.contextPath}/plataforma/soporte" class="btn btn-outline-primary btn-sm">
                        Ver solicitudes
                    </a>
                </div>
            </div>
        </div>
    </div>

    <div class="card mt-4 border-0 shadow-sm">
        <div class="card-body">
            <h2 class="h6 mb-2"><i class="bi bi-credit-card-2-front"></i> Mercado Pago (SaaS)</h2>
            <c:choose>
                <c:when test="${mercadoPagoEstado.listoProduccion}">
                    <span class="badge bg-success">Listo para cobrar en linea</span>
                </c:when>
                <c:when test="${mercadoPagoEstado.habilitado}">
                    <span class="badge bg-warning text-dark">Configuracion incompleta</span>
                </c:when>
                <c:otherwise>
                    <span class="badge bg-secondary">Solo pago manual</span>
                </c:otherwise>
            </c:choose>
            <c:if test="${not empty mercadoPagoEstado.webhookUrl}">
                <p class="small text-muted mb-1 mt-2">Webhook en panel MP:</p>
                <code class="small user-select-all">${mercadoPagoEstado.webhookUrl}</code>
            </c:if>
            <c:if test="${not empty mercadoPagoEstado.advertencias}">
                <ul class="small text-danger mb-0 mt-2">
                    <c:forEach items="${mercadoPagoEstado.advertencias}" var="adv">
                        <li>${adv}</li>
                    </c:forEach>
                </ul>
            </c:if>
            <p class="small mb-0 mt-2">
                Guia: <code>deploy/CHECKLIST_MERCADOPAGO.md</code> ·
                Script: <code>deploy/scripts/verificar-mercadopago.sh</code>
            </p>
        </div>
    </div>

    <div class="card mt-4 border-0 shadow-sm">
        <div class="card-body">
            <h2 class="h6 mb-2"><i class="bi bi-envelope"></i> Correos de suscripcion</h2>
            <c:choose>
                <c:when test="${correoEstado.listoProduccion}">
                    <span class="badge bg-success">SMTP listo</span>
                </c:when>
                <c:when test="${correoEstado.smtpConfigurado}">
                    <span class="badge bg-warning text-dark">SMTP parcial</span>
                </c:when>
                <c:otherwise>
                    <span class="badge bg-secondary">SMTP no configurado</span>
                </c:otherwise>
            </c:choose>
            <c:if test="${not empty correoEstado.smtpHost}">
                <p class="small text-muted mb-1 mt-2">Servidor: <code>${correoEstado.smtpHost}</code></p>
            </c:if>
            <c:if test="${not empty correoEstado.mailFrom}">
                <p class="small text-muted mb-1">Remitente: <code>${correoEstado.mailFrom}</code></p>
            </c:if>
            <c:if test="${correoEstado.baseUrlHttps}">
                <p class="small text-muted mb-2">Enlaces: <code>${correoEstado.appBaseUrl}</code></p>
            </c:if>
            <c:if test="${not empty correoEstado.advertencias}">
                <ul class="small text-danger mb-2">
                    <c:forEach items="${correoEstado.advertencias}" var="adv">
                        <li>${adv}</li>
                    </c:forEach>
                </ul>
            </c:if>
            <p class="small text-muted mb-2">
                Job diario 08:00 (Mexico): avisos a 7, 3, 1 y 0 dias del vencimiento + plan vencido.
                Tambien sirve para recuperacion de contraseña.
            </p>
            <div class="d-flex flex-wrap gap-2 align-items-end mb-3">
                <form method="post" action="${pageContext.request.contextPath}/plataforma/correos/probar" class="d-flex flex-wrap gap-2 align-items-end">
                    <%@ include file="../csrf.jspf" %>
                    <div>
                        <label class="form-label small mb-0" for="emailPrueba">Correo de prueba</label>
                        <input type="email" class="form-control form-control-sm" id="emailPrueba" name="emailPrueba"
                               maxlength="150" placeholder="tu@correo.com" value="${emailPrueba}" required
                               ${not correoEstado.smtpConfigurado ? 'disabled' : ''}>
                    </div>
                    <button type="submit" class="btn btn-outline-secondary btn-sm"
                            ${not correoEstado.smtpConfigurado ? 'disabled' : ''}>
                        Enviar prueba
                    </button>
                </form>
                <c:if test="${correoSuscripcionHabilitado}">
                    <form method="post" action="${pageContext.request.contextPath}/plataforma/correos/enviar-avisos">
                        <%@ include file="../csrf.jspf" %>
                        <button type="submit" class="btn btn-outline-primary btn-sm"
                                onclick="return confirm('Enviar avisos de vencimiento pendientes ahora?');">
                            Enviar avisos ahora
                        </button>
                    </form>
                </c:if>
            </div>
            <p class="small mb-0 text-muted">
                Guia Railway: <code>deploy/RAILWAY.md</code> (seccion SMTP) ·
                Checklist: <code>deploy/CHECKLIST_CORREOS.md</code>
            </p>
        </div>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success mt-3">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger mt-3">${mensajeError}</div>
    </c:if>

    <div class="alert alert-info mt-4">
        Revisa <strong>Soporte</strong> cuando un cliente envie una solicitud desde su panel de administrador.
        En <strong>Pagos</strong> puedes expirar solicitudes vencidas (job diario a las 03:30).
    </div>
</div>
</body>
</html>
