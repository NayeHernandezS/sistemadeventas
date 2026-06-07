<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="FUSION DIGITAL: vende, controla inventario y emite tickets desde el navegador. Ideal para PYMES en Mexico.">
    <title>FUSION DIGITAL — Sistema de ventas para tu negocio</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/landing.css">
</head>
<body>

<nav class="navbar navbar-expand-lg landing-nav py-3 sticky-top">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">
            <span class="brand-accent">FUSION</span> DIGITAL
        </a>
        <div class="d-flex gap-2 ms-auto">
            <a class="btn btn-outline-light btn-sm rounded-pill px-3" href="${pageContext.request.contextPath}/login">
                Iniciar sesion
            </a>
            <a class="btn btn-warning btn-sm rounded-pill px-3 fw-semibold" href="${pageContext.request.contextPath}/registro">
                Probar gratis
            </a>
        </div>
    </div>
</nav>

<section class="landing-hero">
    <div class="container">
        <div class="row align-items-center g-4">
            <div class="col-lg-7">
                <span class="landing-hero__badge">
                    <i class="bi bi-gift"></i> ${mesesGratis} mes${mesesGratis == 1 ? '' : 'es'} de prueba gratis
                </span>
                <h1 class="mb-3">Tu negocio vende mejor desde el navegador</h1>
                <p class="landing-hero__lead mb-4">
                    Catalogo, inventario, tickets, reportes y equipo de vendedores en una sola plataforma.
                    Pensado para tiendas y negocios de servicios en Mexico.
                </p>
                <div class="d-flex flex-wrap gap-2">
                    <a class="btn btn-primary btn-lg rounded-pill px-4" href="${pageContext.request.contextPath}/registro">
                        Crear cuenta gratis
                    </a>
                    <a class="btn btn-outline-secondary btn-lg rounded-pill px-4" href="${pageContext.request.contextPath}/login">
                        Ya tengo cuenta
                    </a>
                </div>
            </div>
            <div class="col-lg-5 text-center">
                <img src="${pageContext.request.contextPath}/img/logo.png" alt="FUSION DIGITAL"
                     class="landing-hero__logo mx-auto"
                     onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/img/logo-placeholder.svg';">
            </div>
        </div>
    </div>
</section>

<section class="landing-section landing-section--alt">
    <div class="container">
        <div class="text-center mb-4">
            <h2 class="landing-section__title h3">Todo lo que necesitas para operar</h2>
            <p class="text-muted mb-0">Sin instalar programas. Solo internet y tu navegador.</p>
        </div>
        <div class="row g-3">
            <div class="col-md-6 col-lg-4">
                <div class="landing-feature">
                    <div class="landing-feature__icon"><i class="bi bi-cart-check"></i></div>
                    <h3 class="h6 fw-bold">Ventas en mostrador</h3>
                    <p class="small text-muted mb-0">Carrito, cobro, ticket e impresion de comprobante al instante.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="landing-feature">
                    <div class="landing-feature__icon"><i class="bi bi-box-seam"></i></div>
                    <h3 class="h6 fw-bold">Inventario en tiempo real</h3>
                    <p class="small text-muted mb-0">Stock, alertas de agotado y ajustes con historial de movimientos.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="landing-feature">
                    <div class="landing-feature__icon"><i class="bi bi-people"></i></div>
                    <h3 class="h6 fw-bold">Varios vendedores</h3>
                    <p class="small text-muted mb-0">Tu equipo vende con su usuario; tu controlas el catalogo y los reportes.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="landing-feature">
                    <div class="landing-feature__icon"><i class="bi bi-graph-up"></i></div>
                    <h3 class="h6 fw-bold">Reportes claros</h3>
                    <p class="small text-muted mb-0">Ventas por periodo, vendedor y exportacion CSV para tu contabilidad.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="landing-feature">
                    <div class="landing-feature__icon"><i class="bi bi-scissors"></i></div>
                    <h3 class="h6 fw-bold">Servicios y agenda</h3>
                    <p class="small text-muted mb-0">Catalogo de servicios y citas para belleza, consultorias y mas.</p>
                </div>
            </div>
            <div class="col-md-6 col-lg-4">
                <div class="landing-feature">
                    <div class="landing-feature__icon"><i class="bi bi-receipt"></i></div>
                    <h3 class="h6 fw-bold">Facturacion basica</h3>
                    <p class="small text-muted mb-0">Datos fiscales del cliente, PDF informativo y soporte para timbrado CFDI.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="landing-section">
    <div class="container text-center">
        <h2 class="landing-section__title h4 mb-3">Para todo tipo de negocio local</h2>
        <div class="mb-0">
            <span class="landing-rubro">Abarrotes</span>
            <span class="landing-rubro">Ferreteria</span>
            <span class="landing-rubro">Ropa</span>
            <span class="landing-rubro">Tecnologia</span>
            <span class="landing-rubro">Farmacia</span>
            <span class="landing-rubro">Restaurante</span>
            <span class="landing-rubro">Belleza</span>
            <span class="landing-rubro">Regalos</span>
            <span class="landing-rubro">Servicios profesionales</span>
        </div>
    </div>
