package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Factura;
import org.nhernandez.webapp.sistemaventas.models.ItemCarro;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.FacturaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    private final TicketRepository ticketRepository;
    private final FacturaRepository facturaRepository;
    private final CfdiTimbradoService cfdiTimbradoService;
    private final InventarioRecetaService inventarioRecetaService;

    public VentaServiceImpl(TicketRepository ticketRepository,
                              FacturaRepository facturaRepository,
                              CfdiTimbradoService cfdiTimbradoService,
                              InventarioRecetaService inventarioRecetaService) {
        this.ticketRepository = ticketRepository;
        this.facturaRepository = facturaRepository;
        this.cfdiTimbradoService = cfdiTimbradoService;
        this.inventarioRecetaService = inventarioRecetaService;
    }

    @Override
    public void validarStock(String tenantOwner, Long productoId, int cantidadRequerida) {
        if (cantidadRequerida <= 0) {
            throw new ServiceJdbcException("La cantidad debe ser al menos 1", null);
        }
        Producto producto = new Producto();
        producto.setId(productoId);
        inventarioRecetaService.validarStockCarrito(tenantOwner, List.of(new ItemCarro(cantidadRequerida, producto)));
    }

    @Override
    public void validarStockCarrito(String tenantOwner, List<ItemCarro> items) {
        inventarioRecetaService.validarStockCarrito(tenantOwner, items);
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
