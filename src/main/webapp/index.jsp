<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Menu de usuario</title>
    <link rel="stylesheet" type="text/css" src="css/estiloindex.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm py-3">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center" href="#">
            <img src="resources/logoferreteria.png" alt="Logo" class="me-3">
            <span class="fw-bold fs-4 text-primary">Tlapaleria<span class="text-warning">Branette</span></span>
        </a>
        <div class="ms-auto">
            <a class="btn btn-outline-success btn-sm rounded-pill px-3" href="${pageContext.request.contextPath}/${not empty sessionScope.username? "logout": "login"}">
                ${not empty sessionScope.username? "Cerrar sesion": "Iniciar sesion"}<i class="bi bi-power"></i>
            </a>
        </div>
    </div>
</nav>

<main class="container my-5">
    <div class="text-center mb-5">
        <h2 class="hero-title mb-2">Panel de Usuario</h2>
        <p class="text-muted">Bienvenido de nuevo. Que tarea deseas realizar hoy?</p>
    </div>

    <div class="row g-4 justify-content-center text-center">

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-cart-plus"></i></div>
                <a href="${pageContext.request.contextPath}/productos" class="menu-link stretched-link">Modulo de Ventas </a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-box-seam"></i></div>
                <a href="${pageContext.request.contextPath}/crudprod" class="menu-link stretched-link">Control de Inventario</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-ticket-detailed"></i></div>
                <a href="${pageContext.request.contextPath}/tickets" class="menu-link stretched-link">Tickets</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4">
                <div class="icon-circle"><i class="bi bi-calendar2-check"></i></div>
                <a href="${pageContext.request.contextPath}/reportes" class="menu-link stretched-link">reportes</a>
            </div>
        </div>

        <div class="col-md-4 col-lg-3">
            <div class="card h-100 menu-card p-4 border-start border-warning border-3">
                <div class="icon-circle"><i class="bi bi-person-lock"></i></div>
                <a href="${pageContext.request.contextPath}/usuarios" class="menu-link stretched-link">Gestion de Acceso</a>
            </div>
        </div>

    </div>
</main>

<footer>
    <div class="container">
        <div class="row g-4 mb-4">
            <div class="col-lg-4 col-md-6">
                <h5 class="footer-title">Ferretería Branette</h5>
                <p class="small">Suministros industriales y del hogar con la mejor calidad del mercado. Llevamos más de 20 años construyendo juntos.</p>
                <div class="mt-3">
                    <a href="#" class="social-btn"><i class="bi bi-facebook"></i></a>
                    <a href="#" class="social-btn"><i class="bi bi-instagram"></i></a>
                    <a href="#" class="social-btn"><i class="bi bi-whatsapp"></i></a>
                </div>
            </div>

            <div class="col-lg-4 col-md-6">
                <h5 class="footer-title">Informacion de Contacto</h5>
                <ul class="list-unstyled small">
                    <li class="mb-3">
                        <i class="bi bi-geo-alt-fill text-warning me-2"></i>
                        Av. Principal Industrial #456, Ciudad de México
                    </li>
                    <li class="mb-3">
                        <i class="bi bi-telephone-fill text-warning me-2"></i>
                        +52 (55) 1234-5678
                    </li>
                    <li class="mb-3">
                        <i class="bi bi-envelope-fill text-warning me-2"></i>
                        soporte@ferrepro.com
                    </li>
                </ul>
            </div>

            <div class="col-lg-4 col-md-12 text-center text-lg-start">
                <h5 class="footer-title">Horario de Atención</h5>
                <p class="small mb-1">Lunes a Viernes: 8:00 AM - 7:00 PM</p>
                <p class="small mb-1">Sábados: 9:00 AM - 2:00 PM</p>
                <p class="text-warning fw-bold small mt-2">Emergencias 24/7 vía WhatsApp</p>
            </div>
        </div>

        <div class="border-top border-secondary py-4 text-center">
            <p class="small mb-0 text-secondary">&copy; 2026 Ferreteria Branette S.A. de C.V. Todos los derechos reservados.</p>
        </div>
    </div>
</footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>