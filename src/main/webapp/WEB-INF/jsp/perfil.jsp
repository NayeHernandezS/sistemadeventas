<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <title>Mi perfil</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
</head>
<body class="bg-light app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-5" style="max-width: 720px;">
    <h1 class="h3 mb-4">Mi perfil</h1>

    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success">${mensajeExito}</div>
    </c:if>
    <c:if test="${not empty mensajeError}">
        <div class="alert alert-danger">${mensajeError}</div>
    </c:if>

    <div class="card shadow-sm mb-4">
        <div class="card-header">Datos de la cuenta</div>
        <div class="card-body">
            <dl class="row mb-0">
                <dt class="col-sm-4">Usuario</dt>
                <dd class="col-sm-8"><strong>${username}</strong></dd>

                <dt class="col-sm-4">Rol</dt>
                <dd class="col-sm-8">${rolEtiqueta}</dd>

                <c:if test="${not empty tenantOwner}">
                    <dt class="col-sm-4">Cuenta del negocio</dt>
                    <dd class="col-sm-8">${tenantOwner}</dd>
                </c:if>

                <c:if test="${not empty adminOwner}">
                    <dt class="col-sm-4">Administrador</dt>
                    <dd class="col-sm-8">${adminOwner}</dd>
                </c:if>

                <c:if test="${not empty tipoNegocioEtiqueta}">
                    <dt class="col-sm-4">Tipo de negocio</dt>
                    <dd class="col-sm-8">${tipoNegocioEtiqueta}</dd>
                </c:if>

                <dt class="col-sm-4">Ultimo acceso</dt>
                <dd class="col-sm-8">
                    <c:choose>
                        <c:when test="${ultimoAcceso != null}">
                            ${ultimoAcceso.format(formatoAcceso)}
                            <c:if test="${usuario.ultimoAccesoEsHoy()}">
                                <span class="badge bg-success ms-1">Hoy</span>
                            </c:if>
                        </c:when>
                        <c:otherwise><span class="text-muted">Sin registro (cierra sesion y vuelve a entrar)</span></c:otherwise>
                    </c:choose>
                </dd>
            </dl>
        </div>
    </div>

    <c:if test="${not empty planActivo}">
        <div class="card shadow-sm mb-4">
            <div class="card-header">Uso del plan</div>
            <div class="card-body">
                <p class="mb-3"><strong>Plan:</strong> ${planNombre}</p>
                <div class="mb-3">
                    <div class="d-flex justify-content-between small mb-1">
                        <span>Vendedores</span>
                        <span>${vendedoresUsados} / ${planActivo.maxVendedores}</span>
                    </div>
                    <div class="progress" style="height: 8px;">
                        <div class="progress-bar progress-bar--pct-dinamico" role="progressbar"
                             style="--pct: <c:out value='${porcentajeVendedores}' default='0'/>"
                             aria-valuenow="${porcentajeVendedores}" aria-valuemin="0" aria-valuemax="100"></div>
                    </div>
                </div>
                <div>
                    <div class="d-flex justify-content-between small mb-1">
                        <span>Productos</span>
                        <span>${productosUsados} / ${planActivo.maxProductos}</span>
                    </div>
                    <div class="progress" style="height: 8px;">
                        <div class="progress-bar bg-info progress-bar--pct-dinamico" role="progressbar"
                             style="--pct: <c:out value='${porcentajeProductos}' default='0'/>"
                             aria-valuenow="${porcentajeProductos}" aria-valuemin="0" aria-valuemax="100"></div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <c:if test="${esAdmin}">
        <div class="card shadow-sm mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>Equipo — ultimo acceso</span>
                <a href="${pageContext.request.contextPath}/usuarios" class="btn btn-sm btn-outline-primary">Gestionar vendedores</a>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Usuario</th>
                            <th>Ultimo acceso</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td><strong>${username}</strong> <span class="badge bg-primary">Tu</span></td>
                            <td>
                                <c:choose>
                                    <c:when test="${ultimoAcceso != null}">
                                        ${ultimoAcceso.format(formatoAcceso)}
                                        <c:if test="${usuario.ultimoAccesoEsHoy()}">
                                            <span class="badge bg-success ms-1">Hoy</span>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <c:forEach var="v" items="${equipoAccesos}">
                            <tr>
                                <td>${v.username}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${v.ultimoAcceso != null}">
                                            ${v.ultimoAcceso.format(formatoAcceso)}
                                            <c:if test="${v.ultimoAccesoEsHoy()}">
                                                <span class="badge bg-success ms-1">Hoy</span>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise><span class="text-muted">Nunca</span></c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <c:if test="${empty equipoAccesos}">
                    <p class="text-muted small px-3 pb-3 mb-0">Aun no tienes vendedores. Cuando entren al sistema, veras su ultimo acceso aqui.</p>
                </c:if>
            </div>
        </div>
    </c:if>

    <c:if test="${not esAdmin and not empty resumenMes}">
        <div class="card shadow-sm mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>Mi actividad</span>
                <a href="${pageContext.request.contextPath}/tickets" class="btn btn-sm btn-outline-primary">Ver todos</a>
            </div>
            <div class="card-body">
                <p class="mb-3 text-muted">Resumen de <strong>${mesActividad}</strong></p>
                <div class="row g-3 mb-4">
                    <div class="col-sm-6">
                        <div class="border rounded p-3 h-100">
                            <div class="small text-muted">Tickets generados</div>
                            <div class="h4 mb-0">${resumenMes.cantidadTickets}</div>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="border rounded p-3 h-100">
                            <div class="small text-muted">Total vendido</div>
                            <div class="h4 mb-0">$${resumenMes.totalImporte}</div>
                        </div>
                    </div>
                </div>

                <h2 class="h6">Tickets recientes</h2>
                <c:choose>
                    <c:when test="${not empty ticketsRecientes}">
                        <div class="table-responsive">
                            <table class="table table-sm align-middle mb-0">
                                <thead>
                                <tr>
                                    <th>Folio</th>
                                    <th>Fecha</th>
                                    <th>Total</th>
                                    <th>Estado</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="t" items="${ticketsRecientes}">
                                    <tr>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/factura?folioTicket=${t.folio}">${t.folio}</a>
                                        </td>
                                        <td>${t.fechaVenta.format(formatoTicket)}</td>
                                        <td>$${t.total}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${t.estado == 'DEVUELTO_TOTAL'}">
                                                    <span class="badge bg-secondary">Devuelto</span>
                                                </c:when>
                                                <c:when test="${t.estado == 'DEVUELTO_PARCIAL'}">
                                                    <span class="badge bg-warning text-dark">Parcial</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-success">Activo</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted mb-0">Aun no has registrado ventas este mes.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:if>

    <c:if test="${esAdmin}">
        <div class="card shadow-sm mb-4">
            <div class="card-header">Logo de tu marca</div>
            <div class="card-body">
                <p class="small text-muted mb-3">
                    Sube el logo de tu empresa (PNG, JPG o WebP, max. 2 MB).
                    La imagen se ajusta automaticamente al espacio del panel, navbar y tickets.
                    Todos los vendedores de tu cuenta veran la misma imagen en el panel principal.
                </p>
                <div class="d-flex align-items-center gap-3 mb-3">
                    <div class="logo-slot logo-slot--md">
                        <c:set var="logoCssClass" value="logo-tenant-img--preview" scope="request"/>
                        <%@ include file="fragmentos/logo-tenant.jspf" %>
                    </div>
                    <div>
                        <c:if test="${tenantTieneLogo}">
                            <span class="badge bg-success">Logo activo</span>
                        </c:if>
                        <c:if test="${not tenantTieneLogo}">
                            <span class="badge bg-secondary">Sin logo personalizado</span>
                        </c:if>
                    </div>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/perfil/logo"
                      enctype="multipart/form-data" class="mb-3">
                    <%@ include file="csrf.jspf" %>
                    <div class="mb-3">
                        <label for="logo" class="form-label">Imagen (PNG, JPG o WebP, max. 2 MB)</label>
                        <input type="file" class="form-control" id="logo" name="logo" accept="image/png,image/jpeg,image/webp" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Subir logo</button>
                </form>
                <c:if test="${tenantTieneLogo}">
                    <form method="post" action="${pageContext.request.contextPath}/perfil/logo/eliminar"
                          onsubmit="return confirm('Quitar el logo del negocio?');">
                        <%@ include file="csrf.jspf" %>
                        <button type="submit" class="btn btn-outline-danger btn-sm">Quitar logo</button>
                    </form>
                </c:if>
            </div>
        </div>
    </c:if>

    <c:if test="${not esAdmin and tenantTieneLogo}">
        <div class="card shadow-sm mb-4">
            <div class="card-header">Logo del negocio</div>
            <div class="card-body d-flex align-items-center gap-3">
                <div class="logo-slot logo-slot--sm">
                    <c:set var="logoCssClass" value="logo-tenant-img--preview" scope="request"/>
                    <%@ include file="fragmentos/logo-tenant.jspf" %>
                </div>
                <p class="small text-muted mb-0">
                    Este es el logo de la cuenta <strong>${tenantOwner}</strong>.
                    Solo el administrador puede cambiarlo.
                </p>
            </div>
        </div>
    </c:if>

    <c:if test="${esAdmin}">
        <div class="card shadow-sm mb-4">
            <div class="card-header">Tipo de negocio</div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/perfil/tipo-negocio">
                    <%@ include file="csrf.jspf" %>
                    <div class="mb-3">
                        <label for="tipoNegocio" class="form-label">Rubro del negocio</label>
                        <select id="tipoNegocio" name="tipoNegocio" class="form-select" required>
                            <option value="">Selecciona una opcion</option>
                            <c:forEach var="entry" items="${tiposNegocio}">
                                <option value="${entry.key}" ${tipoNegocio == entry.key ? 'selected' : ''}>${entry.value}</option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty errores.tipoNegocio}">
                            <div class="text-danger small">${errores.tipoNegocio}</div>
                        </c:if>
                    </div>
                    <button type="submit" class="btn btn-primary">Guardar tipo de negocio</button>
                </form>
            </div>
        </div>
    </c:if>

    <c:if test="${esAdmin}">
        <div class="card shadow-sm mb-4">
            <div class="card-header">Preferencias del negocio</div>
            <div class="card-body">
                <p class="small text-muted">
                    Personaliza alertas de inventario para tu cuenta. Si dejas el campo vacio se usa el valor global
                    (${stockMinimoGlobal} unidades).
                </p>
                <form method="post" action="${pageContext.request.contextPath}/perfil/preferencias">
                    <%@ include file="csrf.jspf" %>
                    <div class="mb-3">
                        <label for="stockMinimo" class="form-label">Umbral de stock bajo (unidades)</label>
                        <input type="number" class="form-control" id="stockMinimo" name="stockMinimo"
                               min="1" max="999" style="max-width: 8rem"
                               value="${stockMinimoTenant}"
                               placeholder="${stockMinimoGlobal}">
                        <c:if test="${not empty errores.stockMinimo}">
                            <div class="text-danger small">${errores.stockMinimo}</div>
                        </c:if>
                    </div>
                    <p class="small mb-3">
                        Umbral activo ahora: <strong>${stockMinimoEfectivo}</strong> unidades
                    </p>
                    <button type="submit" class="btn btn-primary">Guardar preferencias</button>
                </form>
            </div>
        </div>
    </c:if>

    <div class="card shadow-sm mb-4">
        <div class="card-header">Correo electronico</div>
        <div class="card-body">
            <form method="post" action="${pageContext.request.contextPath}/perfil/email">
                <%@ include file="csrf.jspf" %>
                <div class="mb-3">
                    <label for="email" class="form-label">Email de la cuenta</label>
                    <input type="email" class="form-control" id="email" name="email"
                           value="${email}" required maxlength="150">
                    <c:if test="${not empty errores.email}">
                        <div class="text-danger small">${errores.email}</div>
                    </c:if>
                </div>
                <button type="submit" class="btn btn-primary">Guardar email</button>
            </form>
        </div>
    </div>

    <c:if test="${esAdmin}">
        <div class="card shadow-sm mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>Suscripcion del negocio</span>
                <a href="${pageContext.request.contextPath}/suscripcion" class="btn btn-sm btn-outline-primary">
                    Gestionar plan
                </a>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty suscripcion}">
                        <p class="mb-2">
                            <strong>Plan:</strong> ${planNombre}
                            <c:if test="${suscripcion.enPeriodoPrueba}">
                                <span class="badge bg-warning text-dark">Periodo de prueba</span>
                            </c:if>
                        </p>
                        <p class="mb-2"><strong>Vigente hasta:</strong> ${fechaFinTexto}</p>
                        <p class="mb-2">
                            <strong>Estado:</strong>
                            <c:choose>
                                <c:when test="${suscripcionVigente}">
                                    <span class="badge bg-success">Activa</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Vencida</span>
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <c:if test="${not suscripcionVigente}">
                            <div class="alert alert-warning mb-0">
                                Tu suscripcion ha vencido.
                                <a href="${pageContext.request.contextPath}/suscripcion?requierePago=1">Renovar ahora</a>
                            </div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted mb-2">No hay suscripcion registrada para esta cuenta.</p>
                        <a href="${pageContext.request.contextPath}/suscripcion?requierePago=1"
                           class="btn btn-sm btn-primary">Contratar plan</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="card shadow-sm mb-4">
            <div class="card-header">Datos fiscales por defecto</div>
            <div class="card-body">
                <details class="mb-3 border rounded p-3 bg-light">
                    <summary class="fw-semibold" style="cursor:pointer">Guia rapida de facturacion</summary>
                    <div class="small text-muted mt-2">
                        <p><strong>Solo PDF (sin SAT):</strong> llena RFC, razon social, C.P. y regimen de tu negocio abajo y cobra con factura en el carro.</p>
                        <p class="mb-2"><strong>CFDI timbrado:</strong> ademas conecta tu Facturama en la seccion de abajo (cuenta propia + CSD en su panel).</p>
                        <p class="mb-0">Los datos del <em>cliente</em> (RFC receptor) se capturan al cobrar o desde el catalogo <a href="${pageContext.request.contextPath}/clientes">Clientes</a>.</p>
                    </div>
                </details>
                <p class="small text-muted">
                    Se precargan en el carrito al facturar una venta. Puedes modificarlos en cada ticket.
                </p>
                <form method="post" action="${pageContext.request.contextPath}/perfil/datos-fiscales">
                    <%@ include file="csrf.jspf" %>
                    <div class="row g-2">
                        <div class="col-md-4">
                            <label for="rfcDefault" class="form-label">RFC</label>
                            <input type="text" class="form-control" id="rfcDefault" name="rfcDefault"
                                   maxlength="13" value="${datosFiscales.rfc}">
                        </div>
                        <div class="col-md-8">
                            <label for="razonSocialDefault" class="form-label">Razon social o nombre</label>
                            <input type="text" class="form-control" id="razonSocialDefault" name="razonSocialDefault"
                                   maxlength="200" value="${datosFiscales.razonSocial}">
                        </div>
                        <div class="col-md-6">
                            <label for="emailFiscalDefault" class="form-label">Correo (opcional)</label>
                            <input type="email" class="form-control" id="emailFiscalDefault" name="emailFiscalDefault"
                                   maxlength="150" value="${datosFiscales.email}">
                        </div>
                        <div class="col-md-6">
                            <label for="usoCfdiDefault" class="form-label">Uso CFDI (opcional)</label>
                            <input type="text" class="form-control" id="usoCfdiDefault" name="usoCfdiDefault"
                                   maxlength="10" placeholder="ej. G03" value="${datosFiscales.usoCfdi}">
                        </div>
                        <div class="col-md-3">
                            <label for="codigoPostalEmisor" class="form-label">C.P. emisor<c:if test="${cfdiTimbradoDisponible}"> *</c:if></label>
                            <input type="text" class="form-control" id="codigoPostalEmisor" name="codigoPostalEmisor"
                                   maxlength="5" placeholder="5 digitos" value="${datosFiscales.codigoPostal}">
                        </div>
                        <div class="col-md-3">
                            <label for="regimenFiscalEmisor" class="form-label">Regimen fiscal<c:if test="${cfdiTimbradoDisponible}"> *</c:if></label>
                            <input type="text" class="form-control" id="regimenFiscalEmisor" name="regimenFiscalEmisor"
                                   maxlength="3" placeholder="601" value="${datosFiscales.regimenFiscal}">
                        </div>
                        <div class="col-12">
                            <label for="direccionDefault" class="form-label">Direccion (opcional)</label>
                            <input type="text" class="form-control" id="direccionDefault" name="direccionDefault"
                                   maxlength="255" value="${datosFiscales.direccion}">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary mt-3">Guardar datos fiscales</button>
                </form>
            </div>
        </div>

        <div class="card shadow-sm mb-4">
            <div class="card-header">Timbrado CFDI (Facturama)</div>
            <div class="card-body">
                <p class="small text-muted mb-3">
                    Cada negocio usa su propia cuenta Facturama. Crea la cuenta en
                    <a href="https://apisandbox.facturama.mx/" target="_blank" rel="noopener">sandbox</a>
                    o produccion, sube tu CSD en su panel y pega aqui las credenciales API.
                </p>
                <c:if test="${cfdiTimbradoDisponible && cfdiFacturamaPropio}">
                    <div class="alert alert-success py-2 small">Timbrado activo con la cuenta Facturama de este negocio.</div>
                </c:if>
                <c:if test="${not cfdiTimbradoDisponible}">
                    <div class="alert alert-secondary py-2 small">
                        Sin timbrado CFDI. Completa datos fiscales del emisor arriba y conecta Facturama,
                        o seguira disponible solo el PDF informativo al cobrar.
                    </div>
                </c:if>
                <form method="post" action="${pageContext.request.contextPath}/perfil/facturama-cfdi">
                    <%@ include file="csrf.jspf" %>
                    <div class="row g-2">
                        <div class="col-md-6">
                            <label for="facturamaUsername" class="form-label">Usuario API Facturama</label>
                            <input type="text" class="form-control" id="facturamaUsername" name="facturamaUsername"
                                   maxlength="150" value="${datosFiscales.facturamaUsername}">
                        </div>
                        <div class="col-md-6">
                            <label for="facturamaPassword" class="form-label">Contrasena API</label>
                            <input type="password" class="form-control" id="facturamaPassword" name="facturamaPassword"
                                   maxlength="150" autocomplete="new-password"
                                   placeholder="Dejar vacio para no cambiar la contrasena">
                        </div>
                        <div class="col-md-6">
                            <div class="form-check mt-4">
                                <input class="form-check-input" type="checkbox" id="facturamaSandbox" name="facturamaSandbox"
                                       <c:if test="${empty datosFiscales || datosFiscales.facturamaSandbox}">checked</c:if>>
                                <label class="form-check-label" for="facturamaSandbox">Usar entorno sandbox (pruebas)</label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-check mt-4">
                                <input class="form-check-input" type="checkbox" id="cfdiHabilitado" name="cfdiHabilitado"
                                       <c:if test="${not empty datosFiscales && datosFiscales.cfdiHabilitado}">checked</c:if>>
                                <label class="form-check-label" for="cfdiHabilitado">Activar timbrado CFDI al cobrar</label>
                            </div>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary mt-3">Guardar conexion Facturama</button>
                </form>
            </div>
        </div>
    </c:if>

    <div class="card shadow-sm">
        <div class="card-header">Cambiar contraseña</div>
        <div class="card-body">
            <form method="post" action="${pageContext.request.contextPath}/perfil/password">
                <%@ include file="csrf.jspf" %>
                <div class="mb-3">
                    <label for="passwordActual" class="form-label">Contraseña actual</label>
                    <input type="password" class="form-control" id="passwordActual" name="passwordActual" required>
                    <c:if test="${not empty errores.passwordActual}">
                        <div class="text-danger small">${errores.passwordActual}</div>
                    </c:if>
                </div>
                <div class="mb-3">
                    <label for="passwordNueva" class="form-label">Nueva contraseña</label>
                    <input type="password" class="form-control" id="passwordNueva" name="passwordNueva"
                           minlength="4" required>
                    <c:if test="${not empty errores.passwordNueva}">
                        <div class="text-danger small">${errores.passwordNueva}</div>
                    </c:if>
                </div>
                <div class="mb-3">
                    <label for="passwordConfirmacion" class="form-label">Confirmar nueva contraseña</label>
                    <input type="password" class="form-control" id="passwordConfirmacion" name="passwordConfirmacion"
                           minlength="4" required>
                    <c:if test="${not empty errores.passwordConfirmacion}">
                        <div class="text-danger small">${errores.passwordConfirmacion}</div>
                    </c:if>
                </div>
                <button type="submit" class="btn btn-primary">Guardar contraseña</button>
                <a href="${pageContext.request.contextPath}/inicio" class="btn btn-outline-secondary">Volver</a>
            </form>
        </div>
    </div>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
