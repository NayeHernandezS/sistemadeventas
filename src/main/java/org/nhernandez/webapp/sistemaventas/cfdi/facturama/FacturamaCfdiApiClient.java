package org.nhernandez.webapp.sistemaventas.cfdi.facturama;

import com.fasterxml.jackson.databind.JsonNode;
import org.nhernandez.webapp.sistemaventas.cfdi.CfdiException;
import org.nhernandez.webapp.sistemaventas.config.CfdiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class FacturamaCfdiApiClient {

    private final RestClient restClient;
    private final CfdiProperties properties;

    public FacturamaCfdiApiClient(CfdiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth(properties))
                .build();
    }

    public CfdiTimbradoRespuesta timbrar(Map<String, Object> body) {
        try {
            JsonNode json = restClient.post()
                    .uri("/api-lite/3/cfdis")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null) {
                throw new CfdiException("Facturama devolvio respuesta vacia al timbrar");
            }
            String id = texto(json, "Id");
            if (id == null || id.isBlank()) {
                id = texto(json, "id");
            }
            String uuid = extraerUuid(json);
            if (id == null || id.isBlank()) {
                throw new CfdiException("Facturama no devolvio el identificador del CFDI");
            }
            return new CfdiTimbradoRespuesta(id, uuid);
        } catch (RestClientResponseException e) {
            throw new CfdiException("Error al timbrar con Facturama: " + resumirError(e), e);
        }
    }

    public byte[] descargarPdf(String proveedorId) {
        return descargar("/Cfdi/pdf/issued/" + proveedorId);
    }

    public byte[] descargarXml(String proveedorId) {
        return descargar("/Cfdi/xml/issued/" + proveedorId);
    }

    private byte[] descargar(String path) {
        try {
            byte[] contenido = restClient.get()
                    .uri(path)
                    .accept(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_XML, MediaType.TEXT_XML)
                    .retrieve()
                    .body(byte[].class);
            if (contenido == null || contenido.length == 0) {
                throw new CfdiException("Facturama no devolvio el archivo solicitado");
            }
            return contenido;
        } catch (RestClientResponseException e) {
            throw new CfdiException("Error al descargar CFDI de Facturama: " + resumirError(e), e);
        }
    }

    private static String extraerUuid(JsonNode json) {
        JsonNode complement = json.get("Complement");
        if (complement != null) {
            JsonNode stamp = complement.get("TaxStamp");
            if (stamp != null) {
                String uuid = texto(stamp, "Uuid");
                if (uuid != null && !uuid.isBlank()) {
                    return uuid;
                }
            }
        }
        return texto(json, "Uuid");
    }

    private static String texto(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private static String resumirError(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        if (body != null && !body.isBlank()) {
            return body.length() > 400 ? body.substring(0, 400) + "..." : body;
        }
        return e.getStatusCode() + " " + e.getStatusText();
    }

    private static String basicAuth(CfdiProperties properties) {
        String raw = properties.getFacturamaUsername() + ":" + properties.getFacturamaPassword();
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public record CfdiTimbradoRespuesta(String proveedorId, String uuid) {
    }
}
