package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.TokenRecuperacion;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.TokenRecuperacionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecuperacionPasswordServiceImplTest {

    @Mock
    private UsuarioReposository usuarioRepository;

    @Mock
    private TokenRecuperacionRepository tokenRepository;

    @Mock
    private RecuperacionCorreoService correoService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RecuperacionPasswordServiceImpl recuperacionPasswordService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(recuperacionPasswordService, "horasValidez", 2);
    }

    @Test
    void solicitarPorEmail_retornaVacioSiNoExisteUsuario() throws SQLException {
        when(usuarioRepository.porEmail("x@ejemplo.com")).thenReturn(null);

        Optional<String> result = recuperacionPasswordService.solicitarPorEmail(
                "x@ejemplo.com", "http://localhost:8080");

        assertTrue(result.isEmpty());
        verify(tokenRepository, never()).guardar(any());
    }

    @Test
    void solicitarPorEmail_generaTokenYEnviaCorreo() throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setUsername("tienda1");
        usuario.setEmail("tienda1@ejemplo.com");
        when(usuarioRepository.porEmail("tienda1@ejemplo.com")).thenReturn(usuario);
        when(correoService.enviarEnlaceRecuperacion(anyString(), anyString()))
                .thenReturn(Optional.of("http://localhost:8080/recuperar/restablecer?token=abc"));

        Optional<String> demo = recuperacionPasswordService.solicitarPorEmail(
                "tienda1@ejemplo.com", "http://localhost:8080");

        assertTrue(demo.isPresent());
        verify(tokenRepository).invalidarPorUsername("tienda1");
        verify(tokenRepository).guardar(any(TokenRecuperacion.class));
    }

    @Test
    void restablecerConToken_actualizaPasswordYMarcaTokenUsado() throws SQLException {
        TokenRecuperacion token = new TokenRecuperacion();
        token.setId(1L);
        token.setUsername("tienda1");
        token.setToken("tok123");
        token.setFechaExpiracion(LocalDateTime.now().plusHours(1));
        token.setUsado(false);

        Usuario usuario = new Usuario();
        usuario.setUsername("tienda1");
        usuario.setPassword("{noop}vieja");

        when(tokenRepository.porToken("tok123")).thenReturn(token);
        when(usuarioRepository.porUsername("tienda1")).thenReturn(usuario);
        when(passwordEncoder.encode("nueva123")).thenReturn("{bcrypt}hash");

        recuperacionPasswordService.restablecerConToken("tok123", "nueva123");

        verify(usuarioRepository).guardar(usuario);
        verify(tokenRepository).marcarUsado(1L);
        assertEquals("{bcrypt}hash", usuario.getPassword());
    }

    @Test
    void restablecerConToken_rechazaTokenExpirado() throws SQLException {
        TokenRecuperacion token = new TokenRecuperacion();
        token.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));
        token.setUsado(false);
        when(tokenRepository.porToken("exp")).thenReturn(token);

        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> recuperacionPasswordService.restablecerConToken("exp", "nueva123"));

        assertTrue(ex.getMessage().contains("valido"));
    }
}
