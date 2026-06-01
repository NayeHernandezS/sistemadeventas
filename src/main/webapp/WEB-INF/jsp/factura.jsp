<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Factura</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tema.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        @media print {
            .no-print { display: none !important; }
        }
    </style>
</head>
<body>
<%@ include file="fragmentos/nav-tenant.jspf" %>
<div class="container my-4">
    <c:choose>
        <c:when test="${empty factura}">
            <div class="alert alert-warning">
                Esta venta (ticket <strong>${ticket.folio}</strong>) no tiene factura registrada.
                Si el cliente la requiere, finalice la venta marcando la opción de facturación en el carrito.
            </div>
        </c:when>
        <c:otherwise>
            <div class="border p-4">
                <h2 class="mb-4">Factura (comprobante de venta)</h2>
                <div class="row mb-3">
                    <div class="col-md-6">
                        <p class="mb-1"><strong>Folio factura:</strong> ${factura.folioFactura}</p>
                        <p class="mb-1"><strong>Fecha emisión:</strong> ${factura.fechaEmision}</p>
                        <p class="mb-1"><strong>Ticket venta:</strong> ${ticket.folio}</p>
                    </div>
                    <div class="col-md-6">
                        <p class="mb-1"><strong>Vendedor:</strong> ${ticket.usernameVendedor}</p>
                        <p class="mb-1"><strong>Total venta:</strong> $${ticket.total}</p>
                    </div>
                </div>
                <hr>
                <h5>Datos del cliente (fiscal)</h5>
                <p class="mb-1"><strong>RFC:</strong> ${factura.rfc}</p>
                <p class="mb-1"><strong>Razón social / nombre:</strong> ${factura.razonSocial}</p>
                <c:if test="${not empty factura.email}">
                    <p class="mb-1"><strong>Correo:</strong> ${factura.email}</p>
                </c:if>
                <c:if test="${not empty factura.direccion}">
                    <p class="mb-1"><strong>Dirección:</strong> ${factura.direccion}</p>
                </c:if>
                <c:if test="${not empty factura.usoCfdi}">
                    <p class="mb-1"><strong>Uso CFDI:</strong> ${factura.usoCfdi}</p>
                </c:if>
                <c:if test="${not empty factura.codigoPostalReceptor}">
                    <p class="mb-1"><strong>C.P. receptor:</strong> ${factura.codigoPostalReceptor}</p>
                </c:if>
                <hr>
                <h5>Estado CFDI</h5>
                <c:choose>
                    <c:when test="${factura.estaTimbrada()}">
                        <p class="mb-1"><span class="badge bg-success">Timbrado</span></p>
                        <p class="mb-1"><strong>UUID:</strong> ${factura.cfdiUuid}</p>
                    </c:when>
                    <c:when test="${factura.cfdiEstado eq 'ERROR'}">
                        <p class="mb-1"><span class="badge bg-danger">Error de timbrado</span></p>
                        <p class="small text-danger">${factura.cfdiMensaje}</p>
                    </c:when>
                    <c:otherwise>
                        <p class="mb-1"><span class="badge bg-secondary">Comprobante informativo</span></p>
                        <c:if test="${cfdiTimbradoDisponible}">
                            <p class="small text-muted">No se timbro ante el SAT. Revise datos fiscales del emisor en Perfil.</p>
                        </c:if>
                    </c:otherwise>
                </c:choose>
                <hr>
                <h5>Detalle</h5>
                <table class="table table-sm table-bordered">
                    <thead>
                    <tr>
                        <th>Producto</th>
                        <th class="text-end">Precio</th>
                        <th class="text-end">Cant.</th>
                        <th class="text-end">Importe</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${ticket.items}" var="item">
                        <tr>
                            <td>${item.nombreProducto}</td>
                            <td class="text-end">$${item.precioUnitario}</td>
                            <td class="text-end">${item.cantidad}</td>
                            <td class="text-end">$${item.importe}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                    <tfoot>
                    <tr>
                        <th colspan="3" class="text-end">Total</th>
                        <th class="text-end">$${ticket.total}</th>
                    </tr>
                    </tfoot>
                </table>
                <p class="small text-muted mt-3">
                    <c:choose>
                        <c:when test="${factura.estaTimbrada()}">
                            CFDI timbrado ante el SAT via Facturama.
                        </c:when>
                        <c:otherwise>
                            Documento informativo generado por el sistema. No sustituye un CFDI ante el SAT.
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
        </c:otherwise>
    </c:choose>
    <div class="no-print mt-3 d-flex flex-wrap gap-2">
        <c:if test="${not empty factura}">
            <a class="btn btn-primary"
               href="${pageContext.request.contextPath}/factura/pdf?folioTicket=${ticket.folio}">
                PDF informativo
            </a>
            <c:if test="${factura.estaTimbrada()}">
                <a class="btn btn-success"
                   href="${pageContext.request.contextPath}/factura/cfdi/pdf?folioTicket=${ticket.folio}">
                    Descargar CFDI (PDF)
                </a>
                <a class="btn btn-outline-success"
                   href="${pageContext.request.contextPath}/factura/cfdi/xml?folioTicket=${ticket.folio}">
                    Descargar XML
                </a>
            </c:if>
        </c:if>
        <button type="button" class="btn btn-outline-primary" onclick="window.print()">Imprimir</button>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/tickets">Volver a tickets</a>
    </div>
</div>
</body>
</html>
