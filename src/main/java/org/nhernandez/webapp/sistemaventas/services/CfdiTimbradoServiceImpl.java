package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.cfdi.CfdiException;
import org.nhernandez.webapp.sistemaventas.cfdi.facturama.FacturamaCfdiApiClient;
import org.nhernandez.webapp.sistemaventas.cfdi.facturama.FacturamaCfdiRequestBuilder;
import org.nhernandez.webapp.sistemaventas.config.CfdiProperties;
import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.DatosFiscalesNegocioRepository;
import org.nhernandez.webapp.sistemaventas.repositories.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;

@Service
public class CfdiTimbradoServiceImpl implements CfdiTimbradoService {

    private static final Logger log = LoggerFactory.getLogger(CfdiTimbradoServiceImpl.class);

    private final CfdiProperties cfdiProperties;
    private final FacturamaCfdiApiClient facturamaClient;
    private final DatosFiscalesNegocioRepository datosFiscalesRepository;
    private final FacturaRepository facturaRepository;

    public CfdiTimbradoServiceImpl(CfdiProperties cfdiProperties,
                                   FacturamaCfdiApiClient facturamaClient,
                                   DatosFiscalesNegocioRepository datosFiscalesRepository,
                                   FacturaRepository facturaRepository) {
        this.cfdiProperties = cfdiProperties;
        this.facturamaClient = facturamaClient;
        this.datosFiscalesRepository = datosFiscalesRepository;
        this.facturaRepository = facturaRepository;
    }

    @Override
    public boolean disponible() {
        return cfdiProperties.habilitado();
    }

    @Override
    public String reintentarTimbrar(String tenantOwner, TicketVenta ticket, Factura factura) {
        if (!disponible()) {
            throw new ServiceJdbcException(
                    "Timbrado CFDI no esta configurado. Revise credenciales Facturama en el servidor.", null);
        }
        if (ticket == null || ticket.getItems() == null || ticket.getItems().isEmpty()) {
            throw new ServiceJdbcException("El ticket no tiene detalle para timbrar.", null);
        }
        if (factura == null || factura.getId() == null) {
            throw new ServiceJdbcException("No hay factura asociada a este ticket.", null);
        }
        if (factura.estaTimbrada()) {
            throw new ServiceJdbcException("Esta factura ya esta timbrada.", null);
        }
        factura.setCfdiEstado("PENDIENTE");
        factura.setCfdiMensaje(null);
        intentarTimbrar(tenantOwner, ticket, factura);
        if (factura.estaTimbrada()) {
            return "CFDI timbrado correctamente. UUID: " + factura.getCfdiUuid();
        }
        String mensaje = factura.getCfdiMensaje();
        return mensaje != null && !mensaje.isBlank()
                ? "No se pudo timbrar: " + mensaje
                : "No se pudo timbrar el CFDI. Revise los datos e intente de nuevo.";
    }

    @Override
    public void intentarTimbrar(String tenantOwner, TicketVenta ticket, Factura factura) {
        if (!disponible() || factura == null || factura.getId() == null) {
            return;
        }
        try {
            DatosFiscalesNegocio emisor = datosFiscalesRepository.porTenant(tenantOwner);
            if (emisor == null || !emisor.listoParaTimbrarEmisor()) {
                marcarError(factura,
                        "Completa en Perfil los datos fiscales del emisor: RFC, razon social, codigo postal y regimen fiscal.");
                return;
            }
            if (factura.getCodigoPostalReceptor() == null || factura.getCodigoPostalReceptor().isBlank()) {
                marcarError(factura, "Indica el codigo postal del receptor para timbrar el CFDI.");
                return;
            }
            Map<String, Object> body = FacturamaCfdiRequestBuilder.construir(emisor, factura, ticket);
            FacturamaCfdiApiClient.CfdiTimbradoRespuesta respuesta = facturamaClient.timbrar(body);
            factura.setCfdiProveedorId(respuesta.proveedorId());
            factura.setCfdiUuid(respuesta.uuid());
            factura.setCfdiEstado("TIMBRADO");
            factura.setCfdiMensaje(null);
            facturaRepository.actualizarCfdi(factura);
        } catch (CfdiException e) {
            log.warn("CFDI no timbrado para factura {}: {}", factura.getFolioFactura(), e.getMessage());
            marcarError(factura, e.getMessage());
        } catch (SQLException e) {
            log.error("Error de BD al guardar estado CFDI", e);
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public byte[] descargarPdfTimbrado(Factura factura) {
        validarTimbrada(factura);
        return facturamaClient.descargarPdf(factura.getCfdiProveedorId());
    }

    @Override
    public byte[] descargarXmlTimbrado(Factura factura) {
        validarTimbrada(factura);
        return facturamaClient.descargarXml(factura.getCfdiProveedorId());
    }

    private void marcarError(Factura factura, String mensaje) {
        try {
            factura.setCfdiEstado("ERROR");
            factura.setCfdiMensaje(recortar(mensaje));
            facturaRepository.actualizarCfdi(factura);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private static void validarTimbrada(Factura factura) {
        if (factura == null || !factura.estaTimbrada()) {
            throw new ServiceJdbcException("Esta factura no tiene CFDI timbrado.", null);
        }
        if (factura.getCfdiProveedorId() == null || factura.getCfdiProveedorId().isBlank()) {
            throw new ServiceJdbcException("No hay referencia del proveedor de timbrado.", null);
        }
    }

    private static String recortar(String mensaje) {
        if (mensaje == null) {
            return null;
        }
        return mensaje.length() > 500 ? mensaje.substring(0, 497) + "..." : mensaje;
    }
}
