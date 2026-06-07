package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.CategoriaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceRegistroTest {

    @Mock
    private UsuarioReposository usuarioReposository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private PreferenciasTenantService preferenciasTenantService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CatalogoPlantillaService catalogoPlantillaService;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioValido;

    @BeforeEach
    void setUp() {
        usuarioValido = new Usuario();
        usuarioValido.setUsername("mi_tienda");
        usuarioValido.setPassword("1234");
        usuarioValido.setEmail("tienda@ejemplo.com");
        usuarioValido.setTipoNegocio("belleza");
    }

    @Test
    void validarRegistro_rechazaContrasenasDistintas() {
        Map<String, String> errores = usuarioService.validarRegistroCuentaAdmin(usuarioValido, "otra");

        assertEquals("Las contraseñas no coinciden", errores.get("confirmarPassword"));
        assertTrue(errores.size() >= 1);
    }

    @Test
    void validarRegistro_rechazaUsuarioDuplicado() throws SQLException {
        when(usuarioReposository.existeUsername("mi_tienda")).thenReturn(true);

        Map<String, String> errores = usuarioService.validarRegistroCuentaAdmin(usuarioValido, "1234");

        assertEquals("El nombre de usuario ya esta registrado", errores.get("username"));
    }

    @Test
    void validarRegistro_rechazaEmailDuplicado() throws SQLException {
        Usuario otro = new Usuario();
        otro.setUsername("otro");
        when(usuarioReposository.existeUsername("mi_tienda")).thenReturn(false);
        when(usuarioReposository.porEmail("tienda@ejemplo.com")).thenReturn(otro);

        Map<String, String> errores = usuarioService.validarRegistroCuentaAdmin(usuarioValido, "1234");

        assertEquals("Ese email ya esta registrado", errores.get("email"));
    }

    @Test
    void registrarCuentaAdmin_noGuardaSiValidacionFalla() throws SQLException {
        when(usuarioReposository.existeUsername("mi_tienda")).thenReturn(true);

        assertThrows(ServiceJdbcException.class, () ->
                usuarioService.registrarCuentaAdmin(
                        usuarioValido,
                        LocalDateTime.now(),
                        "2026-01"));

        verify(usuarioReposository, never()).guardar(any());
    }
}
