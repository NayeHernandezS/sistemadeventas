package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioRepositoryImp implements UsuarioReposository {
    @Autowired
    @MysqlConn
    private Connection conn;

    @Override
    public boolean existeUsername(String username) throws SQLException {
        return porUsername(username) != null;
    }

    @Override
    public Usuario porUsername(String username) throws SQLException {
        Usuario usuario = null;
        try (PreparedStatement stmt = conn.prepareStatement("select * from usuarios where username=?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = getUsuario(rs);
                }
            }
        }
        return usuario;
    }

    @Override
    public List<Usuario> listar() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select * from usuarios")) {
            while (rs.next()) {
                usuarios.add(getUsuario(rs));
            }
        }
        return usuarios;
    }

    @Override
    public List<Usuario> listarPorAdminOwner(String adminOwner) throws SQLException {
        String sql = "select * from usuarios where admin_owner = ? order by username";
        List<Usuario> usuarios = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, adminOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(getUsuario(rs));
                }
            }
        }
        return usuarios;
    }

    @Override
    public Usuario porId(Long id) throws SQLException {
        Usuario usuario = null;
        try (PreparedStatement stmt = conn.prepareStatement("select * from usuarios where id=?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = getUsuario(rs);
                }
            }
        }
        return usuario;
    }

    @Override
    public Usuario porIdDeTenant(Long id, String adminOwner) throws SQLException {
        String sql = "select * from usuarios where id = ? and admin_owner = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, adminOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getUsuario(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void guardar(Usuario usuario) throws SQLException {
        String sql;
        if (usuario.getId() != null && usuario.getId() > 0) {
            sql = "update usuarios set username=?, password=?, email=?, rol=?, admin_owner=?, tipo_negocio=? where id=?";
        } else {
            sql = "insert into usuarios (username, password, email, rol, admin_owner, tipo_negocio) values (?,?,?,?,?,?)";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getPassword());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getRol());
            if (usuario.getAdminOwner() != null) {
                stmt.setString(5, usuario.getAdminOwner());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            if (usuario.getTipoNegocio() != null && !usuario.getTipoNegocio().isBlank()) {
                stmt.setString(6, usuario.getTipoNegocio());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            if (usuario.getId() != null && usuario.getId() > 0) {
                stmt.setLong(7, usuario.getId());
            }

            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "delete from usuarios where id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private Usuario getUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(rs.getString("password"));
        usuario.setEmail(rs.getString("email"));
        usuario.setRol(rs.getString("rol"));
        try {
            String adminOwner = rs.getString("admin_owner");
            if (!rs.wasNull()) {
                usuario.setAdminOwner(adminOwner);
            }
        } catch (SQLException ignored) {
            // columna admin_owner opcional si no se ejecuto migracion_tenant.sql
        }
        try {
            String tipoNegocio = rs.getString("tipo_negocio");
            if (!rs.wasNull()) {
                usuario.setTipoNegocio(tipoNegocio);
            }
        } catch (SQLException ignored) {
            // columna tipo_negocio opcional si no se ejecuto migracion
        }
        return usuario;
    }
}
