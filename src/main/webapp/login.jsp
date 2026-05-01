<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Ferretería Branette</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="css/estilologin.css">
</head>
<body>

<div class="login-container">
    <div class="login-card shadow-lg">
        <div class="text-center">
            <img src="logoferreteria.png" alt="Logo" class="logo-img mb-3">
            <div class="titulo-ferreteria">
                <h1 class="h4">TLAPALERÍA Y FERRETERÍA</h1>
                <h2 class="h3 fw-bold color-accent">BRANETTE</h2>
            </div>
        </div>

        <form action="/ferreteria/login" method="post" class="mt-4">
            <div class="mb-3 input-group">
                <span class="input-group-text"><i class="bi bi-person-fill"></i></span>
                <input type="text" id="username" name="username" class="form-control" placeholder="Usuario" required>
            </div>

            <div class="mb-4 input-group">
                <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                <input type="password" id="password" name="password" class="form-control" placeholder="Contraseña" required>
            </div>

            <div class="mb-3 input-group">

            <button type="submit" class="btn btn-primary w-100 fw-bold py-2" name="enviar">
                INICIAR SESIÓN
            </button>

            </div>

            <a class="btn btn-primary" href="${pageContext.request.contextPath}/index.jsp">volver</a>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>