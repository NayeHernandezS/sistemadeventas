package org.nhernandez.webapp.sistemaventas.util;

public final class FacturaDatosUtil {

    private FacturaDatosUtil() {
    }

    public static String normalizarRfc(String rfc) {
        if (rfc == null) {
            return "";
        }
        return rfc.trim().toUpperCase().replaceAll("\\s", "");
    }

    public static boolean esRfcValido(String rfc) {
        String rfcNorm = normalizarRfc(rfc);
        return !rfcNorm.isBlank()
                && rfcNorm.length() >= 12
                && rfcNorm.length() <= 13
                && rfcNorm.matches("[A-ZÑ&0-9]+");
    }

    public static String validarRfcObligatorio(String rfc) {
        if (rfc == null || rfc.isBlank()) {
            return "Indica el RFC.";
        }
        if (!esRfcValido(rfc)) {
            return "RFC invalido (12 o 13 caracteres alfanumericos).";
        }
        return null;
    }
}
