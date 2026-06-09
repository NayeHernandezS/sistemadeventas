package org.nhernandez.webapp.sistemaventas.web;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoCheckoutService;
import org.nhernandez.webapp.sistemaventas.pagos.mercadopago.MercadoPagoWebhookSignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * IPN / Webhooks de Mercado Pago (Checkout Pro). Debe ser URL publica HTTPS en produccion.
 */
@RestController
@RequestMapping("/api/mercadopago")
public class MercadoPagoWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoWebhookController.class);

    private final MercadoPagoCheckoutService mercadoPagoCheckoutService;
    private final MercadoPagoWebhookSignatureValidator signatureValidator;

    public MercadoPagoWebhookController(MercadoPagoCheckoutService mercadoPagoCheckoutService,
                                        MercadoPagoWebhookSignatureValidator signatureValidator) {
        this.mercadoPagoCheckoutService = mercadoPagoCheckoutService;
        this.signatureValidator = signatureValidator;
    }

    @GetMapping({"/notificaciones", "/notificaciones/", "/notificacion", "/notificacion/"})
    public ResponseEntity<Void> notificacionGet(
            HttpServletRequest request,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "data.id", required = false) String dataIdQuery) {
        log.info("Webhook MP GET {} query={}", request.getRequestURI(), request.getQueryString());
        String dataId = resolverDataId(id, dataIdQuery, null);
        if (!signatureValidator.esValida(request.getHeader("x-signature"), request.getHeader("x-request-id"), dataId)) {
            return ResponseEntity.status(401).build();
        }
        procesar(topic, id, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/notificaciones", "/notificaciones/", "/notificacion", "/notificacion/"})
    public ResponseEntity<Void> notificacionPost(
            HttpServletRequest request,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "data.id", required = false) String dataIdQuery,
            @RequestBody(required = false) JsonNode cuerpo) {
        log.info("Webhook MP POST {} query={}", request.getRequestURI(), request.getQueryString());
        String dataId = resolverDataId(id, dataIdQuery, cuerpo);
        if (!signatureValidator.esValida(request.getHeader("x-signature"), request.getHeader("x-request-id"), dataId)) {
            return ResponseEntity.status(401).build();
        }
        procesar(topic, id, cuerpo);
        return ResponseEntity.ok().build();
    }

    private static String resolverDataId(String idParam, String dataIdQuery, JsonNode cuerpo) {
        if (dataIdQuery != null && !dataIdQuery.isBlank()) {
            return dataIdQuery.trim();
        }
        if (idParam != null && !idParam.isBlank()) {
            return idParam.trim();
        }
        if (cuerpo != null) {
            JsonNode data = cuerpo.get("data");
            if (data != null && data.has("id")) {
                return data.get("id").asText();
            }
        }
        return null;
    }

    private void procesar(String topic, String id, JsonNode cuerpo) {
        try {
            mercadoPagoCheckoutService.procesarNotificacion(topic, id, cuerpo);
        } catch (Exception e) {
            log.error("Error procesando notificacion Mercado Pago topic={} id={}", topic, id, e);
        }
    }
}
