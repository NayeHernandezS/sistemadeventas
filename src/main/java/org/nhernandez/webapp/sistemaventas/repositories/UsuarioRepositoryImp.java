package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;

import org.nhernandez.webapp.sistemaventas.models.ClienteCuenta;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;

import java.sql.*;
import java.time.LocalDateTime;
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
    public Usuario porEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM usuarios WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getUsuario(rs);
                }
            }
        }
        return null;
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
        if (usuario.getId() != null && usuario.getId() > 0) {
            String sql = "update usuarios set username=?, password=?, email=?, rol=?, admin_owner=?, tipo_negocio=? where id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                bindUsuarioComun(stmt, usuario);
                stmt.setLong(7, usuario.getId());
                stmt.executeUpdate();
            }
            return;
        }
        try {
            insertarConAceptacionLegal(usuario);
        } catch (SQLException e) {
            if (columnasLegalesAusentes(e)) {
                insertarSinAceptacionLegal(usuario);
                return;
            }
            throw e;
        }
    }

    private void insertarConAceptacionLegal(Usuario usuario) throws SQLException {
        String sql = "insert into usuarios (username, password, email, rol, admin_owner, tipo_negocio, "
                + "aceptacion_legal_en, aceptacion_legal_version) values (?,?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindUsuarioComun(stmt, usuario);
            if (usuario.getAceptacionLegalEn() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(usuario.getAceptacionLegalEn()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }
            stmt.setString(8, usuario.getAceptacionLegalVersion());
            stmt.executeUpdate();
        }
    }

    private void insertarSinAceptacionLegal(Usuario usuario) throws SQLException {
        String sql = "insert into usuarios (username, password, email, rol, admin_owner, tipo_negocio) values (?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindUsuarioComun(stmt, usuario);
            stmt.executeUpdate();
        }
    }

    private void bindUsuarioComun(PreparedStatement stmt, Usuario usuario) throws SQLException {
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
    }

    private static boolean columnasLegalesAusentes(SQLException e) {
        String msg = e.getMessage();
        return msg != null && (msg.contains("aceptacion_legal_en") || msg.contains("aceptacion_legal_version"));
    }

    @Override
    public List<ClienteCuenta> listarCuentasCliente() throws SQLException {
        String sql = """
                SELECT u.id, u.username, u.email, u.tipo_negocio,
                       s.fecha_fin, s.en_periodo_prueba, s.estado AS estado_suscripcion,
                       s.plan_codigo,
                       (SELECT COUNT(*) FROM usuarios v
                        WHERE v.admin_owner = u.username
                          AND UPPER(v.rol) = 'VENDEDOR') AS cantidad_vendedores
                FROM usuarios u
                LEFT JOIN suscripciones s ON s.username = u.username
                WHERE UPPER(u.rol) = ?
                ORDER BY u.username
                """;
        List<ClienteCuenta> lista = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, RolUtil.ROL_ADMIN);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClienteCuenta c = new ClienteCuenta();
                    c.setId(rs.getLong("id"));
                    c.setUsername(rs.getString("username"));
                    c.setEmail(rs.getString("email"));
                    try {
                        c.setTipoNegocio(rs.getString("tipo_negocio"));
                    } catch (SQLException ignored) {
                    }
                    c.setCantidadVendedores(rs.getInt("cantidad_vendedores"));
                    Timestamp fin = rs.getTimestamp("fecha_fin");
                    String estadoSub = null;
                    try {
                        c.setEnPeriodoPrueba(rs.getBoolean("en_periodo_prueba"));
                        estadoSub = rs.getString("estado_suscripcion");
                        c.setEstadoSuscripcion(estadoSub);
                        c.setPlanCodigo(rs.getString("plan_codigo"));
                    } catch (SQLException ignored) {
                    }
                    boolean suspendida = estadoSub != null && "SUSPENDIDA".equalsIgnoreCase(estadoSub.trim());
                    if (fin != null) {
                        LocalDateTime fechaFin = fin.toLocalDateTime();
                        c.setFechaFinSuscripcion(fechaFin);
                        c.setVigente(!suspendida && !LocalDateTime.now().isAfter(fechaFin));
                    } else {
                        c.setVigente(false);
                    }
                    if (c.getPlanCodigo() == null || c.getPlanCodigo().isBlank()) {
                        c.setPlanCodigo("EMPRENDEDOR");
                    }
                    lista.add(c);
                }
            }
        }
        return lista;
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
        try {
            Timestamp legalEn = rs.getTimestamp("aceptacion_legal_en");
            if (legalEn != null) {
                usuario.setAceptacionLegalEn(legalEn.toLocalDateTime());
            }
            usuario.setAceptacionLegalVersion(rs.getString("aceptacion_legal_version"));
        } catch (SQLException ignored) {
            // columnas legales opcionales
        }
        return usuario;
    }
}
