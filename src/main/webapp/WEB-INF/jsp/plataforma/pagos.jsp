<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Pagos - Plataforma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav.jspf" %>
<div class="container pb-5">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 class="h3 mb-0">Pagos de suscripcion (todos los clientes)</h1>
        <form method="post" action="${pageContext.request.contextPath}/plataforma/pagos">
            <%@ include file="../csrf.jspf" %>
            <input type="hidden" name="accion" value="expirarVencidos">
            <button type="submit" class="btn btn-outline-warning btn-sm"
                    onclick="return confirm('Marcar como expirados los pagos pendientes fuera de plazo (${diasExpiracionMp} dias MP / ${diasExpiracionManual} dias manual)?');">
                <i class="bi bi-clock-history"></i> Expirar vencidos ahora
            </button>
        </form>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <div class="alert alert-info">
        Los pagos <strong>pendientes</strong> expiran automaticamente tras
        ${diasExpiracionMp} dias (Mercado Pago) o ${diasExpiracionManual} dias (manual).
        Puedes forzar la expiracion de uno en concreto o ejecutar el proceso masivo arriba.
    </div>

    <h2 class="h5 mt-4">Pendientes de confirmacion</h2>
    <c:if test="${empty pagosPendientes}">
        <p class="text-muted">No hay pagos pendientes de confirmacion.</p>
    </c:if>

    <c:if test="${not empty pagosPendientes}">
        <div class="table-responsive mb-4">
            <table class="table table-striped align-middle">
                <thead>
                <tr>
                    <th>Cliente</th>
                    <th>Plan</th>
                    <th>Meses</th>
                    <th>Monto</th>
                    <th>Canal</th>
                    <th>Solicitado</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${pagosPendientes}" var="p">
                    <tr>
                        <td><strong>${p.username}</strong></td>
                        <td>${p.planCodigo}</td>
                        <td>${p.meses}</td>
                        <td>$<fmt:formatNumber value="${p.monto}" minFractionDigits="2"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${p.canal eq 'MERCADOPAGO'}">Mercado Pago</c:when>
                                <c:otherwise>Manual</c:otherwise>
                            </c:choose>
                        </td>
                        <td>${p.fechaSolicitud}</td>
                        <td class="text-nowrap">
                            <form method="post"
                                  action="${pageContext.request.contextPath}/plataforma/pagos"
                                  class="d-inline">
                                <%@ include file="../csrf.jspf" %>
                                <input type="hidden" name="accion" value="confirmar">
                                <input type="hidden" name="pagoId" value="${p.id}">
                                <button type="submit" class="btn btn-sm btn-success">Confirmar</button>
                            </form>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/plataforma/pagos"
                                  class="d-inline ms-1">
                                <%@ include file="../csrf.jspf" %>
                                <input type="hidden" name="accion" value="expirar">
                                <input type="hidden" name="pagoId" value="${p.id}">
                                <button type="submit" class="btn btn-sm btn-outline-secondary"
                                        onclick="return confirm('Marcar este pago como expirado? El cliente podra solicitar uno nuevo.');">
                                    Expirar
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <h2 class="h5 mt-4">Historial expirados <span class="badge bg-secondary">${fn:length(pagosExpirados)}</span></h2>
    <c:if test="${empty pagosExpirados}">
        <p class="text-muted">No hay pagos expirados registrados.</p>
    </c:if>

    <c:if test="${not empty pagosExpirados}">
        <div class="table-responsive">
            <table class="table table-sm table-hover align-middle">
                <thead>
                <tr>
                    <th>Cliente</th>
                    <th>Plan</th>
                    <th>Meses</th>
                    <th>Monto</th>
                    <th>Canal</th>
                    <th>Solicitado</th>
                    <th>Estado</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${pagosExpirados}" var="p">
                    <tr>
                        <td>${p.username}</td>
                        <td>${p.planCodigo}</td>
                        <td>${p.meses}</td>
                        <td>$<fmt:formatNumber value="${p.monto}" minFractionDigits="2"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${p.canal eq 'MERCADOPAGO'}">Mercado Pago</c:when>
                                <c:otherwise>Manual</c:otherwise>
                            </c:choose>
                        </td>
                        <td>${p.fechaSolicitud}</td>
                        <td><span class="badge bg-secondary">Expirado</span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>
</div>
</body>
</html>
