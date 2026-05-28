<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Suscripcion prepago</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow">
                <div class="card-body p-4">
                    <h1 class="h3 mb-3">Plan prepago</h1>

                    <c:if test="${requierePago}">
                        <div class="alert alert-warning">
                            Tu periodo de prueba o suscripcion ha vencido. Renueva para seguir usando el sistema.
                        </div>
                    </c:if>

                    <c:if test="${not empty mensajeExito}">
                        <div class="alert alert-success">${mensajeExito}</div>
                    </c:if>

                    <c:if test="${not empty errores.general}">
                        <div class="alert alert-danger">${errores.general}</div>
                    </c:if>

                    <c:if test="${not empty suscripcion}">
                        <p><strong>Estado:</strong>
                            <c:choose>
                                <c:when test="${suscripcion.enPeriodoPrueba}">Mes de prueba gratis</c:when>
                                <c:otherwise>Plan prepago activo</c:otherwise>
                            </c:choose>
                        </p>
                        <p><strong>Vigente hasta:</strong> ${fechaFinTexto}</p>
                    </c:if>

                    <hr>
                    <h2 class="h5">Contratar meses</h2>
                    <p class="text-muted">Precio: $<fmt:formatNumber value="${precioMes}" minFractionDigits="2"/> MXN por mes</p>

                    <form method="post" action="${pageContext.request.contextPath}/suscripcion" class="row g-3">
                        <div class="col-md-6">
                            <label for="meses" class="form-label">Cantidad de meses (1 a 24)</label>
                            <input type="number" class="form-control" id="meses" name="meses"
                                   min="1" max="24" value="1" required>
                            <c:if test="${not empty errores.meses}">
                                <div class="text-danger small">${errores.meses}</div>
                            </c:if>
                        </div>
                        <div class="col-12">
                            <button type="submit" class="btn btn-primary">Solicitar pago</button>
                            <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary ms-2">Volver al panel</a>
                        </div>
                    </form>

                    <p class="small text-muted mt-3">
                        Al solicitar, queda un pago <strong>PENDIENTE</strong>. El administrador lo confirma y se extiende tu acceso.
                    </p>

                    <c:if test="${not empty pagos}">
                        <hr>
                        <h3 class="h6">Historial de solicitudes</h3>
                        <table class="table table-sm">
                            <thead>
                            <tr>
                                <th>Fecha</th>
                                <th>Meses</th>
                                <th>Monto</th>
                                <th>Estado</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${pagos}" var="p">
                                <tr>
                                    <td>${p.fechaSolicitud}</td>
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
