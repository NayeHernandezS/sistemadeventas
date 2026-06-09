package org.nhernandez.webapp.sistemaventas.pagos.mercadopago;

import com.fasterxml.jackson.databind.JsonNode;
import org.nhernandez.webapp.sistemaventas.config.MercadoPagoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MercadoPagoApiClient {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoApiClient.class);
    private static final String API_BASE = "https://api.mercadopago.com";

    private final RestClient restClient;
    private final MercadoPagoProperties properties;

    public MercadoPagoApiClient(MercadoPagoProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(API_BASE)
                .defaultHeader("Authorization", "Bearer " + tokenSeguro(properties.getAccessToken()))
                .build();
    }

    public PreferenciaCreada crearPreferencia(PreferenciaRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", List.of(Map.of(
                "title", request.titulo(),
                "quantity", 1,
                "unit_price", request.monto().doubleValue(),
                "currency_id", properties.getCurrencyId()
        )));
        body.put("external_reference", request.externalReference());
        if (request.notificationUrl() != null && !request.notificationUrl().isBlank()) {
            body.put("notification_url", request.notificationUrl());
        }
        body.put("back_urls", Map.of(
                "success", request.urlExito(),
                "failure", request.urlFallo(),
                "pending", request.urlPendiente()
        ));
        if (MercadoPagoUrls.admiteAutoReturn(request.urlExito())) {
            body.put("auto_return", "approved");
        }

        try {
            JsonNode json = restClient.post()
                    .uri("/checkout/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null) {
                throw new MercadoPagoException("Respuesta vacia al crear preferencia");
            }
            String preferenceId = texto(json, "id");
            String initPoint = resolverInitPoint(json);
            if (initPoint == null || initPoint.isBlank()) {
                throw new MercadoPagoException("Mercado Pago no devolvio URL de pago");
            }
            log.info("Checkout MP preferencia={} url={}", preferenceId, initPoint);
            return new PreferenciaCreada(preferenceId, initPoint);
        } catch (RestClientResponseException e) {
            String cuerpo = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 403 && cuerpo != null
                    && cuerpo.contains("PA_UNAUTHORIZED_RESULT_FROM_POLICIES")) {
                throw new MercadoPagoException(
                        "Mercado Pago rechazo la solicitud (403): revisa que el Access Token sea el de "
                                + "Credenciales de prueba en mercadopago.com.mx/developers (no el ejemplo del .env), "
                                + "que APP_BASE_URL no sea un dominio falso, y reinicia la aplicacion.",
                        e);
            }
            if (cuerpo != null && cuerpo.contains("invalid_auto_return")) {
                throw new MercadoPagoException(
                        "Mercado Pago requiere HTTPS publico para redireccion automatica. "
                                + "En local usa http://localhost:8080 (sin auto_return) o un tunel ngrok en APP_BASE_URL.",
                        e);
            }
            throw new MercadoPagoException("Error al crear preferencia: " + cuerpo, e);
        }
    }

    public List<PagoMercadoPago> buscarPagosPorReferenciaExterna(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            return List.of();
        }
        try {
            JsonNode json = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/payments/search")
                            .queryParam("sort", "date_created")
                            .queryParam("criteria", "desc")
                            .queryParam("external_reference", externalReference.trim())
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null || !json.has("results")) {
                return List.of();
            }
            List<PagoMercadoPago> pagos = new ArrayList<>();
            for (JsonNode result : json.get("results")) {
                String paymentId = texto(result, "id");
                if (paymentId == null || paymentId.isBlank()) {
                    continue;
                }
                pagos.add(new PagoMercadoPago(
                        paymentId,
                        texto(result, "status"),
                        texto(result, "external_reference"),
                        decimal(result, "transaction_amount"),
                        texto(result, "currency_id"),
                        resolverPreapprovalId(result)
                ));
            }
            return pagos;
        } catch (RestClientResponseException e) {
            throw new MercadoPagoException(
                    "Error al buscar pagos por referencia " + externalReference + ": " + e.getResponseBodyAsString(),
                    e);
        }
    }

    public Optional<PagoMercadoPago> consultarPago(long paymentId) {
        try {
            JsonNode json = restClient.get()
                    .uri("/v1/payments/{id}", paymentId)
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(new PagoMercadoPago(
                    String.valueOf(paymentId),
                    texto(json, "status"),
                    texto(json, "external_reference"),
                    decimal(json, "transaction_amount"),
                    texto(json, "currency_id"),
                    resolverPreapprovalId(json)
            ));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new MercadoPagoException("Error al consultar pago " + paymentId + ": " + e.getResponseBodyAsString(), e);
        }
    }

    public PreapprovalCreada crearPreapproval(PreapprovalRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reason", request.titulo());
        body.put("external_reference", request.externalReference());
        body.put("auto_recurring", Map.of(
                "frequency", 1,
                "frequency_type", "months",
                "transaction_amount", request.monto().doubleValue(),
                "currency_id", properties.getCurrencyId()
        ));
        if (request.backUrl() == null || !MercadoPagoUrls.admiteBackUrl(request.backUrl())) {
            throw new MercadoPagoException(MercadoPagoUrls.MENSAJE_BACK_URL_PREAPPROVAL);
        }
        body.put("back_url", request.backUrl().trim());
        if (request.payerEmail() != null && !request.payerEmail().isBlank()) {
            body.put("payer_email", request.payerEmail().trim());
        }
        if (request.notificationUrl() != null && !request.notificationUrl().isBlank()) {
            body.put("notification_url", request.notificationUrl());
        }

        try {
            JsonNode json = restClient.post()
                    .uri("/preapproval")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null) {
                throw new MercadoPagoException("Respuesta vacia al crear preapproval");
            }
            String preapprovalId = texto(json, "id");
            String initPoint = resolverInitPoint(json);
            if (initPoint == null || initPoint.isBlank()) {
                throw new MercadoPagoException("Mercado Pago no devolvio URL de suscripcion");
            }
            return new PreapprovalCreada(preapprovalId, initPoint);
        } catch (RestClientResponseException e) {
            String cuerpo = e.getResponseBodyAsString();
            if (cuerpo != null && cuerpo.contains("back_url")) {
                throw new MercadoPagoException(MercadoPagoUrls.MENSAJE_BACK_URL_PREAPPROVAL, e);
            }
            throw new MercadoPagoException("Error al crear suscripcion MP: " + cuerpo, e);
        }
    }

    public Optional<PreapprovalMercadoPago> consultarPreapproval(String preapprovalId) {
        try {
            JsonNode json = restClient.get()
                    .uri("/preapproval/{id}", preapprovalId)
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(new PreapprovalMercadoPago(
                    texto(json, "id"),
                    texto(json, "status"),
                    texto(json, "external_reference")
            ));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new MercadoPagoException("Error al consultar preapproval " + preapprovalId, e);
        }
    }

    public void cancelarPreapproval(String preapprovalId) {
        try {
            restClient.put()
                    .uri("/preapproval/{id}", preapprovalId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", "cancelled"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new MercadoPagoException("Error al cancelar suscripcion MP: " + e.getResponseBodyAsString(), e);
        }
    }

    private static String resolverPreapprovalId(JsonNode json) {
        String directo = texto(json, "preapproval_id");
        if (directo != null && !directo.isBlank()) {
            return directo;
        }
        JsonNode metadata = json.get("metadata");
        if (metadata != null && metadata.has("preapproval_id")) {
            return metadata.get("preapproval_id").asText();
        }
        JsonNode poi = json.get("point_of_interaction");
        if (poi != null) {
            JsonNode tx = poi.get("transaction_data");
            if (tx != null && tx.has("subscription_id")) {
                return tx.get("subscription_id").asText();
            }
        }
        return null;
    }

    private static String texto(JsonNode node, String field) {
        JsonNode child = node.get(field);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asText();
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        JsonNode child = node.get(field);
        if (child == null || child.isNull()) {
            return BigDecimal.ZERO;
        }
        return child.decimalValue();
    }

    private String resolverInitPoint(JsonNode json) {
        String sandbox = texto(json, "sandbox_init_point");
        String produccion = texto(json, "init_point");
        boolean tokenPrueba = tokenSeguro(properties.getAccessToken()).startsWith("TEST-");
        if (tokenPrueba) {
            return primeroNoVacio(sandbox, produccion);
        }
        return primeroNoVacio(produccion, sandbox);
    }

    private static String primeroNoVacio(String preferido, String alterno) {
        if (preferido != null && !preferido.isBlank()) {
            return preferido;
        }
        return alterno;
    }

    private static String tokenSeguro(String token) {
        return token == null ? "" : token.trim();
    }

    public record PreferenciaRequest(
            String titulo,
            BigDecimal monto,
            String externalReference,
            String notificationUrl,
            String urlExito,
            String urlFallo,
            String urlPendiente
    ) {
    }

    public record PreferenciaCreada(String preferenceId, String initPoint) {
    }

    public record PagoMercadoPago(
            String paymentId,
            String status,
            String externalReference,
            BigDecimal transactionAmount,
            String currencyId,
            String preapprovalId
    ) {
    }

    public record PreapprovalRequest(
            String titulo,
            BigDecimal monto,
            String externalReference,
            String backUrl,
            String notificationUrl,
            String payerEmail
    ) {
    }

    public record PreapprovalCreada(String preapprovalId, String initPoint) {
    }

    public record PreapprovalMercadoPago(
            String preapprovalId,
            String status,
            String externalReference
    ) {
    }
}
