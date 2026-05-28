<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar sesion - Sistema de Ventas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilologin.css">
</head>
<body>

<div class="login-container">
    <div class="login-card shadow-lg">
        <div class="text-center">
            <div class="logo-slot mx-auto mb-3">
                <img src="${pageContext.request.contextPath}/img/logo.png"
                     alt="Logo del negocio"
                     class="logo-img"
                     onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/img/logo-placeholder.svg';">
            </div>
            <div class="titulo-sistema mt-3">
                <h1 class="h4">SISTEMA DE VENTAS</h1>
                <h2 class="h5 fw-bold color-accent">Inicia sesion en tu cuenta</h2>
            </div>
        </div>

        <c:if test="${not empty mensaje}">
            <div class="alert alert-success mt-3">${mensaje}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-danger mt-3">${error}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post" class="mt-4">
            <div class="mb-3 input-group">
                <span class="input-group-text"><i class="bi bi-person-fill"></i></span>
                <input type="text" id="username" name="username" class="form-control" placeholder="Usuario" required>
            </div>

            <div class="mb-4 input-group">
                <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                <input type="password" id="password" name="password" class="form-control" placeholder="Contrasena" required>
            </div>

            <button type="submit" class="btn btn-primary w-100 fw-bold py-2 mb-3">
                INICIAR SESION
            </button>
        </form>

        <p class="text-center mb-0">
            ¿No tienes cuenta?
            <a href="${pageContext.request.contextPath}/registro">Crear cuenta gratis</a>
        </p>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
