<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Planes y suscripcion</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <style>
        .plan-card { cursor: pointer; border: 2px solid transparent; }
        .plan-card:has(input:checked) { border-color: #5B2A86; background: rgba(91,42,134,0.06); }
        .plan-card--bloqueado {
            cursor: not-allowed;
            opacity: 0.72;
            background: #f8f9fa;
        }
        .plan-card--bloqueado:has(input:checked) {
            border-color: #dc3545;
            background: rgba(220,53,69,0.06);
        }
    </style>
</head>
<body class="bg-light">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-10">
            <div class="card shadow">
                <div class="card-body p-4">
                    <h1 class="h3 mb-3">Planes y suscripcion</h1>

                    <c:if test="${not empty avisoSuscripcion}">
                        <div class="alert alert-${avisoSuscripcion.nivel}">
                            ${avisoSuscripcion.mensaje}
                        </div>
                    </c:if>

                    <c:if test="${requierePago}">
                        <div class="alert alert-warning">
                            Tu periodo de prueba o suscripcion ha vencido. Contrata un plan para seguir usando el sistema.
                        </div>
                    </c:if>

                    <c:if test="${not empty mensajeExito}">
                        <div class="alert alert-success">${mensajeExito}</div>
                    </c:if>

                    <c:if test="${not empty errores.general}">
                        <div class="alert alert-danger">${errores.general}</div>
                    </c:if>

                    <c:if test="${not empty checkoutsAbandonadosMp}">
                        <div class="alert alert-warning">
                            Abandonaste el checkout de Mercado Pago: no se registro ningun cobro.
                            <c:if test="${not empty suscripcion}">
                                Tu plan ya esta activo; no necesitas pagar de nuevo.
                            </c:if>
                            Cancela la solicitud pendiente en el historial o en
                            <a href="${pageContext.request.contextPath}/admin/pagos" class="alert-link">Estado de mis pagos</a>.
                        </div>
                    </c:if>

                    <c:if test="${not empty pagosPendientes && empty checkoutsAbandonadosMp}">
                        <div class="alert alert-warning">
                            Tienes al menos un pago pendiente. Cancelalo o espera confirmacion
                            antes de solicitar otro.
                            <a href="${pageContext.request.contextPath}/admin/pagos" class="alert-link">Ver detalle</a>
                        </div>
                    </c:if>

                    <c:if test="${not empty suscripcion}">
                        <div class="alert alert-info">
                            <strong>Plan actual:</strong> ${planActivoNombre}
                            · <strong>Vigente hasta:</strong> ${fechaFinTexto}
                            <br>
                            <span class="small">Uso: ${vendedoresUsados} / ${planActivo.maxVendedores} vendedores,
                            ${productosUsados} / ${planActivo.maxProductos} productos</span>
                        </div>
                    </c:if>

                    <h2 class="h5">Contratar o cambiar de plan</h2>
                    <p class="text-muted">Todos los planes incluyen soporte directo con la creadora del sistema.</p>

                    <c:set var="hayPlanesBloqueados" value="false"/>
                    <c:forEach items="${planesContratibilidad}" var="entry">
                        <c:if test="${!entry.value.contratable}">
                            <c:set var="hayPlanesBloqueados" value="true"/>
                        </c:if>
                    </c:forEach>
                    <c:if test="${hayPlanesBloqueados}">
                        <div class="alert alert-secondary small">
                            <strong>No puedes bajar a algunos planes</strong> porque tu cuenta usa mas vendedores o
                            productos de los que permiten. Revisa las tarjetas marcadas o ajusta tu uso:
                            <a href="${pageContext.request.contextPath}/usuarios">Usuarios</a> ·
                            <a href="${pageContext.request.contextPath}/crudprod">Inventario</a>
                        </div>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/suscripcion">
                        <%@ include file="csrf.jspf" %>
                        <div class="row g-3 mb-3">
                            <c:forEach items="${planes}" var="plan">
                                <c:set var="evalPlan" value="${planesContratibilidad[plan.codigo]}"/>
                                <div class="col-md-4">
                                    <label class="card plan-card h-100 p-3 ${!evalPlan.contratable ? 'plan-card--bloqueado' : ''}">
                                        <input type="radio" name="planCodigo" value="${plan.codigo}" class="mb-2"
                                               ${planCodigoSeleccion == plan.codigo ? 'checked' : ''}
                                               ${!evalPlan.contratable ? 'disabled' : ''} required>
                                        <h3 class="h6 mb-1">
                                            ${plan.nombre}
                                            <c:if test="${planActivo.codigo == plan.codigo}">
                                                <span class="badge bg-primary">Actual</span>
                                            </c:if>
                                        </h3>
                                        <p class="mb-1"><strong>$<fmt:formatNumber value="${plan.precioMensual}" minFractionDigits="0"/> / mes</strong></p>
                                        <p class="small text-muted mb-2">${plan.descripcion}</p>
                                        <ul class="small mb-2">
                                            <li>${plan.maxVendedores} vendedores</li>
                                            <li>${plan.maxProductos} productos</li>
                                            <li>Soporte incluido</li>
                                        </ul>
                                        <c:if test="${!evalPlan.contratable}">
                                            <div class="alert alert-warning py-2 px-2 mb-0 small">
                                                <strong>No disponible con tu uso actual</strong>
                                                <ul class="mb-0 ps-3">
                                                    <c:forEach items="${evalPlan.motivos}" var="motivo">
                                                        <li>${motivo}</li>
                                                    </c:forEach>
                                                </ul>
                                            </div>
                                        </c:if>
                                    </label>
                                </div>
                            </c:forEach>
                        </div>
                        <c:if test="${not empty errores.planCodigo}">
                            <div class="text-danger small mb-2">${errores.planCodigo}</div>
                        </c:if>

                        <div class="row g-3 align-items-end">
                            <div class="col-md-4">
                                <label for="meses" class="form-label">Meses a pagar (1-24)</label>
                                <input type="number" class="form-control" id="meses" name="meses"
                                       min="1" max="24" value="1" required>
                                <c:if test="${not empty errores.meses}">
                                    <div class="text-danger small">${errores.meses}</div>
                                </c:if>
                            </div>
                            <div class="col-md-8">
                                <c:choose>
                                    <c:when test="${mercadoPagoHabilitado}">
                                        <button type="submit" class="btn btn-primary">
                                            Pagar con Mercado Pago
                                        </button>
                                        <button type="submit" class="btn btn-outline-secondary ms-2" name="manual" value="1">
                                            Solicitar pago manual
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="submit" class="btn btn-primary">Solicitar pago</button>
                                    </c:otherwise>
                                </c:choose>
                                <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary ms-2">Volver</a>
                            </div>
                        </div>
                    </form>

                    <p class="small text-muted mt-3">
                        <c:choose>
                            <c:when test="${mercadoPagoHabilitado}">
                                Con Mercado Pago puedes pagar con tarjeta, transferencia SPEI u OXXO (segun disponibilidad).
                                El plan se activa automaticamente al confirmar el pago.
                                Los pagos pendientes expiran tras 15 dias (MP) o 30 dias (manual) sin confirmacion.
                            </c:when>
                            <c:otherwise>
                                La solicitud queda <strong>PENDIENTE</strong> hasta que la plataforma confirme el pago recibido
                                (expira tras ~30 dias sin confirmacion).
                            </c:otherwise>
                        </c:choose>
                        Consulta el estado en <a href="${pageContext.request.contextPath}/admin/pagos">Estado de mis pagos</a>.
                    </p>

                    <c:if test="${renovacionAutomaticaDisponible}">
                        <hr>
                        <h3 class="h6">Renovacion automatica mensual (Mercado Pago)</h3>
                        <c:if test="${not empty suscripcion && suscripcion.renovacionAutomatica}">
                            <div class="alert alert-success">
                                Tienes renovacion automatica activa. Se cobrara cada mes el plan
                                <strong>${planActivoNombre}</strong>.
                            </div>
                            <form method="post" action="${pageContext.request.contextPath}/suscripcion/auto-renovar/cancelar" class="d-inline">
                                <%@ include file="csrf.jspf" %>
                                <button type="submit" class="btn btn-outline-danger btn-sm"
                                        onclick="return confirm('Cancelar el cobro automatico mensual?');">
                                    Cancelar renovacion automatica
                                </button>
                            </form>
                        </c:if>
                        <c:if test="${empty suscripcion || !suscripcion.renovacionAutomatica}">
                            <p class="small text-muted">
                                Autoriza un cargo mensual en Mercado Pago para no tener que renovar manualmente.
                            </p>
                            <form method="post" action="${pageContext.request.contextPath}/suscripcion/auto-renovar" class="row g-2 align-items-end">
                                <%@ include file="csrf.jspf" %>
                                <div class="col-md-6">
                                    <label for="planAutoRenovar" class="form-label">Plan a cobrar cada mes</label>
                                    <select class="form-select" id="planAutoRenovar" name="planCodigo" required>
                                        <c:forEach items="${planes}" var="plan">
                                            <c:set var="evalPlan" value="${planesContratibilidad[plan.codigo]}"/>
                                            <option value="${plan.codigo}"
                                                    ${planActivo.codigo == plan.codigo ? 'selected' : ''}
                                                    ${!evalPlan.contratable ? 'disabled' : ''}>
                                                ${plan.nombre} — $<fmt:formatNumber value="${plan.precioMensual}" minFractionDigits="0"/>/mes
                                                <c:if test="${!evalPlan.contratable}"> (no disponible)</c:if>
                                            </option>
                                        </c:forEach>
                                    </select>
                                    <c:if test="${hayPlanesBloqueados}">
                                        <div class="form-text">
                                            Los planes no disponibles exceden tus vendedores o productos actuales.
                                        </div>
                                    </c:if>
                                </div>
                                <div class="col-md-6">
                                    <button type="submit" class="btn btn-outline-primary">
                                        Activar renovacion automatica
                                    </button>
                                </div>
                            </form>
                        </c:if>
                    </c:if>

                    <c:if test="${not empty pagos}">
                        <hr>
                        <h3 class="h6">Historial de solicitudes</h3>
                        <table class="table table-sm">
                            <thead>
                            <tr>
                                <th>Fecha</th>
                                <th>Plan</th>
                                <th>Meses</th>
                                <th>Monto</th>
                                <th>Estado</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${pagos}" var="p">
                                <tr>
                                    <td>${p.fechaSolicitud}</td>
                                    <td>${p.planCodigo}</td>
                                    <td>${p.meses}</td>
                                    <td>$<fmt:formatNumber value="${p.monto}" minFractionDigits="2"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.estado eq 'CONFIRMADO'}">
                                                <span class="badge bg-success">Confirmado</span>
                                            </c:when>
                                            <c:when test="${p.estado eq 'EXPIRADO'}">
                                                <span class="badge bg-secondary">Expirado</span>
                                            </c:when>
                                            <c:when test="${p.estado eq 'PENDIENTE'}">
                                                <span class="badge bg-warning text-dark">Pendiente</span>
                                                <form method="post" action="${pageContext.request.contextPath}/suscripcion/pago/cancelar"
                                                      class="d-inline ms-1">
                                                    <%@ include file="csrf.jspf" %>
                                                    <input type="hidden" name="pagoId" value="${p.id}">
                                                    <button type="submit" class="btn btn-link btn-sm p-0 align-baseline"
                                                            onclick="return confirm('Cancelar esta solicitud pendiente?');">
                                                        Cancelar
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>${p.estado}</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
