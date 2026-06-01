package org.nhernandez.webapp.sistemaventas.pagos.mercadopago;

/**
 * Validacion de URLs enviadas a Checkout Pro (MP rechaza dominios falsos o invalidos).
 */
public final class MercadoPagoUrls {

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
        if (urlExito == null || urlExito.isBlank() || esPlaceholder(urlExito)) {
            return false;
        }
        String u = urlExito.toLowerCase();
        if (u.contains("localhost") || u.contains("127.0.0.1")) {
            return false;
        }
        return u.startsWith("https://");
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
