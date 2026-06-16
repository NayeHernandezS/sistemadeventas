package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.cfdi.CfdiCredentials;
import org.nhernandez.webapp.sistemaventas.cfdi.CfdiException;
import org.nhernandez.webapp.sistemaventas.cfdi.facturama.FacturamaCfdiApiClient;
import org.nhernandez.webapp.sistemaventas.cfdi.facturama.FacturamaCfdiClientFactory;
import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;
import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.DatosFiscalesNegocioRepository;
import org.nhernandez.webapp.sistemaventas.repositories.FacturaRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CfdiTimbradoServiceImplTest {

    @Mock
    private TenantCfdiCredentialsResolver credentialsResolver;

    @Mock
    private FacturamaCfdiClientFactory clientFactory;

    @Mock
    private FacturamaCfdiApiClient facturamaClient;

    @Mock
    private DatosFiscalesNegocioRepository datosFiscalesRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private CfdiTimbradoServiceImpl service;

    @Test
    void reintentarTimbrar_rechazaSiCfdiNoConfigurado() {
        when(credentialsResolver.disponible("tienda1")).thenReturn(false);

        assertThrows(ServiceJdbcException.class,
                () -> service.reintentarTimbrar("tienda1", ticketConItems(), facturaPendiente()));
    }

    @Test
    void reintentarTimbrar_rechazaSiYaTimbrada() {
        when(credentialsResolver.disponible("tienda1")).thenReturn(true);
        Factura factura = facturaPendiente();
        factura.setCfdiEstado("TIMBRADO");
        factura.setCfdiUuid("UUID-123");

        assertThrows(ServiceJdbcException.class,
                () -> service.reintentarTimbrar("tienda1", ticketConItems(), factura));
    }

    @Test
    void reintentarTimbrar_devuelveMensajeErrorSiFallaTimbrado() throws SQLException {
        when(credentialsResolver.disponible("tienda1")).thenReturn(true);
        when(credentialsResolver.resolver("tienda1")).thenReturn(Optional.of(credenciales()));
        when(clientFactory.crear(any())).thenReturn(facturamaClient);
        DatosFiscalesNegocio emisor = emisorListo();
        when(datosFiscalesRepository.porTenant("tienda1")).thenReturn(emisor);
        when(facturamaClient.timbrar(any())).thenThrow(new CfdiException("RFC receptor invalido"));

        Factura factura = facturaPendiente();
        factura.setCodigoPostalReceptor("01000");

        String mensaje = service.reintentarTimbrar("tienda1", ticketConItems(), factura);

        assertTrue(mensaje.contains("No se pudo timbrar"));
        assertEquals("ERROR", factura.getCfdiEstado());
        verify(facturaRepository).actualizarCfdi(any(Factura.class));
    }

    @Test
    void reintentarTimbrar_exitosoCuandoFacturamaResponde() throws Exception {
        when(credentialsResolver.disponible("tienda1")).thenReturn(true);
        when(credentialsResolver.resolver("tienda1")).thenReturn(Optional.of(credenciales()));
        when(clientFactory.crear(any())).thenReturn(facturamaClient);
        when(datosFiscalesRepository.porTenant("tienda1")).thenReturn(emisorListo());
        when(facturamaClient.timbrar(any())).thenReturn(
                new FacturamaCfdiApiClient.CfdiTimbradoRespuesta("prov-1", "UUID-OK"));

        Factura factura = facturaPendiente();
        factura.setCodigoPostalReceptor("01000");

        String mensaje = service.reintentarTimbrar("tienda1", ticketConItems(), factura);

        assertTrue(mensaje.contains("timbrado correctamente"));
        assertEquals("TIMBRADO", factura.getCfdiEstado());
        assertEquals("UUID-OK", factura.getCfdiUuid());
    }

    private static CfdiCredentials credenciales() {
        return new CfdiCredentials("user", "pass", true);
    }

    private static DatosFiscalesNegocio emisorListo() {
        DatosFiscalesNegocio emisor = new DatosFiscalesNegocio();
        emisor.setRfc("XAXX010101000");
        emisor.setRazonSocial("Emisor");
        emisor.setCodigoPostal("01000");
        emisor.setRegimenFiscal("601");
        return emisor;
    }

    private static Factura facturaPendiente() {
        Factura f = new Factura();
        f.setId(10L);
        f.setFolioFactura("FAC-1");
        f.setRfc("XAXX010101000");
        f.setRazonSocial("Cliente");
        f.setCfdiEstado("ERROR");
        return f;
    }

    private static TicketVenta ticketConItems() {
        TicketItem item = new TicketItem();
        item.setNombreProducto("Producto");
        item.setCantidad(1);
        item.setPrecioUnitario(100);
        item.setImporte(100);
        TicketVenta ticket = new TicketVenta();
        ticket.setId(1L);
        ticket.setFolio("TCK-1");
        ticket.setItems(List.of(item));
        return ticket;
    }
}
