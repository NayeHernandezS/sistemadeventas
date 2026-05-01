package org.nhernandez.webapp.ferreteria.controllers;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.nhernandez.webapp.ferreteria.models.Carro;
import org.nhernandez.webapp.ferreteria.models.ItemCarro;
import org.nhernandez.webapp.ferreteria.models.Factura;
import org.nhernandez.webapp.ferreteria.models.TicketItem;
import org.nhernandez.webapp.ferreteria.models.TicketVenta;
import org.nhernandez.webapp.ferreteria.repositories.FacturaRepository;
import org.nhernandez.webapp.ferreteria.repositories.TicketRepository;
import org.nhernandez.webapp.ferreteria.services.LoginService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebServlet("/carro/finalizar")
public class FinalizarVentaServlet extends HttpServlet {

    private static final String SESSION_TICKETS_KEY = "ticketsVenta";

    @Inject
    private Carro carro;

    @Inject
    private LoginService loginService;

    @Inject
    private TicketRepository ticketRepository;

    @Inject
    private FacturaRepository facturaRepository;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        TicketVenta ticket = construirTicket(carro, usernameOpt.get());
        try {
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
        } catch (SQLException e) {
            throw new ServletException("Error al guardar el ticket en base de datos.", e);
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

    private TicketVenta construirTicket(Carro carro, String username) {
        TicketVenta ticket = new TicketVenta();
        ticket.setFolio(generarFolio());
        ticket.setFechaVenta(LocalDateTime.now());
        ticket.setUsernameVendedor(username);
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
