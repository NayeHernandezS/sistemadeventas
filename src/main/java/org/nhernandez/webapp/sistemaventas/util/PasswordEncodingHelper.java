package org.nhernandez.webapp.sistemaventas.util;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Formato de contraseñas compatible con {@link org.springframework.security.crypto.password.DelegatingPasswordEncoder}.
 * Contraseñas legacy sin prefijo se tratan como {@code {noop}} al autenticar.
 */
public final class PasswordEncodingHelper {

    private PasswordEncodingHelper() {
    }

    public static String toAuthenticationFormat(String stored) {
        if (stored == null || stored.isBlank()) {
            return "";
        }
        if (stored.startsWith("{")) {
            return stored;
        }
        return "{noop}" + stored;
    }

    public static String encodeIfPlain(PasswordEncoder encoder, String password) {
        if (password == null || password.isBlank()) {
            return password;
        }
        if (password.startsWith("{")) {
            return password;
        }
        return encoder.encode(password);
    }

    public static boolean matches(PasswordEncoder encoder, String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        return encoder.matches(rawPassword, toAuthenticationFormat(storedPassword));
    }
}
