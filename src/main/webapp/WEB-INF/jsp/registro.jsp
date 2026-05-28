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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilologin.css">
</head>
<body>

<div class="login-container">
    <div class="login-card shadow-lg">
        <div class="text-center">
            <i class="bi bi-person-plus display-4 text-primary"></i>
            <h1 class="h4 mt-3">Crear cuenta de administrador</h1>
            <p class="text-muted small">Abre tu negocio en el sistema, obtén <strong>1 mes gratis</strong> y agrega tus vendedores. Tus datos quedan aislados de otras cuentas.</p>
        </div>

        <form action="${pageContext.request.contextPath}/registro" method="post" class="mt-4">
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
