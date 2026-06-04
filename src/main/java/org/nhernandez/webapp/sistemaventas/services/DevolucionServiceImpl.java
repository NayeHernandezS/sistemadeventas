package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Devolucion;
import org.nhernandez.webapp.sistemaventas.models.DevolucionItem;
import org.nhernandez.webapp.sistemaventas.models.LineaDevolucionVista;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.repositories.DevolucionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DevolucionServiceImpl implements DevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final TicketRepository ticketRepository;
    private final ProductoRepository productoRepository;

    public DevolucionServiceImpl(DevolucionRepository devolucionRepository,
                                 TicketRepository ticketRepository,
                                 ProductoRepository productoRepository) {
        this.devolucionRepository = devolucionRepository;
        this.ticketRepository = ticketRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public List<Devolucion> listarPorTenant(String tenantOwner) {
        try {
            return devolucionRepository.listarPorTenant(tenantOwner);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public TicketVenta obtenerTicketParaDevolucion(Long ticketId, String tenantOwner) {
        try {
            TicketVenta ticket = ticketRepository.porIdDeTenant(ticketId, tenantOwner);
            if (ticket == null) {
                throw new ServiceJdbcException("Ticket no encontrado", null);
            }
            if (!ticket.permiteDevolucion()) {
                throw new ServiceJdbcException("Este ticket ya fue devuelto por completo", null);
            }
            return ticket;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public List<LineaDevolucionVista> lineasDisponibles(TicketVenta ticket) {
        List<LineaDevolucionVista> lineas = new ArrayList<>();
        if (ticket.getItems() == null) {
            return lineas;
        }
        try {
            for (TicketItem item : ticket.getItems()) {
                int yaDevuelta = devolucionRepository.cantidadDevueltaDeProducto(
                        ticket.getId(), item.getProductoId());
                int disponible = item.getCantidad() - yaDevuelta;
                if (disponible <= 0) {
                    continue;
                }
                LineaDevolucionVista linea = new LineaDevolucionVista();
                linea.setProductoId(item.getProductoId());
                linea.setNombreProducto(item.getNombreProducto());
                linea.setPrecioUnitario(item.getPrecioUnitario());
                linea.setCantidadVendida(item.getCantidad());
                linea.setCantidadYaDevuelta(yaDevuelta);
                linea.setCantidadDisponible(disponible);
                lineas.add(linea);
            }
            return lineas;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public Devolucion registrarDevolucion(Long ticketId, String tenantOwner, String username,
                                          Map<Long, Integer> cantidadesPorProducto, String motivo) {
        TicketVenta ticket = obtenerTicketParaDevolucion(ticketId, tenantOwner);
        List<LineaDevolucionVista> disponibles = lineasDisponibles(ticket);
        if (disponibles.isEmpty()) {
            throw new ServiceJdbcException("No hay productos pendientes de devolver en este ticket", null);
        }

        List<DevolucionItem> itemsDevolucion = new ArrayList<>();
        int totalDevuelto = 0;

        for (LineaDevolucionVista linea : disponibles) {
            Integer cant = cantidadesPorProducto.get(linea.getProductoId());
            if (cant == null || cant <= 0) {
                continue;
            }
            if (cant > linea.getCantidadDisponible()) {
                throw new ServiceJdbcException(
                        "Cantidad invalida para " + linea.getNombreProducto()
                                + " (max " + linea.getCantidadDisponible() + ")", null);
            }
            DevolucionItem item = new DevolucionItem();
            item.setProductoId(linea.getProductoId());
            item.setNombreProducto(linea.getNombreProducto());
            item.setCantidad(cant);
            item.setPrecioUnitario(linea.getPrecioUnitario());
            item.setImporte(cant * linea.getPrecioUnitario());
            itemsDevolucion.add(item);
            totalDevuelto += item.getImporte();
        }

        if (itemsDevolucion.isEmpty()) {
            throw new ServiceJdbcException("Indica al menos un producto y cantidad a devolver", null);
        }

        Devolucion devolucion = new Devolucion();
        devolucion.setFolio(generarFolioDevolucion());
        devolucion.setTicketId(ticket.getId());
        devolucion.setTicketFolio(ticket.getFolio());
        devolucion.setTenantOwner(tenantOwner);
        devolucion.setUsernameRegistro(username);
        devolucion.setFechaDevolucion(LocalDateTime.now());
        devolucion.setMotivo(motivo != null && !motivo.isBlank() ? motivo.trim() : null);
        devolucion.setTotalDevuelto(totalDevuelto);
        devolucion.setItems(itemsDevolucion);

        try {
            devolucionRepository.guardar(devolucion);
            for (DevolucionItem item : itemsDevolucion) {
                Producto producto = productoRepository.porIdPorOwner(item.getProductoId(), tenantOwner);
                if (producto != null && producto.esServicio()) {
                    continue;
                }
                productoRepository.agregarExistencias(item.getProductoId(), tenantOwner, item.getCantidad());
            }
            actualizarEstadoTicket(ticket, tenantOwner);
            return devolucion;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private void actualizarEstadoTicket(TicketVenta ticket, String tenantOwner) throws SQLException {
        boolean quedaPendiente = false;
        for (TicketItem item : ticket.getItems()) {
            int ya = devolucionRepository.cantidadDevueltaDeProducto(ticket.getId(), item.getProductoId());
            if (ya < item.getCantidad()) {
                quedaPendiente = true;
                break;
            }
        }
        String nuevoEstado = quedaPendiente ? "DEVUELTO_PARCIAL" : "DEVUELTO_TOTAL";
        ticketRepository.actualizarEstado(ticket.getId(), tenantOwner, nuevoEstado);
    }

    private String generarFolioDevolucion() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String sufijo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "DEV-" + timestamp + "-" + sufijo;
    }
}
