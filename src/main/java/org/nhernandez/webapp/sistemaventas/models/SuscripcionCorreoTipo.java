package org.nhernandez.webapp.sistemaventas.models;

/**
 * Tipos de correo transaccional de suscripcion (evita reenvios duplicados).
 */
public final class SuscripcionCorreoTipo {

    public static final String VENCIDO = "VENCIDO";

    private SuscripcionCorreoTipo() {
    }

    public static String codigoAviso(int diasAntes) {
        return "AVISO_" + diasAntes;
    }

    public static boolean esVencido(String codigo) {
        return VENCIDO.equals(codigo);
    }
}
