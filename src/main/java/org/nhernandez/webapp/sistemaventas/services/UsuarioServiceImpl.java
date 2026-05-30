package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.nhernandez.webapp.sistemaventas.util.CategoriaPlantillaUtil;
import org.nhernandez.webapp.sistemaventas.util.PasswordEncodingHelper;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioReposository usuarioReposository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private SuscripcionService suscripcionService;

    @Autowired
    private PlanLimiteService planLimiteService;

    @Autowired
    public UsuarioServiceImpl(UsuarioReposository usuarioReposository,
                              CategoriaRepository categoriaRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioReposository = usuarioReposository;
        this.categoriaRepository = categoriaRepository;
        this.passwordEncoder = passwordEncoder;
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
    public void registrarCuentaAdmin(Usuario usuario, String planCodigo) {
        try {
            if (usuarioReposository.existeUsername(usuario.getUsername())) {
                throw new ServiceJdbcException("El nombre de usuario ya esta registrado", null);
            }
            usuario.setRol(RolUtil.ROL_ADMIN);
            usuario.setAdminOwner(null);
            guardar(usuario);
            categoriaRepository.crearSugeridasSiNoExisten(
                    usuario.getUsername(),
                    CategoriaPlantillaUtil.paraTipoNegocio(usuario.getTipoNegocio())
            );
            suscripcionService.iniciarMesGratis(usuario.getUsername(), planCodigo);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }
}
