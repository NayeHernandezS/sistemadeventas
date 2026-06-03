package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.config.MercadoPagoProperties;
import org.nhernandez.webapp.sistemaventas.models.MercadoPagoEstado;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoCheckoutService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoProduccionServiceTest {

    private static final String TOKEN_TEST =
            "TEST-1234567890123456-012345-abcdefghijklmnopqrstuvwx-123456789";

    @Mock
    private MercadoPagoProperties properties;

    @Mock
    private MercadoPagoCheckoutService checkoutService;

    @Mock
    private PagoSuscripcionExpiracionService expiracionService;

    @InjectMocks
    private MercadoPagoProduccionService service;

    @Test
    void evaluar_listoCuandoTokenSecretYUrlHttps() {
        when(properties.getAccessToken()).thenReturn(TOKEN_TEST);
        when(properties.getWebhookSecret()).thenReturn("mi-secret");
        when(checkoutService.habilitado()).thenReturn(true);
        when(expiracionService.getDiasMercadoPago()).thenReturn(15);
        ReflectionTestUtils.setField(service, "appBaseUrl", "https://ventas.ejemplo.com");

        MercadoPagoEstado estado = service.evaluar();

        assertTrue(estado.isListoProduccion());
        assertTrue(estado.getWebhookUrl().contains("/api/mercadopago/notificaciones"));
    }

    @Test
    void evaluar_advierteSinWebhookSecret() {
        when(properties.getAccessToken()).thenReturn(TOKEN_TEST);
        when(properties.getWebhookSecret()).thenReturn("");
        when(checkoutService.habilitado()).thenReturn(true);
        when(expiracionService.getDiasMercadoPago()).thenReturn(15);
        ReflectionTestUtils.setField(service, "appBaseUrl", "https://ventas.ejemplo.com");

        MercadoPagoEstado estado = service.evaluar();

        assertFalse(estado.isListoProduccion());
        assertTrue(estado.getAdvertencias().stream().anyMatch(a -> a.contains("WEBHOOK_SECRET")));
    }
}
