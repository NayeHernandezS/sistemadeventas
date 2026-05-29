<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Planes y suscripcion</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <style>
        .plan-card { cursor: pointer; border: 2px solid transparent; }
        .plan-card:has(input:checked) { border-color: #5B2A86; background: rgba(91,42,134,0.06); }
    </style>
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-10">
            <div class="card shadow">
                <div class="card-body p-4">
                    <h1 class="h3 mb-3">Planes y suscripcion</h1>

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

                    <form method="post" action="${pageContext.request.contextPath}/suscripcion">
                        <%@ include file="csrf.jspf" %>
                        <div class="row g-3 mb-3">
                            <c:forEach items="${planes}" var="plan">
                                <div class="col-md-4">
                                    <label class="card plan-card h-100 p-3">
                                        <input type="radio" name="planCodigo" value="${plan.codigo}" class="mb-2"
                                               ${planCodigoSeleccion == plan.codigo ? 'checked' : ''} required>
                                        <h3 class="h6">${plan.nombre}</h3>
                                        <p class="mb-1"><strong>$<fmt:formatNumber value="${plan.precioMensual}" minFractionDigits="0"/> / mes</strong></p>
                                        <p class="small text-muted mb-2">${plan.descripcion}</p>
                                        <ul class="small mb-0">
                                            <li>${plan.maxVendedores} vendedores</li>
                                            <li>${plan.maxProductos} productos</li>
                                            <li>Soporte incluido</li>
                                        </ul>
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
                                <button type="submit" class="btn btn-primary">Solicitar pago</button>
                                <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary ms-2">Volver</a>
                            </div>
                        </div>
                    </form>

                    <p class="small text-muted mt-3">
                        La solicitud queda <strong>PENDIENTE</strong> hasta que se confirme el pago y se active tu plan.
                    </p>

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
                                    <td>${p.estado}</td>
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
