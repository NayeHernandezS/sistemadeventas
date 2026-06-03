<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${titulo}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="bg-light">
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow border-0">
                <div class="card-body p-4 p-md-5 legal-doc small">
                    <h1 class="h4">${titulo}</h1>
                    <p class="text-muted">Version ${versionLegal}</p>

                    <h2 class="h6 mt-4">1. Objeto</h2>
                    <p>Regula el uso del software Sistema de Ventas (SaaS) para gestion de ventas, inventario y funciones relacionadas.</p>

                    <h2 class="h6 mt-3">2. Cuenta</h2>
                    <p>Debes proporcionar datos veraces. Eres responsable de tus credenciales y del uso de tu cuenta.</p>

                    <h2 class="h6 mt-3">3. Uso permitido</h2>
                    <p>Prohibido uso ilicito, acceso no autorizado a otros tenants o ataques a la infraestructura.</p>

                    <h2 class="h6 mt-3">4. Tus datos de negocio</h2>
                    <p>Los datos que cargues son tuyos. Otorgas al operador las facultades necesarias para alojarlos y prestar el Servicio.</p>

                    <h2 class="h6 mt-3">5. Cambios y disponibilidad</h2>
                    <p>El operador puede actualizar funciones, precios o estos terminos. La version vigente se muestra en el registro.</p>

                    <h2 class="h6 mt-3">6. Responsabilidad</h2>
                    <p>En la medida legal permitida, la responsabilidad se limita a los montos de suscripcion pagados en los ultimos doce meses cuando aplique.</p>

                    <h2 class="h6 mt-3">7. Contacto</h2>
                    <p>Modulo Soporte en la aplicacion o canales del operador.</p>

                    <p class="text-muted mt-4 mb-0">Texto extendido: <code>docs/TERMINOS_SERVICIO.md</code></p>
                </div>
                <div class="card-footer bg-white">
                    <a href="${pageContext.request.contextPath}/registro" class="btn btn-primary btn-sm">Volver al registro</a>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
