package org.nhernandez.webapp.sistemaventas.security;

import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UsuarioReposository usuarioRepository;

    public DbUserDetailsService(UsuarioReposository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Usuario usuario = usuarioRepository.porUsername(username);
            if (usuario == null) {
                throw new UsernameNotFoundException("Usuario no encontrado: " + username);
            }
            return new UsuarioPrincipal(usuario);
        } catch (SQLException e) {
            throw new AuthenticationServiceException("Error al consultar la base de datos", e);
        }
    }
}
