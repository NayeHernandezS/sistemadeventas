<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>${cliente.username} - Plataforma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav.jspf" %>
<div class="container pb-5">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <div>
            <a href="${pageContext.request.contextPath}/plataforma/clientes" class="btn btn-sm btn-outline-secondary mb-2">
                <i class="bi bi-arrow-left"></i> Volver a clientes
            </a>
            <h1 class="h3 mb-0">${cliente.username}</h1>
        </div>
    </div>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-5">
            <div class="card shadow-sm">
                <div class="card-header">Resumen de cuenta</div>
                <div class="card-body">
                    <dl class="row mb-0 small">
                        <dt class="col-sm-5">Email</dt>
                        <dd class="col-sm-7">${empty cliente.email ? '—' : cliente.email}</dd>
                        <dt class="col-sm-5">Tipo negocio</dt>
                        <dd class="col-sm-7">${empty cliente.tipoNegocio ? '—' : cliente.tipoNegocio}</dd>
                        <dt class="col-sm-5">Plan</dt>
                        <dd class="col-sm-7">${planNombre} (${cliente.planCodigo})</dd>
                        <dt class="col-sm-5">Vendedores</dt>
                        <dd class="col-sm-7">${cliente.cantidadVendedores}</dd>
                        <dt class="col-sm-5">Vence</dt>
                        <dd class="col-sm-7">
                            <c:choose>
                                <c:when test="${cliente.fechaFinSuscripcion != null}">
                                    ${cliente.fechaFinSuscripcion.format(formatoFecha)}
                                </c:when>
                                <c:otherwise><span class="text-danger">Sin suscripcion</span></c:otherwise>
                            </c:choose>
                        </dd>
                        <dt class="col-sm-5">Estado</dt>
                        <dd class="col-sm-7">
                            <c:choose>
                                <c:when test="${cuentaSuspendida}">
                                    <span class="badge bg-danger">Suspendida</span>
                                </c:when>
                                <c:when test="${cliente.vigente}">
                                    <span class="badge bg-success">Activa</span>
                                    <c:if test="${cliente.enPeriodoPrueba}">
                                        <span class="badge bg-warning text-dark">Prueba</span>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Vencida</span>
                                </c:otherwise>
                            </c:choose>
                        </dd>
                    </dl>
                </div>
            </div>

            <div class="card shadow-sm mt-3">
                <div class="card-header">Acciones</div>
                <div class="card-body d-grid gap-3">
                    <form method="post" action="${pageContext.request.contextPath}/plataforma/clientes"
                          class="d-flex gap-2 align-items-end flex-wrap">
                        <%@ include file="../csrf.jspf" %>
                        <input type="hidden" name="accion" value="extender">
                        <input type="hidden" name="username" value="${cliente.username}">
                        <input type="hidden" name="desdeDetalle" value="true">
                        <div>
                            <label class="form-label small mb-1">Extender (meses)</label>
                            <input type="number" name="meses" class="form-control form-control-sm"
                                   style="width:5rem" min="1" max="24" value="1" required>
                        </div>
                        <button type="submit" class="btn btn-sm btn-primary">Extender</button>
                    </form>

                    <form method="post" action="${pageContext.request.contextPath}/plataforma/clientes"
                          class="d-flex gap-2 align-items-end flex-wrap">
                        <%@ include file="../csrf.jspf" %>
                        <input type="hidden" name="accion" value="cambiarPlan">
                        <input type="hidden" name="username" value="${cliente.username}">
                        <input type="hidden" name="desdeDetalle" value="true">
                        <div class="flex-grow-1">
                            <label class="form-label small mb-1">Cambiar plan</label>
                            <select name="planCodigo" class="form-select form-select-sm" required>
                                <c:forEach items="${planes}" var="plan">
                                    <option value="${plan.codigo}"
                                            ${cliente.planCodigo == plan.codigo ? 'selected' : ''}>
                                        ${plan.nombre} — max ${plan.maxVendedores} vend. / ${plan.maxProductos} prod.
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-sm btn-outline-primary">Aplicar plan</button>
                    </form>

                    <c:choose>
                        <c:when test="${cuentaSuspendida}">
                            <form method="post" action="${pageContext.request.contextPath}/plataforma/clientes">
                                <%@ include file="../csrf.jspf" %>
                                <input type="hidden" name="accion" value="reactivar">
                                <input type="hidden" name="username" value="${cliente.username}">
                                <input type="hidden" name="desdeDetalle" value="true">
                                <button type="submit" class="btn btn-sm btn-success w-100">Reactivar cuenta</button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <form method="post" action="${pageContext.request.contextPath}/plataforma/clientes"
                                  onsubmit="return confirm('Suspender esta cuenta? El cliente no podra usar ventas ni inventario.');">
                                <%@ include file="../csrf.jspf" %>
                                <input type="hidden" name="accion" value="suspender">
                                <input type="hidden" name="username" value="${cliente.username}">
                                <input type="hidden" name="desdeDetalle" value="true">
                                <button type="submit" class="btn btn-sm btn-outline-danger w-100">Suspender cuenta</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="card shadow-sm">
                <div class="card-header">Historial de pagos</div>
                <div class="card-body p-0">
                    <c:if test="${empty pagos}">
                        <p class="text-muted p-3 mb-0">Sin solicitudes de pago registradas.</p>
                    </c:if>
                    <c:if test="${not empty pagos}">
                        <div class="table-responsive">
                            <table class="table table-sm table-striped mb-0 align-middle">
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Fecha</th>
                                    <th>Plan</th>
                                    <th>Meses</th>
                                    <th>Monto</th>
                                    <th>Canal</th>
                                    <th>Estado</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${pagos}" var="p">
                                    <tr>
                                        <td>${p.id}</td>
                                        <td>${p.fechaSolicitud.format(formatoFecha)}</td>
                                        <td>${p.planCodigo}</td>
                                        <td>${p.meses}</td>
                                        <td>$<fmt:formatNumber value="${p.monto}" minFractionDigits="2"/></td>
                                        <td>${p.canal}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.estado eq 'CONFIRMADO'}">
                                                    <span class="badge bg-success">Confirmado</span>
                                                </c:when>
                                                <c:when test="${p.estado eq 'PENDIENTE'}">
                                                    <span class="badge bg-warning text-dark">Pendiente</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">${p.estado}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
