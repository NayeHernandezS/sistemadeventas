<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel plataforma</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body>
<%@ include file="fragmentos/nav.jspf" %>
<div class="container pb-5">
    <h1 class="h3 mb-4">Administracion del SaaS</h1>
    <p class="text-muted">Gestiona las cuentas de tus clientes, confirma pagos y extiende suscripciones.</p>

    <div class="row g-4 mt-2">
        <div class="col-md-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h2 class="h5"><i class="bi bi-people text-primary"></i> Clientes</h2>
                    <p class="display-6 mb-3">${totalClientes}</p>
                    <a href="${pageContext.request.contextPath}/plataforma/clientes" class="btn btn-primary btn-sm">
                        Ver cuentas
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h2 class="h5"><i class="bi bi-credit-card text-warning"></i> Pagos pendientes</h2>
                    <p class="display-6 mb-3">${pagosPendientes}</p>
                    <a href="${pageContext.request.contextPath}/plataforma/pagos" class="btn btn-outline-primary btn-sm">
                        Revisar pagos
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h2 class="h5"><i class="bi bi-headset text-info"></i> Soporte abierto</h2>
                    <p class="display-6 mb-3">${soporteAbiertas}</p>
                    <a href="${pageContext.request.contextPath}/plataforma/soporte" class="btn btn-outline-primary btn-sm">
                        Ver solicitudes
                    </a>
                </div>
            </div>
        </div>
    </div>

    <div class="alert alert-info mt-4">
        Revisa <strong>Soporte</strong> cuando un cliente envie una solicitud desde su panel de administrador.
    </div>
</div>
</body>
</html>
