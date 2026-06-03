package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    List<Usuario> listarVendedoresDelTenant(String tenantOwner);

    Optional<Usuario> porIdDeTenant(Long id, String tenantOwner);

    void guardar(Usuario usuario);

    void guardarVendedor(Usuario usuario, String tenantOwner);

    void eliminarDeTenant(Long id, String tenantOwner);

    void registrarCuentaAdmin(Usuario usuario, String planCodigo);

    void registrarCuentaAdmin(Usuario usuario, String planCodigo,
                              LocalDateTime aceptacionLegalEn, String aceptacionLegalVersion);

    void cambiarPassword(String username, String passwordActual, String passwordNueva);

    Optional<Usuario> porUsername(String username);

    void actualizarEmail(String username, String emailNuevo);

    void actualizarTipoNegocio(String username, String tipoNegocio);
}
