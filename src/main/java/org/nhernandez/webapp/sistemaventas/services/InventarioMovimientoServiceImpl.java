package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.configs.MysqlConn;
import org.nhernandez.webapp.sistemaventas.models.MovimientoInventario;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TipoMovimientoInventario;
import org.nhernandez.webapp.sistemaventas.repositories.MovimientoInventarioRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class InventarioMovimientoServiceImpl implements InventarioMovimientoService {

    private static final int LIMITE_HISTORIAL_DEFAULT = 50;

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final Connection conn;

    @Autowired
    public InventarioMovimientoServiceImpl(ProductoRepository productoRepository,
                                           MovimientoInventarioRepository movimientoRepository,
                                           @MysqlConn Connection conn) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.conn = conn;
    }

    @Override
    public void aplicarMovimiento(String tenantOwner, String username, long productoId,
                                  TipoMovimientoInventario tipo, int cantidad, String motivo) {
        if (tenantOwner == null || tenantOwner.isBlank()) {
            throw new ServiceJdbcException("Cuenta de negocio no identificada.", null);
        }
        if (username == null || username.isBlank()) {
            throw new ServiceJdbcException("Usuario no identificado.", null);
        }
        if (tipo == null) {
            throw new ServiceJdbcException("Tipo de movimiento no valido.", null);
        }

        try {
            Producto producto = productoRepository.porIdPorOwner(productoId, tenantOwner);
            if (producto == null) {
                throw new ServiceJdbcException("Producto no encontrado en tu inventario.", null);
            }

            int antes = producto.getExistencias();
            int despues = calcularExistenciasDespues(tipo, antes, cantidad);
            int cantidadRegistro = cantidadRegistrada(tipo, antes, despues, cantidad);

            if (despues == antes) {
                throw new ServiceJdbcException("El movimiento no cambia las existencias.", null);
            }

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setTenantOwner(tenantOwner);
            movimiento.setProductoId(productoId);
            movimiento.setTipo(tipo);
            movimiento.setCantidad(cantidadRegistro);
            movimiento.setExistenciasAntes(antes);
            movimiento.setExistenciasDespues(despues);
            movimiento.setMotivo(motivo != null ? motivo.trim() : null);
            movimiento.setUsername(username);

            boolean autoCommitAnterior = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                movimientoRepository.insertar(movimiento);
                aplicarCambioStock(tipo, productoId, tenantOwner, cantidad, despues);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitAnterior);
            }
        } catch (SQLException e) {
            throw new ServiceJdbcException(mensajeAmigable(e), e);
        }
    }

    @Override
    public List<MovimientoInventario> listarRecientes(String tenantOwner, int limite) {
        int max = limite > 0 ? limite : LIMITE_HISTORIAL_DEFAULT;
        try {
            return movimientoRepository.listarRecientesPorTenant(tenantOwner, max);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private void aplicarCambioStock(TipoMovimientoInventario tipo, long productoId,
                                     String tenantOwner, int cantidad, int existenciasDespues) throws SQLException {
        switch (tipo) {
            case ENTRADA -> productoRepository.agregarExistencias(productoId, tenantOwner, cantidad);
            case SALIDA -> productoRepository.descontarExistencias(productoId, tenantOwner, cantidad);
            case AJUSTE -> productoRepository.actualizarExistencias(productoId, tenantOwner, existenciasDespues);
        }
    }

    private static int calcularExistenciasDespues(TipoMovimientoInventario tipo, int antes, int cantidad) {
        return switch (tipo) {
            case ENTRADA -> {
                if (cantidad <= 0) {
                    throw new ServiceJdbcException("Indica una cantidad mayor a cero para la entrada.", null);
                }
                yield antes + cantidad;
            }
            case SALIDA -> {
                if (cantidad <= 0) {
                    throw new ServiceJdbcException("Indica una cantidad mayor a cero para la salida.", null);
                }
                if (antes < cantidad) {
                    throw new ServiceJdbcException(
                            "Stock insuficiente: hay " + antes + " unidades y solicitaste retirar " + cantidad + ".",
                            null);
                }
                yield antes - cantidad;
            }
            case AJUSTE -> {
                if (cantidad < 0) {
                    throw new ServiceJdbcException("La cantidad final no puede ser negativa.", null);
                }
                yield cantidad;
            }
        };
    }

    private static int cantidadRegistrada(TipoMovimientoInventario tipo, int antes, int despues, int cantidad) {
        if (tipo == TipoMovimientoInventario.AJUSTE) {
            return Math.abs(despues - antes);
        }
        return cantidad;
    }

    private static String mensajeAmigable(SQLException e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("Stock insuficiente")) {
            return msg;
        }
        return msg != null ? msg : "Error al registrar el movimiento de inventario";
    }
}
