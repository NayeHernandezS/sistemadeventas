<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nueva contraseña</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilologin.css">
</head>
<body class="pagina-auth pagina-auth--centrada">
<div class="login-container">
    <div class="login-card shadow-lg">
        <h1 class="h4 text-center mb-3">Nueva contraseña</h1>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/recuperar/restablecer">
            <%@ include file="csrf.jspf" %>
            <input type="hidden" name="token" value="${token}">
            <div class="mb-3">
                <label for="passwordNueva" class="form-label">Nueva contraseña</label>
                <input type="password" class="form-control" id="passwordNueva" name="passwordNueva"
                       minlength="4" required autofocus>
                <c:if test="${not empty errores.passwordNueva}">
                    <div class="text-danger small">${errores.passwordNueva}</div>
                </c:if>
            </div>
            <div class="mb-3">
                <label for="passwordConfirmacion" class="form-label">Confirmar contraseña</label>
                <input type="password" class="form-control" id="passwordConfirmacion" name="passwordConfirmacion"
                       minlength="4" required>
                <c:if test="${not empty errores.passwordConfirmacion}">
                    <div class="text-danger small">${errores.passwordConfirmacion}</div>
                </c:if>
            </div>
            <button type="submit" class="btn btn-primary w-100">Guardar contraseña</button>
        </form>

        <p class="text-center mt-3 mb-0">
            <a href="${pageContext.request.contextPath}/login">Volver al login</a>
        </p>
    </div>
</div>
</body>
</html>
