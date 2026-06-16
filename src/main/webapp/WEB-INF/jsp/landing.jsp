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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/landing.css?v=2">
</head>
<body>

<nav class="navbar navbar-expand-lg landing-nav py-3 sticky-top">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">
            <span class="brand-accent">FUSION</span> DIGITAL
        </a>
        <div class="d-flex gap-2 ms-auto align-items-center">
            <c:if test="${not empty soporteWhatsappUrl}">
                <a class="btn btn-success btn-sm rounded-pill px-3 fw-semibold d-inline-flex align-items-center gap-1"
                   href="${soporteWhatsappUrl}"
                   target="_blank"
                   rel="noopener noreferrer"
                   title="Contactar por WhatsApp">
                    <svg class="landing-whatsapp-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" aria-hidden="true">
                        <path fill="currentColor" d="M13.601 2.326A7.85 7.85 0 0 0 7.994 0C3.627 0 .068 3.558.064 7.926c0 1.399.366 2.76 1.057 3.965L0 16l4.204-1.102a7.9 7.9 0 0 0 3.79.965h.004c4.368 0 7.926-3.558 7.93-7.93A7.9 7.9 0 0 0 13.6 2.326zM7.994 14.521a6.6 6.6 0 0 1-3.356-.92l-.24-.144-2.494.654.666-2.433-.156-.251a6.56 6.56 0 0 1-1.007-3.505c0-3.626 2.957-6.584 6.591-6.584a6.56 6.56 0 0 1 4.66 1.931 6.56 6.56 0 0 1 1.928 4.66c-.004 3.639-2.961 6.592-6.592 6.592m3.615-4.934c-.197-.099-1.17-.578-1.353-.646-.182-.066-.315-.099-.445.099-.133.197-.513.646-.627.775-.114.133-.232.148-.43.05-.197-.1-.836-.308-1.592-.985-.59-.525-.985-1.175-1.103-1.372-.114-.198-.011-.304.088-.403.09-.098.197-.232.296-.346.1-.114.133-.198.198-.33.066-.134.033-.248-.015-.347-.05-.099-.445-1.076-.612-1.47-.16-.389-.323-.335-.445-.34-.114-.007-.247-.007-.38-.007a.73.73 0 0 0-.529.247c-.182.198-.691.677-.691 1.654 0 .977.71 1.916.81 2.049.098.133 1.394 2.132 3.383 2.992.47.205.84.326 1.129.418.475.152.904.129 1.246.08.38-.058 1.171-.48 1.336-.943.164-.464.164-.86.114-.943-.049-.084-.182-.133-.38-.232"/>
                    </svg>
                    WhatsApp
                </a>
            </c:if>
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
                <c:if test="${not empty soporteWhatsappUrl}">
                    <span class="mx-2">·</span>
                    <a href="${soporteWhatsappUrl}" target="_blank" rel="noopener noreferrer">
                        <i class="bi bi-whatsapp"></i> WhatsApp
                    </a>
                </c:if>
            </div>
        </div>
        <p class="small text-center mt-3 mb-0">&copy; 2026 FUSION DIGITAL. Cada cuenta gestiona su propio negocio.</p>
    </div>
</footer>

<c:if test="${not empty soporteWhatsappUrl}">
    <a href="${soporteWhatsappUrl}"
       class="landing-whatsapp-fab"
       target="_blank"
       rel="noopener noreferrer"
       title="Contactar por WhatsApp"
       aria-label="Contactar por WhatsApp">
        <svg class="landing-whatsapp-icon landing-whatsapp-icon--fab" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" aria-hidden="true">
            <path fill="currentColor" d="M13.601 2.326A7.85 7.85 0 0 0 7.994 0C3.627 0 .068 3.558.064 7.926c0 1.399.366 2.76 1.057 3.965L0 16l4.204-1.102a7.9 7.9 0 0 0 3.79.965h.004c4.368 0 7.926-3.558 7.93-7.93A7.9 7.9 0 0 0 13.6 2.326zM7.994 14.521a6.6 6.6 0 0 1-3.356-.92l-.24-.144-2.494.654.666-2.433-.156-.251a6.56 6.56 0 0 1-1.007-3.505c0-3.626 2.957-6.584 6.591-6.584a6.56 6.56 0 0 1 4.66 1.931 6.56 6.56 0 0 1 1.928 4.66c-.004 3.639-2.961 6.592-6.592 6.592m3.615-4.934c-.197-.099-1.17-.578-1.353-.646-.182-.066-.315-.099-.445.099-.133.197-.513.646-.627.775-.114.133-.232.148-.43.05-.197-.1-.836-.308-1.592-.985-.59-.525-.985-1.175-1.103-1.372-.114-.198-.011-.304.088-.403.09-.098.197-.232.296-.346.1-.114.133-.198.198-.33.066-.134.033-.248-.015-.347-.05-.099-.445-1.076-.612-1.47-.16-.389-.323-.335-.445-.34-.114-.007-.247-.007-.38-.007a.73.73 0 0 0-.529.247c-.182.198-.691.677-.691 1.654 0 .977.71 1.916.81 2.049.098.133 1.394 2.132 3.383 2.992.47.205.84.326 1.129.418.475.152.904.129 1.246.08.38-.058 1.171-.48 1.336-.943.164-.464.164-.86.114-.943-.049-.084-.182-.133-.38-.232"/>
        </svg>
        <span class="landing-whatsapp-fab__label">WhatsApp</span>
    </a>
</c:if>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
