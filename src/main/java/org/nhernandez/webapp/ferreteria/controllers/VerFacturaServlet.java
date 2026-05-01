package org.nhernandez.webapp.ferreteria.controllers;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.ferreteria.models.Factura;
import org.nhernandez.webapp.ferreteria.models.TicketVenta;
import org.nhernandez.webapp.ferreteria.repositories.FacturaRepository;
import org.nhernandez.webapp.ferreteria.repositories.TicketRepository;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/factura")
public class VerFacturaServlet extends HttpServlet {

    @Inject
    private TicketRepository ticketRepository;

    @Inject
    private FacturaRepository facturaRepository;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String folioTicket = req.getParameter("folioTicket");
        if (folioTicket == null || folioTicket.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Indique el folio del ticket.");
            return;
        }

        try {
            TicketVenta ticket = ticketRepository.porFolio(folioTicket.trim());
            if (ticket == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Ticket no encontrado.");
                return;
            }
            Factura factura = facturaRepository.porTicketId(ticket.getId());
            req.setAttribute("ticket", ticket);
            req.setAttribute("factura", factura);
            getServletContext().getRequestDispatcher("/factura.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al consultar la factura.", e);
        }
    }
}