</section>

<section class="landing-section landing-section--alt" id="planes">
    <div class="container">
        <div class="text-center mb-4">
            <h2 class="landing-section__title h3">Planes simples en pesos mexicanos</h2>
            <p class="text-muted">Elige el plan cuando termines tu periodo de prueba.</p>
        </div>
        <div class="row g-3 justify-content-center">
            <c:forEach items="${planes}" var="plan" varStatus="st">
                <div class="col-md-6 col-lg-4">
                    <div class="landing-plan ${st.index == 1 ? 'landing-plan--destacado' : ''}">
                        <h3 class="h5 fw-bold mb-1">${plan.nombre}</h3>
                        <p class="small text-muted flex-grow-1">${plan.descripcion}</p>
                        <p class="landing-plan__precio mb-2">
                            $${plan.precioMensual}<small>/mes</small>
                        </p>
                        <ul class="list-unstyled small mb-4">
                            <li><i class="bi bi-check2 text-success"></i> ${plan.maxVendedores} vendedor(es)</li>
                            <li><i class="bi bi-check2 text-success"></i> ${plan.maxProductos} productos en catalogo</li>
                            <li><i class="bi bi-check2 text-success"></i> Soporte incluido</li>
                        </ul>
                        <a class="btn ${st.index == 1 ? 'btn-primary' : 'btn-outline-primary'} w-100 rounded-pill"
                           href="${pageContext.request.contextPath}/registro">
                            Empezar prueba
                        </a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</section>

<section class="landing-section">
    <div class="container">
        <div class="landing-cta">
            <h2 class="h3 mb-3">Listo para digitalizar tu negocio?</h2>
            <p class="mb-4">
                Registrate en minutos, configura tu catalogo con el asistente inicial y registra tu primera venta hoy mismo.
            </p>
            <a class="btn btn-warning btn-lg rounded-pill px-5 fw-bold" href="${pageContext.request.contextPath}/registro">
                Crear mi cuenta gratis
            </a>
        </div>
    </div>
</section>

<footer class="landing-footer">
    <div class="container">
        <div class="row g-3 align-items-center">
            <div class="col-md-6 text-center text-md-start">
                <strong class="text-white">FUSION DIGITAL</strong>
                <span class="small d-block">Sistema de ventas para PYMES</span>
            </div>
            <div class="col-md-6 text-center text-md-end small">
                <a href="${pageContext.request.contextPath}/registro/terminos" class="me-3">Terminos</a>
                <a href="${pageContext.request.contextPath}/registro/privacidad" class="me-3">Privacidad</a>
                <a href="mailto:${soporteEmail}">${soporteEmail}</a>
            </div>
        </div>
        <p class="small text-center mt-3 mb-0">&copy; 2026 FUSION DIGITAL. Cada cuenta gestiona su propio negocio.</p>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
