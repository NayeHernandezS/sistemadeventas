package org.nhernandez.webapp.sistemaventas.services;

import java.util.Optional;

public record ResultadoEnvioCorreo(boolean exito, String mensaje) {

    public static ResultadoEnvioCorreo ok() {
        return new ResultadoEnvioCorreo(true, null);
    }

    public static ResultadoEnvioCorreo error(String mensaje) {
        return new ResultadoEnvioCorreo(false, mensaje);
    }
}
