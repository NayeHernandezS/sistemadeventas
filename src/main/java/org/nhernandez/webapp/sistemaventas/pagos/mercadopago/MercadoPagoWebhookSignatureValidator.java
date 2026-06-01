package org.nhernandez.webapp.sistemaventas.pagos.mercadopago;

import org.nhernandez.webapp.sistemaventas.config.MercadoPagoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Valida la firma {@code x-signature} de notificaciones webhook de Mercado Pago.
 * @see <a href="https://www.mercadopago.com.mx/developers/es/docs/your-integrations/notifications/webhooks">Webhooks MP</a>
 */
@Component
public class MercadoPagoWebhookSignatureValidator {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoWebhookSignatureValidator.class);

    private final MercadoPagoProperties properties;

    public MercadoPagoWebhookSignatureValidator(MercadoPagoProperties properties) {
        this.properties = properties;
    }

    /**
     * @return true si la firma es valida o si no hay secret configurado (modo desarrollo).
     */
    public boolean esValida(String xSignature, String xRequestId, String dataId) {
        String secret = properties.getWebhookSecret();
        if (secret == null || secret.isBlank()) {
            log.debug("Webhook MP sin MERCADOPAGO_WEBHOOK_SECRET: se omite validacion de firma");
            return true;
        }
        if (xSignature == null || xSignature.isBlank()) {
            log.warn("Webhook MP rechazado: falta header x-signature");
            return false;
        }
        Map<String, String> partes = parsearHeader(xSignature);
        String ts = partes.get("ts");
        String v1 = partes.get("v1");
        if (ts == null || v1 == null) {
            log.warn("Webhook MP rechazado: x-signature incompleto");
            return false;
        }
        String plantilla = construirPlantilla(dataId, xRequestId, ts);
        String calculada = hmacSha256Hex(secret.trim(), plantilla);
        boolean valida = MessageDigest.isEqual(
                calculada.getBytes(StandardCharsets.UTF_8),
                v1.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        if (!valida) {
            log.warn("Webhook MP rechazado: firma invalida para data.id={}", dataId);
        }
        return valida;
    }

    static Map<String, String> parsearHeader(String xSignature) {
        return java.util.Arrays.stream(xSignature.split(","))
                .map(String::trim)
                .filter(p -> p.contains("="))
                .map(p -> p.split("=", 2))
                .collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim(), (a, b) -> b));
    }

    static String construirPlantilla(String dataId, String xRequestId, String ts) {
        StringBuilder sb = new StringBuilder();
        if (dataId != null && !dataId.isBlank()) {
            sb.append("id:").append(dataId.trim().toLowerCase(Locale.ROOT)).append(';');
        }
        if (xRequestId != null && !xRequestId.isBlank()) {
            sb.append("request-id:").append(xRequestId.trim()).append(';');
        }
        if (ts != null && !ts.isBlank()) {
            sb.append("ts:").append(ts.trim()).append(';');
        }
        return sb.toString();
    }

    static String hmacSha256Hex(String secret, String mensaje) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(mensaje.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular HMAC SHA256", e);
        }
    }
}
