package org.nhernandez.webapp.sistemaventas.pagos.mercadopago;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MercadoPagoCheckoutServiceTest {

    @Test
    void parsearReferenciaInterna_prefijoPago() {
        assertEquals(42L, MercadoPagoCheckoutService.parsearReferenciaInterna("pago_42"));
    }

    @Test
    void parsearReferenciaInterna_soloNumero() {
        assertEquals(7L, MercadoPagoCheckoutService.parsearReferenciaInterna("7"));
    }

    @Test
    void parsearReferenciaInterna_invalida() {
        assertNull(MercadoPagoCheckoutService.parsearReferenciaInterna("otro-ref"));
    }

    @Test
    void montosCoinciden_aceptaDosDecimales() {
        assertTrue(MercadoPagoCheckoutService.montosCoinciden(
                new BigDecimal("298.00"), new BigDecimal("298")));
    }

    @Test
    void montosCoinciden_rechazaDiferencia() {
        assertFalse(MercadoPagoCheckoutService.montosCoinciden(
                new BigDecimal("298.00"), new BigDecimal("299.00")));
    }

    @Test
    void tokenConfigurado_rechazaPlaceholder() {
        assertFalse(MercadoPagoCheckoutService.tokenConfigurado("TEST-tu-token"));
    }

    @Test
    void tokenConfigurado_aceptaFormatoPrueba() {
        assertTrue(MercadoPagoCheckoutService.tokenConfigurado(
                "TEST-1234567890123456-012345-abcdefghijklmnopqrstuvwx-123456789"));
    }

    @Test
    void admiteAutoReturn_rechazaLocalhost() {
        assertFalse(MercadoPagoUrls.admiteAutoReturn("http://localhost:8080/suscripcion/pago-exitoso"));
    }

    @Test
    void admiteAutoReturn_aceptaHttpsPublico() {
        assertTrue(MercadoPagoUrls.admiteAutoReturn("https://ventas.midominio.com/suscripcion/pago-exitoso"));
    }
}
