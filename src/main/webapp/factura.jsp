<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Factura</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    <style>
        @media print {
            .no-print { display: none !important; }
        }
    </style>
</head>
<body>
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
                <p class="small text-muted mt-3">Documento informativo generado por el sistema. No sustituye un CFDI ante el SAT.</p>
            </div>
        </c:otherwise>
    </c:choose>
    <div class="no-print mt-3">
        <button type="button" class="btn btn-outline-primary" onclick="window.print()">Imprimir</button>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/tickets">Volver a tickets</a>
    </div>
</div>
</body>
</html>
