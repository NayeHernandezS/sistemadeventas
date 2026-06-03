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

                    <h2 class="h6 mt-4">1. Responsable</h2>
                    <p>El operador de la plataforma Sistema de Ventas, contacto via modulo Soporte.</p>

                    <h2 class="h6 mt-3">2. Datos recabados</h2>
                    <ul>
                        <li>Cuenta: usuario, email, rol.</li>
                        <li>Negocio: tipo de negocio, datos fiscales opcionales.</li>
                        <li>Operacion: productos, ventas, clientes que registres.</li>
                        <li>Tecnicos: logs y seguridad.</li>
                    </ul>

                    <h2 class="h6 mt-3">3. Finalidad y conservacion</h2>
                    <p>Prestacion del Servicio y seguridad. Conservacion mientras la cuenta este activa y por obligaciones legales.</p>

                    <h2 class="h6 mt-3">4. Comparticion</h2>
                    <p>No vendemos datos. Proveedores (hosting, correo, Mercado Pago) y autoridades cuando la ley lo exija.</p>

                    <h2 class="h6 mt-3">5. Derechos</h2>
                    <p>Puedes solicitar acceso, rectificacion o baja de tu cuenta via Soporte. Sobre datos de tus clientes finales, tu eres responsable.</p>

                    <h2 class="h6 mt-3">6. Seguridad y cambios</h2>
                    <p>Medidas razonables (HTTPS, cifrado de contraseñas, multi-tenant). Actualizaciones publicaran nueva version.</p>

                    <p class="text-muted mt-4 mb-0">Texto extendido: <code>docs/AVISO_PRIVACIDAD.md</code></p>
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
