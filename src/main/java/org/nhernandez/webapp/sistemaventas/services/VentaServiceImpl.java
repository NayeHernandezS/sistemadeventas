package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.ItemCarro;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.FacturaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    private final ProductoRepository productoRepository;
    private final TicketRepository ticketRepository;
    private final FacturaRepository facturaRepository;
    private final CfdiTimbradoService cfdiTimbradoService;

    public VentaServiceImpl(ProductoRepository productoRepository,
                              TicketRepository ticketRepository,
                              FacturaRepository facturaRepository,
                              CfdiTimbradoService cfdiTimbradoService) {
        this.productoRepository = productoRepository;
        this.ticketRepository = ticketRepository;
        this.facturaRepository = facturaRepository;
        this.cfdiTimbradoService = cfdiTimbradoService;
    }

    @Override
    public void validarStock(String tenantOwner, Long productoId, int cantidadRequerida) {
        if (cantidadRequerida <= 0) {
            throw new ServiceJdbcException("La cantidad debe ser al menos 1", null);
        }
        try {
            Producto producto = productoRepository.porIdPorOwner(productoId, tenantOwner);
            if (producto == null) {
                throw new ServiceJdbcException("Producto no encontrado", null);
            }
            if (producto.esServicio()) {
                return;
            }
            if (producto.getExistencias() < cantidadRequerida) {
                throw new ServiceJdbcException(
                        "Stock insuficiente para \"" + producto.getNombre()
                                + "\": disponibles " + producto.getExistencias()
                                + ", solicitadas " + cantidadRequerida,
                        null);
            }
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void validarStockCarrito(String tenantOwner, List<ItemCarro> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (ItemCarro item : items) {
            validarStock(tenantOwner, item.getProducto().getId(), item.getCantidad());
        }
    }

    @Override
    public TicketVenta registrarVenta(TicketVenta ticket, Factura facturaOpcional) {
        validarStockCarrito(ticket.getTenantOwner(), itemsDesdeTicket(ticket));
        try {
            ticketRepository.guardar(ticket);
            if (facturaOpcional != null && ticket.getId() != null) {
                facturaOpcional.setTicketId(ticket.getId());
                if (facturaOpcional.getCfdiEstado() == null || facturaOpcional.getCfdiEstado().isBlank()) {
                    facturaOpcional.setCfdiEstado(
                            cfdiTimbradoService.disponible(ticket.getTenantOwner()) ? "PENDIENTE" : "INFORMATIVO");
                }
                facturaRepository.guardar(facturaOpcional);
                cfdiTimbradoService.intentarTimbrar(ticket.getTenantOwner(), ticket, facturaOpcional);
            }
            return ticket;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private static List<ItemCarro> itemsDesdeTicket(TicketVenta ticket) {
        return ticket.getItems().stream()
                .map(item -> {
                    Producto p = new Producto();
                    p.setId(item.getProductoId());
                    p.setNombre(item.getNombreProducto());
                    p.setPrecio(item.getPrecioUnitario());
                    return new ItemCarro(item.getCantidad(), p);
                })
                .toList();
    }
}
