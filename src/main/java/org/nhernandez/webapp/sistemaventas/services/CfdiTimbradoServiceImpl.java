package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.cfdi.CfdiCredentials;
import org.nhernandez.webapp.sistemaventas.cfdi.CfdiException;
import org.nhernandez.webapp.sistemaventas.cfdi.facturama.FacturamaCfdiApiClient;
import org.nhernandez.webapp.sistemaventas.cfdi.facturama.FacturamaCfdiClientFactory;
import org.nhernandez.webapp.sistemaventas.cfdi.facturama.FacturamaCfdiRequestBuilder;
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
import java.util.Optional;

@Service
public class CfdiTimbradoServiceImpl implements CfdiTimbradoService {

    private static final Logger log = LoggerFactory.getLogger(CfdiTimbradoServiceImpl.class);

    private final TenantCfdiCredentialsResolver credentialsResolver;
    private final FacturamaCfdiClientFactory clientFactory;
    private final DatosFiscalesNegocioRepository datosFiscalesRepository;
    private final FacturaRepository facturaRepository;

    public CfdiTimbradoServiceImpl(TenantCfdiCredentialsResolver credentialsResolver,
                                   FacturamaCfdiClientFactory clientFactory,
                                   DatosFiscalesNegocioRepository datosFiscalesRepository,
                                   FacturaRepository facturaRepository) {
        this.credentialsResolver = credentialsResolver;
        this.clientFactory = clientFactory;
        this.datosFiscalesRepository = datosFiscalesRepository;
        this.facturaRepository = facturaRepository;
    }

    @Override
    public boolean disponible(String tenantOwner) {
        return credentialsResolver.disponible(tenantOwner);
    }

    @Override
    public boolean usaCredencialesTenant(String tenantOwner) {
        return credentialsResolver.usaCredencialesTenant(tenantOwner);
    }

    @Override
    public String reintentarTimbrar(String tenantOwner, TicketVenta ticket, Factura factura) {
        if (!disponible(tenantOwner)) {
            throw new ServiceJdbcException(
                    "Timbrado CFDI no esta configurado. Conecta tu cuenta Facturama en Mi perfil.", null);
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
        if (!disponible(tenantOwner) || factura == null || factura.getId() == null) {
            return;
        }
        Optional<CfdiCredentials> credenciales = credentialsResolver.resolver(tenantOwner);
        if (credenciales.isEmpty()) {
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
            FacturamaCfdiApiClient client = clientFactory.crear(credenciales.get());
            Map<String, Object> body = FacturamaCfdiRequestBuilder.construir(emisor, factura, ticket);
            FacturamaCfdiApiClient.CfdiTimbradoRespuesta respuesta = client.timbrar(body);
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
    public byte[] descargarPdfTimbrado(String tenantOwner, Factura factura) {
        return descargarArchivo(tenantOwner, factura, true);
    }

    @Override
    public byte[] descargarXmlTimbrado(String tenantOwner, Factura factura) {
        return descargarArchivo(tenantOwner, factura, false);
    }

    private byte[] descargarArchivo(String tenantOwner, Factura factura, boolean pdf) {
        validarTimbrada(factura);
        CfdiCredentials credenciales = credentialsResolver.resolver(tenantOwner)
                .orElseThrow(() -> new ServiceJdbcException(
                        "Timbrado CFDI no configurado para este negocio.", null));
        FacturamaCfdiApiClient client = clientFactory.crear(credenciales);
        return pdf ? client.descargarPdf(factura.getCfdiProveedorId()) : client.descargarXml(factura.getCfdiProveedorId());
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
