package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.*;
import org.nhernandez.webapp.sistemaventas.repositories.FacturaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ProductoService;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/carro")
public class CarroController {

    private static final String SESSION_TICKETS_KEY = "ticketsVenta";

    private final ProductoService productoService;
    private final Carro carro;
    private final LoginService loginService;
    private final TicketRepository ticketRepository;
    private final FacturaRepository facturaRepository;

    public CarroController(@ProductoServicePrincipal ProductoService productoService,
                             Carro carro,
                             LoginService loginService,
                             TicketRepository ticketRepository,
                             FacturaRepository facturaRepository) {
        this.productoService = productoService;
        this.carro = carro;
        this.loginService = loginService;
        this.ticketRepository = ticketRepository;
        this.facturaRepository = facturaRepository;
    }

    @GetMapping("/ver")
    public String verCarro() {
        return "carro";
    }

    @GetMapping("/agregar")
    public String agregar(HttpServletRequest req) {
        Long id = Long.parseLong(req.getParameter("id"));
        String tenant = TenantUtil.getTenantOwner(req);
        Optional<Producto> producto = productoService.porIdPorOwner(id, tenant);
        if (producto.isPresent()) {
            carro.addItemCarro(new ItemCarro(1, producto.get()));
        }
        return "redirect:/carro/ver";
    }

    @PostMapping("/actualizar")
    public String actualizar(HttpServletRequest req) {
        updateProductos(req, carro);
        updateCantidades(req, carro);
        return "redirect:/carro/ver";
    }

    @PostMapping("/finalizar")
    public void finalizar(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión para generar tickets.");
            return;
        }

        if (carro.isEmpty()) {
            req.getSession().setAttribute("mensajeTicket", "No hay productos en el carrito para generar ticket.");
            resp.sendRedirect(req.getContextPath() + "/carro/ver");
            return;
        }

        boolean requiereFactura = req.getParameter("requiereFactura") != null;
        String rfc = limpiar(req.getParameter("rfcFactura"));
        String razonSocial = limpiar(req.getParameter("razonSocial"));
        String emailFactura = limpiar(req.getParameter("emailFactura"));
        String direccion = limpiar(req.getParameter("direccionFactura"));
        String usoCfdi = limpiar(req.getParameter("usoCfdi"));

        if (requiereFactura) {
            String error = validarDatosFactura(rfc, razonSocial);
            if (error != null) {
                req.getSession().setAttribute("mensajeTicket", error);
                resp.sendRedirect(req.getContextPath() + "/carro/ver");
                return;
            }
        }

        String tenant = TenantUtil.getTenantOwner(req);
        TicketVenta ticket = construirTicket(carro, usernameOpt.get(), tenant);
        ticketRepository.guardar(ticket);
        if (requiereFactura && ticket.getId() != null) {
            Factura factura = new Factura();
            factura.setTicketId(ticket.getId());
            factura.setFolioFactura(generarFolioFactura());
            factura.setRfc(rfc.toUpperCase());
            factura.setRazonSocial(razonSocial);
            factura.setEmail(emailFactura.isEmpty() ? null : emailFactura);
            factura.setDireccion(direccion.isEmpty() ? null : direccion);
            factura.setUsoCfdi(usoCfdi.isEmpty() ? null : usoCfdi);
            factura.setFechaEmision(LocalDateTime.now());
            facturaRepository.guardar(factura);
        }
        guardarTicketEnSesion(req.getSession(), ticket);
        carro.vaciar();
        String msg = "Venta finalizada. Ticket " + ticket.getFolio() + " generado.";
        if (requiereFactura) {
            msg += " Factura emitida; puede consultarla desde Tickets.";
        }
        req.getSession().setAttribute("mensajeTicket", msg);
        resp.sendRedirect(req.getContextPath() + "/tickets");
    }

    private void updateProductos(HttpServletRequest request, Carro carro) {
        String[] deleteIds = request.getParameterValues("deleteProductos");
        if (deleteIds != null && deleteIds.length > 0) {
            carro.removeProductos(Arrays.asList(deleteIds));
        }
    }

    private void updateCantidades(HttpServletRequest request, Carro carro) {
        Enumeration<String> enumer = request.getParameterNames();
        while (enumer.hasMoreElements()) {
            String paramName = enumer.nextElement();
            if (paramName.startsWith("cant_")) {
                String id = paramName.substring(5);
                String cantidad = request.getParameter(paramName);
                if (cantidad != null) {
                    carro.updateCantidad(id, Integer.parseInt(cantidad));
                }
            }
        }
    }

    private String limpiar(String s) {
        return s == null ? "" : s.trim();
    }

    private String validarDatosFactura(String rfc, String razonSocial) {
        if (razonSocial.isBlank()) {
            return "Para facturar indique la razón social o nombre del cliente.";
        }
        if (rfc.isBlank()) {
            return "Para facturar indique el RFC del cliente.";
        }
        String rfcNorm = rfc.toUpperCase().replaceAll("\\s", "");
        if (rfcNorm.length() < 12 || rfcNorm.length() > 13 || !rfcNorm.matches("[A-ZÑ&0-9]+")) {
            return "RFC inválido (debe tener 12 o 13 caracteres alfanuméricos).";
        }
        return null;
    }

    private String generarFolioFactura() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String sufijo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "FAC-" + timestamp + "-" + sufijo;
    }

    private TicketVenta construirTicket(Carro carro, String username, String tenantOwner) {
        TicketVenta ticket = new TicketVenta();
        ticket.setFolio(generarFolio());
        ticket.setFechaVenta(LocalDateTime.now());
        ticket.setUsernameVendedor(username);
        ticket.setTenantOwner(tenantOwner);
        ticket.setTotal(carro.getTotal());

        List<TicketItem> detalle = new ArrayList<>();
        for (ItemCarro itemCarro : carro.getItems()) {
            TicketItem ticketItem = new TicketItem();
            ticketItem.setProductoId(itemCarro.getProducto().getId());
            ticketItem.setNombreProducto(itemCarro.getProducto().getNombre());
            ticketItem.setPrecioUnitario(itemCarro.getProducto().getPrecio());
            ticketItem.setCantidad(itemCarro.getCantidad());
            ticketItem.setImporte(itemCarro.getImporte());
            detalle.add(ticketItem);
        }
        ticket.setItems(detalle);
        return ticket;
    }

    private String generarFolio() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String sufijo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TCK-" + timestamp + "-" + sufijo;
    }

    @SuppressWarnings("unchecked")
    private void guardarTicketEnSesion(HttpSession session, TicketVenta ticket) {
        List<TicketVenta> tickets = (List<TicketVenta>) session.getAttribute(SESSION_TICKETS_KEY);
        if (tickets == null) {
            tickets = new ArrayList<>();
        }
        tickets.add(0, ticket);
        session.setAttribute(SESSION_TICKETS_KEY, tickets);
    }
}
