<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Sistema de Ventas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estiloindex.css">
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm py-3">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${pageContext.request.contextPath}/">
            <img src="${pageContext.request.contextPath}/img/logo.png"
                 alt="Logo del negocio"
                 class="navbar-logo"
                 onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/img/logo-placeholder.svg';">
            <span class="fw-bold fs-4 text-primary">Mi<span class="text-warning">Sistema</span> de Ventas</span>
        </a>
        <div class="ms-auto d-flex align-items-center gap-2">
            <span class="text-muted small d-none d-md-inline">Hola, ${sessionScope.username}</span>
            <a class="btn btn-outline-danger btn-sm rounded-pill px-3" href="${pageContext.request.contextPath}/logout">
                Cerrar sesion <i class="bi bi-power"></i>
            </a>
        </div>
    </div>
</nav>

<main class="container my-5">
    <div class="text-center mb-5">
        <div class="logo-slot logo-slot--hero mx-auto mb-3">
            <img src="${pageContext.request.contextPath}/img/logo.png"
                 alt="Logo del negocio"
                 class="logo-hero"
                 onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/img/logo-placeholder.svg';">
        </div>
        <h2 class="hero-title mb-2">Panel de ventas</h2>
        <p class="text-muted">Gestiona tu catalogo, ventas y reportes desde tu cuenta.</p>
        <c:if test="${param.sinPlan eq '1' && sessionScope.rol ne 'ADMIN'}">
            <div class="alert alert-warning mt-3 mx-auto" style="max-width: 520px;">
                La suscripcion del negocio no esta activa. Pide al administrador de tu cuenta que renueve el plan.
            </div>
        </c:if>
        <c:if test="${sessionScope.rol eq 'ADMIN'}">
        <a href="${pageContext.request.contextPath}/suscripcion" class="btn btn-outline-primary btn-sm mt-2">
            Ver mi suscripcion prepago
        </a>
        </c:if>
    </div>

    <div class="row g-4 justify-content-center text-center">

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-cart-plus"></i></div>
                <a href="${pageContext.request.contextPath}/productos" class="menu-link stretched-link">Modulo de ventas</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-box-seam"></i></div>
                <a href="${pageContext.request.contextPath}/crudprod" class="menu-link stretched-link">Mi inventario</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-ticket-detailed"></i></div>
                <a href="${pageContext.request.contextPath}/tickets" class="menu-link stretched-link">Mis tickets</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-calendar2-check"></i></div>
                <a href="${pageContext.request.contextPath}/reportes" class="menu-link stretched-link">Reportes</a>
            </div>
        </div>

        <c:if test="${sessionScope.rol eq 'ADMIN'}">
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-warning border-3">
                <div class="icon-circle"><i class="bi bi-people"></i></div>
                <a href="${pageContext.request.contextPath}/usuarios" class="menu-link stretched-link">Mis vendedores</a>
            </div>
        </div>
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-success border-3">
                <div class="icon-circle"><i class="bi bi-credit-card"></i></div>
                <a href="${pageContext.request.contextPath}/admin/pagos" class="menu-link stretched-link">Mis pagos pendientes</a>
            </div>
        </div>
        </c:if>

    </div>
</main>

<footer>
    <div class="container py-4 text-center">
        <p class="small mb-0 text-secondary">&copy; 2026 Sistema de Ventas. Cada cuenta gestiona su propio negocio.</p>
    </div>
</footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
