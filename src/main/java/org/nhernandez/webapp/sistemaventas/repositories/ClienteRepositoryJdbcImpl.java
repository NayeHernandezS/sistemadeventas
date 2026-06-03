package org.nhernandez.webapp.sistemaventas.repositories;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ClienteRepositoryJdbcImpl implements ClienteRepository {

    private final Connection conn;

    @Autowired
    public ClienteRepositoryJdbcImpl(@MysqlConn Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<Cliente> listarActivosPorTenant(String tenantOwner) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = """
                SELECT id, tenant_owner, nombre, rfc, razon_social, email, codigo_postal, uso_cfdi, activo, fecha_registro
                FROM clientes
                WHERE tenant_owner = ? AND activo = 1
                ORDER BY nombre ASC
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapear(rs));
                }
            }
        }
        return clientes;
    }

    @Override
    public Cliente porIdPorTenant(Long id, String tenantOwner) throws SQLException {
        String sql = """
                SELECT id, tenant_owner, nombre, rfc, razon_social, email, codigo_postal, uso_cfdi, activo, fecha_registro
                FROM clientes
                WHERE id = ? AND tenant_owner = ? AND activo = 1
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, tenantOwner);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void guardar(Cliente cliente) throws SQLException {
        if (cliente.getTenantOwner() == null || cliente.getTenantOwner().isBlank()) {
            throw new SQLException("tenant_owner es obligatorio al guardar un cliente");
        }
        String nombre = texto(cliente.getNombre());
        if (nombre.isBlank()) {
            throw new SQLException("El nombre del cliente es obligatorio");
        }

        String sql;
        if (cliente.getId() != null && cliente.getId() > 0) {
            sql = """
                    UPDATE clientes
                    SET nombre = ?, rfc = ?, razon_social = ?, email = ?, codigo_postal = ?, uso_cfdi = ?
                    WHERE id = ? AND tenant_owner = ? AND activo = 1
                    """;
        } else {
            sql = """
                    INSERT INTO clientes (tenant_owner, nombre, rfc, razon_social, email, codigo_postal, uso_cfdi)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (cliente.getId() != null && cliente.getId() > 0) {
                stmt.setString(1, nombre);
                setNullableString(stmt, 2, cliente.getRfc());
                setNullableString(stmt, 3, textoObligatorio(cliente.getRazonSocial()));
                setNullableString(stmt, 4, textoObligatorio(cliente.getEmail()));
                setNullableString(stmt, 5, textoObligatorio(cliente.getCodigoPostal()));
                setNullableString(stmt, 6, textoObligatorio(cliente.getUsoCfdi()));
                stmt.setLong(7, cliente.getId());
                stmt.setString(8, cliente.getTenantOwner());
            } else {
                stmt.setString(1, cliente.getTenantOwner());
                stmt.setString(2, nombre);
                setNullableString(stmt, 3, cliente.getRfc());
                setNullableString(stmt, 4, textoObligatorio(cliente.getRazonSocial()));
                setNullableString(stmt, 5, textoObligatorio(cliente.getEmail()));
                setNullableString(stmt, 6, textoObligatorio(cliente.getCodigoPostal()));
                setNullableString(stmt, 7, textoObligatorio(cliente.getUsoCfdi()));
            }
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo guardar el cliente o no pertenece a tu cuenta");
            }
        }
    }

    @Override
    public void desactivarPorTenant(Long id, String tenantOwner) throws SQLException {
        String sql = "UPDATE clientes SET activo = 0 WHERE id = ? AND tenant_owner = ? AND activo = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, tenantOwner);
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Cliente no encontrado o sin permiso");
            }
        }
    }

    private static Cliente mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("id"));
        cliente.setTenantOwner(rs.getString("tenant_owner"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setRfc(rs.getString("rfc"));
        cliente.setRazonSocial(rs.getString("razon_social"));
        cliente.setEmail(rs.getString("email"));
        cliente.setCodigoPostal(rs.getString("codigo_postal"));
        cliente.setUsoCfdi(rs.getString("uso_cfdi"));
        cliente.setActivo(rs.getInt("activo") == 1);
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            cliente.setFechaRegistro(ts.toLocalDateTime());
        }
        return cliente;
    }

    private static String texto(String value) {
        return value != null ? value.trim() : "";
    }

    private static String textoObligatorio(String value) {
        String limpio = texto(value);
        return limpio.isBlank() ? null : limpio;
    }

    private static void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            stmt.setNull(index, java.sql.Types.VARCHAR);
        } else {
            stmt.setString(index, value);
        }
    }
}
