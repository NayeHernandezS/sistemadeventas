<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Mi perfil</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<div class="container my-5" style="max-width: 520px;">
    <h1 class="h3 mb-4">Mi perfil</h1>
    <p class="text-muted">Cuenta: <strong>${username}</strong></p>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <div class="card shadow-sm">
        <div class="card-header">Cambiar contraseña</div>
        <div class="card-body">
            <form method="post" action="${pageContext.request.contextPath}/perfil/password">
                <%@ include file="csrf.jspf" %>
                <div class="mb-3">
                    <label for="passwordActual" class="form-label">Contraseña actual</label>
                    <input type="password" class="form-control" id="passwordActual" name="passwordActual" required>
                    <c:if test="${not empty errores.passwordActual}">
                        <div class="text-danger small">${errores.passwordActual}</div>
                    </c:if>
                </div>
                <div class="mb-3">
                    <label for="passwordNueva" class="form-label">Nueva contraseña</label>
                    <input type="password" class="form-control" id="passwordNueva" name="passwordNueva"
                           minlength="4" required>
                    <c:if test="${not empty errores.passwordNueva}">
                        <div class="text-danger small">${errores.passwordNueva}</div>
                    </c:if>
                </div>
                <div class="mb-3">
                    <label for="passwordConfirmacion" class="form-label">Confirmar nueva contraseña</label>
                    <input type="password" class="form-control" id="passwordConfirmacion" name="passwordConfirmacion"
                           minlength="4" required>
                    <c:if test="${not empty errores.passwordConfirmacion}">
                        <div class="text-danger small">${errores.passwordConfirmacion}</div>
                    </c:if>
                </div>
                <button type="submit" class="btn btn-primary">Guardar</button>
                <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary">Volver</a>
            </form>
        </div>
    </div>
</div>
</body>
</html>
