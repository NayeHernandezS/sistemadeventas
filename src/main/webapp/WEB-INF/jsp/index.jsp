<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Sistema de Ventas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estiloindex.css">
</head>
<body>

<%@ include file="fragmentos/nav-tenant.jspf" %>

<main class="container my-5">
    <div class="text-center mb-5">
        <div class="logo-slot logo-slot--hero mx-auto mb-3">
            <c:set var="logoCssClass" value="logo-hero" scope="request"/>
            <%@ include file="fragmentos/logo-tenant.jspf" %>
        </div>
        <h2 class="hero-title mb-2">Panel de ventas</h2>
        <p class="text-muted">Gestiona tu catalogo, ventas y reportes desde tu cuenta.</p>
        <c:if test="${param.sinPlan eq '1' && sessionScope.rol ne 'ADMIN'}">
            <div class="alert alert-warning mt-3 mx-auto" style="max-width: 520px;">
                La suscripcion del negocio no esta activa. Pide al administrador de tu cuenta que renueve el plan.
            </div>
        </c:if>
        <c:if test="${sessionScope.rol eq 'ADMIN'}">
        <c:if test="${not empty avisoSuscripcion}">
            <div class="alert alert-${avisoSuscripcion.nivel} mt-3 mx-auto text-start" style="max-width: 520px;">
                <i class="bi bi-exclamation-triangle"></i> ${avisoSuscripcion.mensaje}
                <a href="${pageContext.request.contextPath}/suscripcion" class="alert-link ms-1">Renovar ahora</a>
            </div>
        </c:if>
        <div class="alert alert-info mt-3 mx-auto text-start" style="max-width: 520px;">
            <strong>Plan:</strong> ${planNombre}<br>
            <span class="small">Vendedores: ${vendedoresUsados} / ${vendedoresMax} ·
            Productos: ${productosUsados} / ${productosMax}</span>
        </div>
        <a href="${pageContext.request.contextPath}/suscripcion" class="btn btn-outline-primary btn-sm mt-2">
            Ver planes y suscripcion
        </a>
        <c:if test="${cantidadConAlerta > 0}">
        <div class="alert alert-warning mt-3 mx-auto text-start" style="max-width: 520px;">
            <i class="bi bi-exclamation-triangle"></i>
            <strong>Inventario:</strong>
            ${cantidadConAlerta} producto(s) requieren atencion
            (<c:if test="${cantidadAgotados > 0}">${cantidadAgotados} agotado(s)</c:if><c:if test="${cantidadAgotados > 0 && cantidadStockBajo > 0}">, </c:if><c:if test="${cantidadStockBajo > 0}">${cantidadStockBajo} stock bajo</c:if>).
            Umbral: ${stockMinimo} uds.
            <a href="${pageContext.request.contextPath}/crudprod" class="alert-link">Ver inventario</a>
        </div>
        </c:if>
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
                <div class="icon-circle"><i class="bi bi-arrow-return-left"></i></div>
                <a href="${pageContext.request.contextPath}/devoluciones" class="menu-link stretched-link">Devoluciones</a>
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
            <div class="card h-100 menu-card p-4 border-start border-success border-3">
                <div class="icon-circle"><i class="bi bi-tags"></i></div>
                <a href="${pageContext.request.contextPath}/categorias" class="menu-link stretched-link">Categorias</a>
            </div>
        </div>
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-warning border-3">
                <div class="icon-circle"><i class="bi bi-people"></i></div>
                <a href="${pageContext.request.contextPath}/usuarios" class="menu-link stretched-link">Mis vendedores</a>
            </div>
        </div>
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-primary border-3">
                <div class="icon-circle"><i class="bi bi-credit-card"></i></div>
                <a href="${pageContext.request.contextPath}/admin/pagos" class="menu-link stretched-link">Estado de mis pagos</a>
            </div>
        </div>
        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-info border-3">
                <div class="icon-circle"><i class="bi bi-headset"></i></div>
                <a href="${pageContext.request.contextPath}/soporte" class="menu-link stretched-link">Soporte</a>
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
