<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recuperar contraseña</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilologin.css">
</head>
<body>
<div class="login-container">
    <div class="login-card shadow-lg">
        <h1 class="h4 text-center mb-3">Recuperar contraseña</h1>
        <p class="text-muted small text-center">
            Escribe el correo de tu cuenta. Te enviaremos un enlace para crear una nueva contraseña.
        </p>

        <c:if test="${not empty mensajeExito}">
            <div class="alert alert-success">${mensajeExito}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>
        <c:if test="${not empty enlaceDemo}">
            <div class="alert alert-info small">
                <strong>Modo demo (sin SMTP):</strong> usa este enlace para restablecer la contraseña:<br>
                <a href="${enlaceDemo}">${enlaceDemo}</a>
            </div>
        </c:if>

        <c:if test="${empty mensajeExito}">
            <form method="post" action="${pageContext.request.contextPath}/recuperar" class="mt-3">
                <%@ include file="csrf.jspf" %>
                <div class="mb-3">
                    <label for="email" class="form-label">Correo de la cuenta</label>
                    <input type="email" class="form-control" id="email" name="email"
                           value="${email}" required autofocus>
                </div>
                <button type="submit" class="btn btn-primary w-100">Enviar enlace</button>
            </form>
        </c:if>

        <p class="text-center mt-3 mb-0">
            <a href="${pageContext.request.contextPath}/login">Volver al login</a>
        </p>
    </div>
</div>
</body>
</html>
