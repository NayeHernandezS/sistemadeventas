package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.TokenRecuperacion;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.TokenRecuperacionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.nhernandez.webapp.sistemaventas.util.PasswordEncodingHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecuperacionPasswordServiceImpl implements RecuperacionPasswordService {

    private final UsuarioReposository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final RecuperacionCorreoService correoService;
    private final PasswordEncoder passwordEncoder;

    @Value("${recuperacion.token.horas:2}")
    private int horasValidez;

    public RecuperacionPasswordServiceImpl(UsuarioReposository usuarioRepository,
                                           TokenRecuperacionRepository tokenRepository,
                                           RecuperacionCorreoService correoService,
                                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.correoService = correoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<String> solicitarPorEmail(String email, String baseUrl) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            Usuario usuario = usuarioRepository.porEmail(email.trim());
            if (usuario == null) {
                return Optional.empty();
            }

            tokenRepository.invalidarPorUsername(usuario.getUsername());

            String token = UUID.randomUUID().toString().replace("-", "");
            LocalDateTime ahora = LocalDateTime.now();
            TokenRecuperacion registro = new TokenRecuperacion();
            registro.setUsername(usuario.getUsername());
            registro.setToken(token);
            registro.setFechaCreacion(ahora);
            registro.setFechaExpiracion(ahora.plusHours(horasValidez > 0 ? horasValidez : 2));
            tokenRepository.guardar(registro);

            String enlace = baseUrl + "/recuperar/restablecer?token=" + token;
            return correoService.enviarEnlaceRecuperacion(usuario.getEmail(), enlace);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void restablecerConToken(String token, String passwordNueva) {
        if (passwordNueva == null || passwordNueva.length() < 4) {
            throw new ServiceJdbcException("La contraseña debe tener al menos 4 caracteres", null);
        }
        try {
            TokenRecuperacion registro = tokenRepository.porToken(token != null ? token.trim() : "");
            if (registro == null || !registro.esValido()) {
                throw new ServiceJdbcException("El enlace no es valido o ha expirado", null);
            }
            Usuario usuario = usuarioRepository.porUsername(registro.getUsername());
            if (usuario == null) {
                throw new ServiceJdbcException("Usuario no encontrado", null);
            }
            usuario.setPassword(PasswordEncodingHelper.encodeIfPlain(passwordEncoder, passwordNueva));
            usuarioRepository.guardar(usuario);
            tokenRepository.marcarUsado(registro.getId());
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public boolean tokenValido(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            TokenRecuperacion registro = tokenRepository.porToken(token.trim());
            return registro != null && registro.esValido();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }
}
