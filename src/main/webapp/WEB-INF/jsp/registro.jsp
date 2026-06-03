<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crear cuenta - Sistema de Ventas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilologin.css">
    <style>
        .login-container { max-width: 520px; }
        .plan-card { cursor: pointer; border: 2px solid transparent; }
        .plan-card:has(input:checked) { border-color: var(--color-morado, #5B2A86); background: rgba(91,42,134,0.06); }
    </style>
</head>
<body>

<div class="login-container">
    <div class="login-card shadow-lg">
        <div class="text-center">
            <i class="bi bi-person-plus display-4 text-primary"></i>
            <h1 class="h4 mt-3">Crear cuenta de administrador</h1>
            <p class="text-muted small">1 mes gratis con el plan que elijas. Soporte incluido por la creadora del sistema.</p>
        </div>

        <form action="${pageContext.request.contextPath}/registro" method="post" class="mt-4">
            <%@ include file="csrf.jspf" %>
            <div class="mb-3">
                <label for="username" class="form-label">Usuario</label>
                <input type="text" id="username" name="username" class="form-control"
                       value="${username}" required minlength="3">
                <c:if test="${not empty errores.username}">
                    <div class="text-danger small">${errores.username}</div>
                </c:if>
            </div>

            <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input type="email" id="email" name="email" class="form-control"
                       value="${email}" required>
                <c:if test="${not empty errores.email}">
                    <div class="text-danger small">${errores.email}</div>
                </c:if>
            </div>

            <div class="mb-3">
                <label for="tipoNegocio" class="form-label">Tipo de negocio</label>
                <select id="tipoNegocio" name="tipoNegocio" class="form-select" required>
                    <option value="">Selecciona una opcion</option>
                    <c:forEach var="entry" items="${tiposNegocio}">
                        <option value="${entry.key}" ${tipoNegocio == entry.key ? 'selected' : ''}>${entry.value}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errores.tipoNegocio}">
                    <div class="text-danger small">${errores.tipoNegocio}</div>
                </c:if>
            </div>

            <div class="mb-3">
                <label class="form-label">Elige tu plan</label>
                <c:if test="${not empty errores.planCodigo}">
                    <div class="text-danger small mb-2">${errores.planCodigo}</div>
                </c:if>
                <div class="row g-2">
                    <c:forEach items="${planes}" var="plan">
                        <div class="col-12">
                            <label class="card plan-card p-3 mb-0 w-100">
                                <div class="d-flex gap-2 align-items-start">
                                    <input type="radio" name="planCodigo" value="${plan.codigo}"
                                           class="mt-1" ${planCodigo == plan.codigo ? 'checked' : ''} required>
                                    <div>
                                        <strong>${plan.nombre}</strong>
                                        <span class="badge bg-warning text-dark ms-1">$${plan.precioMensual} / mes</span>
                                        <div class="small text-muted mt-1">${plan.descripcion}</div>
                                        <div class="small">Hasta ${plan.maxVendedores} vendedores · ${plan.maxProductos} productos</div>
                                    </div>
                                </div>
                            </label>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <div class="mb-3">
                <label for="password" class="form-label">Contrasena</label>
                <input type="password" id="password" name="password" class="form-control" required minlength="4">
                <c:if test="${not empty errores.password}">
                    <div class="text-danger small">${errores.password}</div>
                </c:if>
            </div>

            <div class="mb-4">
                <label for="confirmarPassword" class="form-label">Confirmar contrasena</label>
                <input type="password" id="confirmarPassword" name="confirmarPassword" class="form-control" required>
                <c:if test="${not empty errores.confirmarPassword}">
                    <div class="text-danger small">${errores.confirmarPassword}</div>
                </c:if>
            </div>

            <div class="border rounded p-3 mb-4 bg-light">
                <p class="small fw-semibold mb-2">Documentos legales (version ${versionLegal})</p>
                <div class="form-check mb-2">
                    <input class="form-check-input" type="checkbox" name="aceptaTerminos" id="aceptaTerminos" value="1"
                           ${aceptaTerminos eq '1' || aceptaTerminos eq 'on' ? 'checked' : ''} required>
                    <label class="form-check-label small" for="aceptaTerminos">
                        Acepto los <a href="${pageContext.request.contextPath}/registro/terminos" target="_blank" rel="noopener">Terminos de servicio</a>
                    </label>
                </div>
                <c:if test="${not empty errores.aceptaTerminos}">
                    <div class="text-danger small mb-2">${errores.aceptaTerminos}</div>
                </c:if>
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="aceptaPrivacidad" id="aceptaPrivacidad" value="1"
                           ${aceptaPrivacidad eq '1' || aceptaPrivacidad eq 'on' ? 'checked' : ''} required>
                    <label class="form-check-label small" for="aceptaPrivacidad">
                        Acepto el <a href="${pageContext.request.contextPath}/registro/privacidad" target="_blank" rel="noopener">Aviso de privacidad</a>
                    </label>
                </div>
                <c:if test="${not empty errores.aceptaPrivacidad}">
                    <div class="text-danger small mt-2">${errores.aceptaPrivacidad}</div>
                </c:if>
            </div>

            <button type="submit" class="btn btn-primary w-100 fw-bold py-2 mb-3">REGISTRARME</button>
        </form>

        <p class="text-center mb-0">
            ¿Ya tienes cuenta?
            <a href="${pageContext.request.contextPath}/login">Iniciar sesion</a>
        </p>
    </div>
</div>

</body>
</html>
