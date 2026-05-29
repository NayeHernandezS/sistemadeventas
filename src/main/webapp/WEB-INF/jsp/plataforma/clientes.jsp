<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Clientes - Plataforma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav.jspf" %>
<div class="container pb-5">
    <h1 class="h3 mb-3">Cuentas de clientes</h1>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <c:if test="${empty clientes}">
        <p class="text-muted">Aun no hay cuentas de negocio registradas.</p>
    </c:if>

    <c:if test="${not empty clientes}">
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle">
                <thead>
                <tr>
                    <th>Cuenta</th>
                    <th>Email</th>
                    <th>Tipo negocio</th>
                    <th>Plan</th>
                    <th>Vendedores</th>
                    <th>Vence</th>
                    <th>Estado</th>
                    <th>Soporte</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${clientes}" var="c">
                    <tr>
                        <td><strong>${c.username}</strong></td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty c.email}">
                                    <a href="mailto:${c.email}">${c.email}</a>
                                </c:when>
                                <c:otherwise><span class="text-muted">—</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>${empty c.tipoNegocio ? '—' : c.tipoNegocio}</td>
                        <td>${empty c.planCodigo ? '—' : c.planCodigo}</td>
                        <td>${c.cantidadVendedores}</td>
                        <td>
                            <c:choose>
                                <c:when test="${c.fechaFinSuscripcion != null}">
                                    ${c.fechaFinSuscripcion.format(formatoFecha)}
                                </c:when>
                                <c:otherwise><span class="text-danger">Sin suscripcion</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${c.vigente}">
                                    <span class="badge bg-success">Activa</span>
                                    <c:if test="${c.enPeriodoPrueba}"><span class="badge bg-warning text-dark">Prueba</span></c:if>
                                </c:when>
                                <c:otherwise><span class="badge bg-secondary">Vencida</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/plataforma/clientes/extender"
                                  class="d-flex gap-1 flex-wrap">
                                <%@ include file="../csrf.jspf" %>
                                <input type="hidden" name="username" value="${c.username}">
                                <input type="number" name="meses" class="form-control form-control-sm"
                                       style="width:4.5rem" min="1" max="24" value="1" title="Meses a extender">
                                <button type="submit" class="btn btn-sm btn-primary">Extender</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>
</div>
</body>
</html>
