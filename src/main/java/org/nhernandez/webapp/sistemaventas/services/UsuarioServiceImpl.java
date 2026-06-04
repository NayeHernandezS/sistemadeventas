package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.nhernandez.webapp.sistemaventas.legal.DocumentosLegales;
import org.nhernandez.webapp.sistemaventas.util.CategoriaPlantillaUtil;
import org.nhernandez.webapp.sistemaventas.util.PasswordEncodingHelper;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioReposository usuarioReposository;
    private final CategoriaRepository categoriaRepository;
    private final PreferenciasTenantService preferenciasTenantService;
    private final PasswordEncoder passwordEncoder;
    private final CatalogoPlantillaService catalogoPlantillaService;

    @Autowired
    private PlanLimiteService planLimiteService;

    @Autowired
    public UsuarioServiceImpl(UsuarioReposository usuarioReposository,
                              CategoriaRepository categoriaRepository,
                              PreferenciasTenantService preferenciasTenantService,
                              PasswordEncoder passwordEncoder,
                              CatalogoPlantillaService catalogoPlantillaService) {
        this.usuarioReposository = usuarioReposository;
        this.categoriaRepository = categoriaRepository;
        this.preferenciasTenantService = preferenciasTenantService;
        this.passwordEncoder = passwordEncoder;
        this.catalogoPlantillaService = catalogoPlantillaService;
    }

    @Override
    public List<Usuario> listarVendedoresDelTenant(String tenantOwner) {
        try {
            return usuarioReposository.listarPorAdminOwner(tenantOwner);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Optional<Usuario> porIdDeTenant(Long id, String tenantOwner) {
        try {
            return Optional.ofNullable(usuarioReposository.porIdDeTenant(id, tenantOwner));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void guardar(Usuario usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(PasswordEncodingHelper.encodeIfPlain(passwordEncoder, usuario.getPassword()));
        }
        try {
            usuarioReposository.guardar(usuario);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void guardarVendedor(Usuario usuario, String tenantOwner) {
        if (usuario.getId() == null || usuario.getId() <= 0) {
            planLimiteService.validarNuevoVendedor(tenantOwner);
        }
        usuario.setRol(RolUtil.ROL_VENDEDOR);
        usuario.setAdminOwner(tenantOwner);
        guardar(usuario);
    }

    @Override
    public void eliminarDeTenant(Long id, String tenantOwner) {
        try {
            Usuario usuario = usuarioReposository.porIdDeTenant(id, tenantOwner);
            if (usuario == null) {
                throw new ServiceJdbcException("Usuario no encontrado en tu cuenta", null);
            }
            usuarioReposository.eliminar(id);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void registrarCuentaAdmin(Usuario usuario,
                                     LocalDateTime aceptacionLegalEn, String aceptacionLegalVersion) {
        try {
            if (usuarioReposository.existeUsername(usuario.getUsername())) {
                throw new ServiceJdbcException("El nombre de usuario ya esta registrado", null);
            }
            if (aceptacionLegalEn == null || aceptacionLegalVersion == null || aceptacionLegalVersion.isBlank()) {
                throw new ServiceJdbcException("Se requiere aceptacion de terminos y privacidad", null);
            }
            usuario.setRol(RolUtil.ROL_ADMIN);
            usuario.setAdminOwner(null);
            usuario.setAceptacionLegalEn(aceptacionLegalEn);
            usuario.setAceptacionLegalVersion(aceptacionLegalVersion.trim());
            guardar(usuario);
            categoriaRepository.crearSugeridasSiNoExisten(
                    usuario.getUsername(),
                    CategoriaPlantillaUtil.todasCategoriasParaTipoNegocio(usuario.getTipoNegocio())
            );
            preferenciasTenantService.iniciarOnboarding(usuario.getUsername());
            catalogoPlantillaService.importarCatalogoInicial(
                    usuario.getUsername(),
                    usuario.getTipoNegocio()
            );
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Optional<Usuario> porUsername(String username) {
        try {
            return Optional.ofNullable(usuarioReposository.porUsername(username));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void actualizarEmail(String username, String emailNuevo) {
        if (emailNuevo == null || emailNuevo.isBlank()) {
            throw new ServiceJdbcException("El email es requerido", null);
        }
        String email = emailNuevo.trim();
        if (!email.contains("@") || email.length() < 5) {
            throw new ServiceJdbcException("Indica un email valido", null);
        }
        try {
            Usuario usuario = usuarioReposository.porUsername(username);
            if (usuario == null) {
                throw new ServiceJdbcException("Usuario no encontrado", null);
            }
            Usuario otro = usuarioReposository.porEmail(email);
            if (otro != null && !otro.getUsername().equalsIgnoreCase(username)) {
                throw new ServiceJdbcException("Ese email ya esta registrado en otra cuenta", null);
            }
            usuario.setEmail(email);
            usuarioReposository.guardar(usuario);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void actualizarTipoNegocio(String username, String tipoNegocio) {
        if (!TipoNegocioUtil.esValido(tipoNegocio)) {
            throw new ServiceJdbcException("Selecciona un tipo de negocio valido", null);
        }
        try {
            Usuario usuario = usuarioReposository.porUsername(username);
            if (usuario == null) {
                throw new ServiceJdbcException("Usuario no encontrado", null);
            }
            if (!RolUtil.esAdmin(usuario)) {
                throw new ServiceJdbcException("Solo el administrador puede cambiar el tipo de negocio", null);
            }
            usuario.setTipoNegocio(tipoNegocio.trim().toLowerCase());
            usuarioReposository.guardar(usuario);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void cambiarPassword(String username, String passwordActual, String passwordNueva) {
        if (passwordNueva == null || passwordNueva.length() < 4) {
            throw new ServiceJdbcException("La nueva contraseña debe tener al menos 4 caracteres", null);
        }
        try {
            Usuario usuario = usuarioReposository.porUsername(username);
            if (usuario == null) {
                throw new ServiceJdbcException("Usuario no encontrado", null);
            }
            if (!PasswordEncodingHelper.matches(passwordEncoder, passwordActual, usuario.getPassword())) {
                throw new ServiceJdbcException("La contraseña actual no es correcta", null);
            }
            usuario.setPassword(PasswordEncodingHelper.encodeIfPlain(passwordEncoder, passwordNueva));
            usuarioReposository.guardar(usuario);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }
}
