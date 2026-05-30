package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    List<Usuario> listarVendedoresDelTenant(String tenantOwner);

    Optional<Usuario> porIdDeTenant(Long id, String tenantOwner);

    void guardar(Usuario usuario);

    void guardarVendedor(Usuario usuario, String tenantOwner);

    void eliminarDeTenant(Long id, String tenantOwner);

    void registrarCuentaAdmin(Usuario usuario, String planCodigo);
}
