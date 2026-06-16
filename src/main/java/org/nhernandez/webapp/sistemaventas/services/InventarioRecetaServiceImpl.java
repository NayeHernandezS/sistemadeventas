package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ItemCarro;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.Receta;
import org.nhernandez.webapp.sistemaventas.models.RecetaLinea;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.RecetaRepository;
import org.nhernandez.webapp.sistemaventas.util.UnidadMedidaUtil;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InventarioRecetaServiceImpl implements InventarioRecetaService {

    private final ProductoRepository productoRepository;
    private final RecetaRepository recetaRepository;

    public InventarioRecetaServiceImpl(ProductoRepository productoRepository,
                                       RecetaRepository recetaRepository) {
        this.productoRepository = productoRepository;
        this.recetaRepository = recetaRepository;
    }

    @Override
    public void validarStockCarrito(String tenantOwner, List<ItemCarro> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<Long, Integer> requeridoPorInsumo = new HashMap<>();
        for (ItemCarro item : items) {
            if (item == null || item.getProducto() == null) {
                continue;
            }
            Producto producto = cargarProducto(tenantOwner, item.getProducto().getId());
            if (producto.esServicio()) {
                continue;
            }
            Optional<Receta> receta = recetaDeProducto(tenantOwner, producto.getId());
            if (receta.isPresent()) {
                acumularInsumos(receta.get(), item.getCantidad(), requeridoPorInsumo);
            } else {
                validarStockDirecto(producto, item.getCantidad());
            }
        }
        validarInsumosAcumulados(tenantOwner, requeridoPorInsumo);
    }

    @Override
    public void descontarPorTicket(TicketVenta ticket) throws SQLException {
        if (ticket.getItems() == null || ticket.getItems().isEmpty()) {
            return;
        }
        String tenantOwner = ticket.getTenantOwner();
        if (tenantOwner == null || tenantOwner.isBlank()) {
            throw new SQLException("tenant_owner es obligatorio para descontar inventario");
        }
        Map<Long, Integer> requeridoPorInsumo = new HashMap<>();
        for (TicketItem item : ticket.getItems()) {
            Producto producto = productoRepository.porIdPorOwner(item.getProductoId(), tenantOwner);
            if (producto == null) {
                throw new SQLException("Producto no encontrado id=" + item.getProductoId());
            }
            if (producto.esServicio()) {
                continue;
            }
            Optional<Receta> receta = recetaDeProducto(tenantOwner, producto.getId());
            if (receta.isPresent()) {
                acumularInsumos(receta.get(), item.getCantidad(), requeridoPorInsumo);
            } else {
                productoRepository.descontarExistencias(item.getProductoId(), tenantOwner, item.getCantidad());
            }
        }
        for (Map.Entry<Long, Integer> entry : requeridoPorInsumo.entrySet()) {
            productoRepository.descontarExistencias(entry.getKey(), tenantOwner, entry.getValue());
        }
    }

    private void acumularInsumos(Receta receta, int cantidadPlatillo, Map<Long, Integer> acumulado) {
        try {
            List<RecetaLinea> lineas = recetaRepository.listarLineasPorReceta(receta.getId());
            for (RecetaLinea linea : lineas) {
                int porPorcion = UnidadMedidaUtil.aUnidadBase(linea.getCantidad(), linea.getUnidad());
                if (porPorcion <= 0) {
                    continue;
                }
                long insumoId = linea.getInsumoProductoId();
                int total = porPorcion * cantidadPlatillo;
                acumulado.merge(insumoId, total, Integer::sum);
            }
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private void validarInsumosAcumulados(String tenantOwner, Map<Long, Integer> requeridoPorInsumo) {
        for (Map.Entry<Long, Integer> entry : requeridoPorInsumo.entrySet()) {
            Producto insumo = cargarProducto(tenantOwner, entry.getKey());
            int requerido = entry.getValue();
            if (insumo.getExistencias() < requerido) {
                throw new ServiceJdbcException(
                        "Stock insuficiente de insumo \"" + insumo.getNombre()
                                + "\": disponible " + UnidadMedidaUtil.formatear(insumo.getExistencias(), insumo.getUnidadMedida())
                                + ", requerido " + UnidadMedidaUtil.formatear(requerido, insumo.getUnidadMedida()),
                        null);
            }
        }
    }

    private void validarStockDirecto(Producto producto, int cantidadRequerida) {
        if (cantidadRequerida <= 0) {
            throw new ServiceJdbcException("La cantidad debe ser al menos 1", null);
        }
        if (producto.getExistencias() < cantidadRequerida) {
            String disponible = UnidadMedidaUtil.formatear(producto.getExistencias(), producto.getUnidadMedida());
            throw new ServiceJdbcException(
                    "Stock insuficiente para \"" + producto.getNombre()
                            + "\": disponibles " + disponible
                            + ", solicitadas " + cantidadRequerida,
                    null);
        }
    }

    private Producto cargarProducto(String tenantOwner, Long productoId) {
        try {
            Producto producto = productoRepository.porIdPorOwner(productoId, tenantOwner);
            if (producto == null) {
                throw new ServiceJdbcException("Producto no encontrado", null);
            }
            return producto;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private Optional<Receta> recetaDeProducto(String tenantOwner, Long productoId) {
        try {
            return recetaRepository.porProductoId(tenantOwner, productoId);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }
}
