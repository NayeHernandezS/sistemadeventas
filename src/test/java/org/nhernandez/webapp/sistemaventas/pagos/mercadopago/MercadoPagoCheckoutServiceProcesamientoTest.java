package org.nhernandez.webapp.sistemaventas.pagos.mercadopago;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.config.MercadoPagoProperties;
import org.nhernandez.webapp.sistemaventas.services.RenovacionAutomaticaService;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoCheckoutServiceProcesamientoTest {

    @Mock
    private MercadoPagoProperties properties;

    @Mock
    private MercadoPagoApiClient apiClient;

    @Mock
    private SuscripcionService suscripcionService;

    @Mock
    private RenovacionAutomaticaService renovacionAutomaticaService;

    private MercadoPagoCheckoutService service;

    @BeforeEach
    void setUp() {
        when(properties.getAccessToken()).thenReturn(
                "TEST-1234567890123456-012345-abcdefghijklmnopqrstuvwx-123456789");
        service = new MercadoPagoCheckoutService(properties, apiClient, suscripcionService,
                renovacionAutomaticaService);
    }

    @Test
    void procesarPagoPorId_aprobado_confirmaUnaVez() {
        var pagoMp = new MercadoPagoApiClient.PagoMercadoPago(
                "555", "approved", "pago_12", new BigDecimal("149.00"), "MXN", null);
        when(apiClient.consultarPago(555L)).thenReturn(Optional.of(pagoMp));

        service.procesarPagoPorId(555L);
        service.procesarPagoPorId(555L);

        verify(suscripcionService, times(2)).confirmarPagoMercadoPago(
                eq(12L), eq("555"), eq(new BigDecimal("149.00")), eq("MXN"));
    }

    @Test
    void procesarPagoPorId_pendiente_noConfirma() {
        var pagoMp = new MercadoPagoApiClient.PagoMercadoPago(
                "556", "pending", "pago_13", new BigDecimal("149.00"), "MXN", null);
        when(apiClient.consultarPago(556L)).thenReturn(Optional.of(pagoMp));

        service.procesarPagoPorId(556L);

        verify(suscripcionService, never()).confirmarPagoMercadoPago(any(), any(), any(), any());
    }
}
