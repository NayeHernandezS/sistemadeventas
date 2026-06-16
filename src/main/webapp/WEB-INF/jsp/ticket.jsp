<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.time.format.DateTimeFormatter" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="fmtFecha" value="<%=DateTimeFormatter.ofPattern(\"dd/MM/yyyy HH:mm\")%>"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <%@ include file="fragmentos/head-pwa.jspf" %>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ticket ${ticket.folio}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/ticket.css">
</head>
<body class="bg-light app-con-nav-movil">
<%@ include file="fragmentos/nav-tenant.jspf" %>

<div class="container py-4">
    <c:if test="${not empty mensajeExito}">
        <div class="alert alert-success no-print mx-auto ticket-page">${mensajeExito}</div>
    </c:if>
    <c:if test="${param.onboarding eq 'primeraVenta'}">
        <div class="alert alert-primary no-print mx-auto ticket-page text-center">
            <strong>Tu negocio ya puede cobrar.</strong>
            <a href="${pageContext.request.contextPath}/onboarding/listo" class="alert-link ms-1">Continuar configuracion</a>
        </div>
    </c:if>

    <div class="ticket-page">
        <article class="ticket-recibo" id="ticket-recibo">
            <header class="ticket-recibo__header">
                <div class="mb-2">
                    <c:set var="logoCssClass" value="ticket-recibo__logo" scope="request"/>
                    <%@ include file="fragmentos/logo-tenant.jspf" %>
                </div>
                <p class="ticket-recibo__titulo">COMPROBANTE DE VENTA</p>
                <p class="ticket-recibo__subtitulo">FUSION DIGITAL</p>
            </header>

            <div class="ticket-recibo__meta">
                <p><strong>Folio:</strong> ${ticket.folio}</p>
                <p><strong>Fecha:</strong> ${ticket.fechaVenta.format(fmtFecha)}</p>
                <p><strong>Vendedor:</strong> ${ticket.usernameVendedor}</p>
                <c:if test="${ticket.tieneNombreCliente}">
                    <p><strong>Cliente:</strong> ${ticket.nombreCliente}</p>
                </c:if>
                <p>
                    <strong>Estado:</strong>
                    <c:choose>
                        <c:when test="${ticket.estado eq 'DEVUELTO_TOTAL'}">Devuelto total</c:when>
                        <c:when test="${ticket.estado eq 'DEVUELTO_PARCIAL'}">Devolucion parcial</c:when>
                        <c:otherwise>Vigente</c:otherwise>
                    </c:choose>
                </p>
                <c:if test="${tieneFactura}">
                    <p><strong>Factura:</strong> ${factura.folioFactura}</p>
                </c:if>
            </div>

            <table class="ticket-recibo__lineas">
                <thead>
                <tr>
                    <th>Concepto</th>
                    <th class="text-end">Cant</th>
                    <th class="text-end">Importe</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${ticket.items}" var="item">
                    <tr>
                        <td>
                            ${item.nombreProducto}
                            <span class="text-muted">@ $${item.precioUnitario}</span>
                        </td>
                        <td class="text-end">${item.cantidad}</td>
                        <td class="text-end">$${item.importe}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <div class="ticket-recibo__total">
                <span>TOTAL</span>
                <span>$${ticket.total}</span>
            </div>

            <footer class="ticket-recibo__pie">
                <p class="mb-0">Gracias por su compra</p>
                <p class="mb-0">Conserve este comprobante</p>
            </footer>
        </article>

        <div class="no-print mt-4 d-flex flex-wrap gap-2 justify-content-center">
            <button type="button" class="btn btn-primary" onclick="window.print()">
                <i class="bi bi-printer"></i> Imprimir ticket
            </button>
            <c:if test="${tieneFactura}">
                <a class="btn btn-outline-primary"
                   href="${pageContext.request.contextPath}/factura?folioTicket=${ticket.folio}">
                    <i class="bi bi-receipt"></i> Ver factura
                </a>
            </c:if>
            <c:if test="${ticket.estado ne 'DEVUELTO_TOTAL'}">
                <a class="btn btn-outline-warning"
                   href="${pageContext.request.contextPath}/devoluciones/nueva?ticketId=${ticket.id}">
                    <i class="bi bi-arrow-return-left"></i> Devolucion
                </a>
            </c:if>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/tickets">
                <i class="bi bi-list-ul"></i> Mis tickets
            </a>
            <a class="btn btn-success" href="${pageContext.request.contextPath}/productos">
                <i class="bi bi-cart-plus"></i> Nueva venta
            </a>
        </div>
    </div>
</div>
<%@ include file="fragmentos/foot-app.jspf" %>
</body>
</html>
