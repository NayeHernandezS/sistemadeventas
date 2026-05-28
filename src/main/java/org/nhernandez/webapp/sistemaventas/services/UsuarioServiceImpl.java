package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.nhernandez.webapp.sistemaventas.util.CategoriaPlantillaUtil;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioReposository usuarioReposository;
    private final CategoriaRepository categoriaRepository;

    @Autowired
    private SuscripcionService suscripcionService;

    @Autowired
    public UsuarioServiceImpl(UsuarioReposository usuarioReposository,
                              CategoriaRepository categoriaRepository) {
        this.usuarioReposository = usuarioReposository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public Optional<Usuario> login(String username, String password) {
        try {
            return Optional.ofNullable(usuarioReposository.porUsername(username))
                    .filter(u -> Objects.equals(u.getPassword(), password));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
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
        try {
            usuarioReposository.guardar(usuario);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void guardarVendedor(Usuario usuario, String tenantOwner) {
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
    public void registrarCuentaAdmin(Usuario usuario) {
        try {
            if (usuarioReposository.existeUsername(usuario.getUsername())) {
                throw new ServiceJdbcException("El nombre de usuario ya esta registrado", null);
            }
            usuario.setRol(RolUtil.ROL_ADMIN);
            usuario.setAdminOwner(null);
            usuarioReposository.guardar(usuario);
            categoriaRepository.crearSugeridasSiNoExisten(
                    usuario.getUsername(),
                    CategoriaPlantillaUtil.paraTipoNegocio(usuario.getTipoNegocio())
            );
            suscripcionService.iniciarMesGratis(usuario.getUsername());
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }
}
