<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - Sistema de Ventas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="bg-light pagina-error">
<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card shadow-sm">
                <div class="card-body p-4 text-center">
                    <h1 class="display-6 mb-3">Error ${status}</h1>
                    <p class="text-muted mb-4">${mensaje}</p>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/">Volver al inicio</a>
                </div>
            </div>
        </div>
    </div>
</main>
</body>
</html>
