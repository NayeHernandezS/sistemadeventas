<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crear cuenta - FUSION DIGITAL</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilologin.css">
</head>
<body class="pagina-auth pagina-auth--registro">

<div class="login-container">
    <div class="login-card shadow-lg">
        <div class="text-center mb-4">
            <i class="bi bi-person-plus display-4 text-primary"></i>
            <h1 class="h4 mt-3 mb-1">Crear cuenta de administrador</h1>
            <p class="text-muted small mb-0">Tras registrarte, inicias sesion y eliges tu plan en el panel (1 mes gratis).</p>
        </div>

        <c:if test="${not empty errores.general}">
            <div class="alert alert-danger py-2">${errores.general}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/registro" method="post" autocomplete="on">
            <%@ include file="csrf.jspf" %>

            <fieldset class="border-0 p-0 m-0">
                <legend class="form-label fw-semibold fs-6 mb-3">Tu cuenta</legend>

                <div class="mb-3">
                    <label for="username" class="form-label">Usuario</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-person-fill"></i></span>
                        <input type="text" id="username" name="username" class="form-control"
                               value="${username}" required minlength="3" maxlength="100"
                               autocomplete="username" autocapitalize="off" spellcheck="false"
                               placeholder="Ej. mi_tienda">
                    </div>
                    <c:if test="${not empty errores.username}">
                        <div class="text-danger small mt-1">${errores.username}</div>
                    </c:if>
                </div>

                <div class="mb-3">
                    <label for="email" class="form-label">Email</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-envelope-fill"></i></span>
                        <input type="email" id="email" name="email" class="form-control"
                               value="${email}" required autocomplete="email"
                               placeholder="tu@correo.com">
                    </div>
                    <c:if test="${not empty errores.email}">
                        <div class="text-danger small mt-1">${errores.email}</div>
                    </c:if>
                </div>

                <div class="mb-3">
                    <label for="password" class="form-label">Contrasena</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                        <input type="password" id="password" name="password" class="form-control"
                               required minlength="4" autocomplete="new-password"
                               placeholder="Minimo 4 caracteres">
                    </div>
                    <c:if test="${not empty errores.password}">
                        <div class="text-danger small mt-1">${errores.password}</div>
                    </c:if>
                </div>

                <div class="mb-4">
                    <label for="confirmarPassword" class="form-label">Confirmar contrasena</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                        <input type="password" id="confirmarPassword" name="confirmarPassword"
                               class="form-control" required autocomplete="new-password">
                    </div>
                    <c:if test="${not empty errores.confirmarPassword}">
                        <div class="text-danger small mt-1">${errores.confirmarPassword}</div>
                    </c:if>
                </div>
            </fieldset>

            <fieldset class="border-0 p-0 m-0 mb-4">
                <legend class="form-label fw-semibold fs-6 mb-3">Tu negocio</legend>

                <div class="mb-3">
                    <label for="tipoNegocio" class="form-label">Tipo de negocio</label>
                    <select id="tipoNegocio" name="tipoNegocio" class="form-select" required>
                        <option value="">Selecciona una opcion</option>
                        <c:forEach var="entry" items="${tiposNegocio}">
                            <option value="${entry.key}" ${tipoNegocio == entry.key ? 'selected' : ''}>${entry.value}</option>
                        </c:forEach>
                    </select>
                    <c:if test="${not empty errores.tipoNegocio}">
                        <div class="text-danger small mt-1">${errores.tipoNegocio}</div>
                    </c:if>
                </div>

            </fieldset>

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
            ·
            <a href="${pageContext.request.contextPath}/">Sitio</a>
        </p>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
