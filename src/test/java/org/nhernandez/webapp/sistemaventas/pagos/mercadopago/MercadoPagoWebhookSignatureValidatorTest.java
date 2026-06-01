package org.nhernandez.webapp.sistemaventas.pagos.mercadopago;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MercadoPagoWebhookSignatureValidatorTest {

    private static final String SECRET = "mi-secreto-webhook";

    @Test
    void parsearHeader_extraeTsYV1() {
        var partes = MercadoPagoWebhookSignatureValidator.parsearHeader(
                "ts=1742505638683,v1=abc123def456");

        assertEquals("1742505638683", partes.get("ts"));
        assertEquals("abc123def456", partes.get("v1"));
    }

    @Test
    void construirPlantilla_incluyePartesPresentes() {
        String plantilla = MercadoPagoWebhookSignatureValidator.construirPlantilla(
                "123456", "req-uuid", "1742505638683");

        assertEquals("id:123456;request-id:req-uuid;ts:1742505638683;", plantilla);
    }

    @Test
    void construirPlantilla_idAlfanumericoEnMinusculas() {
        String plantilla = MercadoPagoWebhookSignatureValidator.construirPlantilla(
                "ORD01ABC", null, "999");

        assertEquals("id:ord01abc;ts:999;", plantilla);
    }

    @Test
    void hmacSha256Hex_esDeterministico() {
        String plantilla = "id:123456;request-id:req-uuid;ts:1742505638683;";
        String hash1 = MercadoPagoWebhookSignatureValidator.hmacSha256Hex(SECRET, plantilla);
        String hash2 = MercadoPagoWebhookSignatureValidator.hmacSha256Hex(SECRET, plantilla);

        assertEquals(hash1, hash2);
        assertFalse(hash1.isBlank());
    }

    @Test
    void esValida_aceptaFirmaCorrecta() {
        String dataId = "123456";
        String xRequestId = "bb56a2f1-6aae-46ac-982e-9dcd3581d08e";
        String ts = "1742505638683";
        String plantilla = MercadoPagoWebhookSignatureValidator.construirPlantilla(dataId, xRequestId, ts);
        String v1 = MercadoPagoWebhookSignatureValidator.hmacSha256Hex(SECRET, plantilla);

        var props = new org.nhernandez.webapp.sistemaventas.config.MercadoPagoProperties();
        props.setWebhookSecret(SECRET);
        var validator = new MercadoPagoWebhookSignatureValidator(props);

        assertTrue(validator.esValida("ts=" + ts + ",v1=" + v1, xRequestId, dataId));
    }

    @Test
    void esValida_rechazaFirmaIncorrecta() {
        var props = new org.nhernandez.webapp.sistemaventas.config.MercadoPagoProperties();
        props.setWebhookSecret(SECRET);
        var validator = new MercadoPagoWebhookSignatureValidator(props);

        assertFalse(validator.esValida("ts=123,v1=deadbeef", "req-id", "456"));
    }

    @Test
    void esValida_omiteSiNoHaySecret() {
        var props = new org.nhernandez.webapp.sistemaventas.config.MercadoPagoProperties();
        var validator = new MercadoPagoWebhookSignatureValidator(props);

        assertTrue(validator.esValida(null, null, null));
    }
}
