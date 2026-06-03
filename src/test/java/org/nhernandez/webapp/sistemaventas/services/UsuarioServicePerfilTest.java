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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServicePerfilTest {

    @Mock
    private UsuarioReposository usuarioReposository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SuscripcionService suscripcionService;

    @Mock
    private PlanLimiteService planLimiteService;

    @Mock
    private PreferenciasTenantService preferenciasTenantService;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("tienda1");
        usuario.setEmail("viejo@ejemplo.com");
        usuario.setPassword("{bcrypt}hash");
        usuario.setRol("ADMIN");
    }

    @Test
    void porUsername_devuelveUsuario() throws SQLException {
        when(usuarioReposository.porUsername("tienda1")).thenReturn(usuario);

        Optional<Usuario> result = usuarioService.porUsername("tienda1");

        assertEquals("tienda1", result.orElseThrow().getUsername());
    }

    @Test
    void actualizarEmail_guardaNuevoEmail() throws SQLException {
        when(usuarioReposository.porUsername("tienda1")).thenReturn(usuario);
        when(usuarioReposository.porEmail("nuevo@ejemplo.com")).thenReturn(null);

        usuarioService.actualizarEmail("tienda1", "nuevo@ejemplo.com");

        assertEquals("nuevo@ejemplo.com", usuario.getEmail());
        verify(usuarioReposository).guardar(usuario);
    }

    @Test
    void actualizarEmail_rechazaEmailDuplicado() throws SQLException {
        Usuario otro = new Usuario();
        otro.setUsername("otraCuenta");
        when(usuarioReposository.porUsername("tienda1")).thenReturn(usuario);
        when(usuarioReposository.porEmail("nuevo@ejemplo.com")).thenReturn(otro);

        assertThrows(ServiceJdbcException.class,
                () -> usuarioService.actualizarEmail("tienda1", "nuevo@ejemplo.com"));
        verify(usuarioReposository, never()).guardar(any());
    }

    @Test
    void actualizarEmail_rechazaEmailInvalido() {
        assertThrows(ServiceJdbcException.class,
                () -> usuarioService.actualizarEmail("tienda1", "correo-invalido"));
    }

    @Test
    void actualizarTipoNegocio_guardaRubroValido() throws SQLException {
        when(usuarioReposository.porUsername("tienda1")).thenReturn(usuario);

        usuarioService.actualizarTipoNegocio("tienda1", "ferreteria");

        assertEquals("ferreteria", usuario.getTipoNegocio());
        verify(usuarioReposository).guardar(usuario);
    }

    @Test
    void actualizarTipoNegocio_rechazaRubroInvalido() {
        assertThrows(ServiceJdbcException.class,
                () -> usuarioService.actualizarTipoNegocio("tienda1", "invalido"));
    }
}
