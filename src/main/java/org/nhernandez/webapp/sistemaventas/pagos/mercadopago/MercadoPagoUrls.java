package org.nhernandez.webapp.sistemaventas.pagos.mercadopago;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Validacion de URLs enviadas a Checkout Pro y Preapproval (MP rechaza dominios falsos o invalidos).
 */
public final class MercadoPagoUrls {

    public static final String MENSAJE_BACK_URL_PREAPPROVAL =
            "Renovacion automatica requiere APP_BASE_URL con HTTPS publico (sin localhost ni dominio de ejemplo). "
                    + "Configuralo en .env y reinicia la app, o usa un tunel (ngrok). Ver deploy/CHECKLIST_MERCADOPAGO.md";

    private MercadoPagoUrls() {
    }

    public static boolean esPlaceholder(String url) {
        if (url == null || url.isBlank()) {
            return true;
        }
        String u = url.toLowerCase();
        return u.contains("tu-dominio")
                || u.contains("tudominio")
                || u.contains("example.com");
    }

    public static boolean admiteWebhook(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank() || esPlaceholder(baseUrl)) {
            return false;
        }
        String u = baseUrl.toLowerCase();
        if (u.contains("localhost") || u.contains("127.0.0.1")) {
            return false;
        }
        return u.startsWith("https://");
    }

    public static boolean admiteAutoReturn(String urlExito) {
        return admiteBackUrl(urlExito);
    }

    /**
     * Preapproval (renovacion automatica) exige {@code back_url} HTTPS publica y absoluta.
     */
    public static boolean admiteBackUrl(String url) {
        if (url == null || url.isBlank() || esPlaceholder(url) || !esUrlAbsolutaValida(url)) {
            return false;
        }
        String u = url.toLowerCase();
        if (u.contains("localhost") || u.contains("127.0.0.1")) {
            return false;
        }
        return u.startsWith("https://");
    }

    public static boolean esUrlAbsolutaValida(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(url.trim());
            return uri.isAbsolute() && uri.getScheme() != null && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * URL de retorno tras autorizar la suscripcion en MP (Preapproval).
     * Prioriza APP_BASE_URL si es HTTPS valido; si no, la URL de la peticion actual.
     */
    public static String resolverUrlRetorno(String appBaseUrl, String baseUrlSolicitud, String pathRelativo) {
        String path = normalizarPath(pathRelativo);
        for (String base : candidatosBase(appBaseUrl, baseUrlSolicitud)) {
            String url = sinBarraFinal(base) + path;
            if (admiteBackUrl(url)) {
                return url;
            }
        }
        return null;
    }

    private static String normalizarPath(String pathRelativo) {
        if (pathRelativo == null || pathRelativo.isBlank()) {
            return "/";
        }
        return pathRelativo.startsWith("/") ? pathRelativo : "/" + pathRelativo;
    }

    private static List<String> candidatosBase(String appBaseUrl, String baseUrlSolicitud) {
        List<String> bases = new ArrayList<>(2);
        if (appBaseUrl != null && !appBaseUrl.isBlank() && !esPlaceholder(appBaseUrl)) {
            bases.add(appBaseUrl.trim());
        }
        if (baseUrlSolicitud != null && !baseUrlSolicitud.isBlank()) {
            bases.add(baseUrlSolicitud.trim());
        }
        return bases;
    }

    public static String resolverBase(String appBaseUrl, String baseUrlSolicitud) {
        if (appBaseUrl != null && !appBaseUrl.isBlank() && !esPlaceholder(appBaseUrl)) {
            return sinBarraFinal(appBaseUrl.trim());
        }
        if (baseUrlSolicitud != null && !baseUrlSolicitud.isBlank()) {
            return sinBarraFinal(baseUrlSolicitud.trim());
        }
        throw new IllegalArgumentException("No hay URL base valida para Mercado Pago");
    }

    public static String sinBarraFinal(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String urlNotificacionOpcional(String appBaseUrl, String baseUrlSolicitud) {
        String base = resolverBase(appBaseUrl, baseUrlSolicitud);
        if (!admiteWebhook(base)) {
            return null;
        }
        return base + "/api/mercadopago/notificaciones";
    }
}
