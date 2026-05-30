package org.nhernandez.webapp.sistemaventas.services;

import java.util.Optional;

public interface RecuperacionPasswordService {

    /**
     * @return enlace de restablecimiento solo en modo demo (sin SMTP configurado)
     */
    Optional<String> solicitarPorEmail(String email, String baseUrl);

    void restablecerConToken(String token, String passwordNueva);

    boolean tokenValido(String token);
}
