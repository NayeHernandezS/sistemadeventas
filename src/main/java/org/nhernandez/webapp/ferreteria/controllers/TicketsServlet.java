package org.nhernandez.webapp.ferreteria.controllers;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.ferreteria.models.TicketVenta;
import org.nhernandez.webapp.ferreteria.repositories.TicketRepository;
import org.nhernandez.webapp.ferreteria.services.LoginService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet("/tickets")
public class TicketsServlet extends HttpServlet {

    @Inject
    private TicketRepository ticketRepository;

    @Inject
    private LoginService loginService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> usernameOpt = loginService.getUsername(req);
        if (usernameOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión para consultar tickets.");
            return;
        }

        try {
            List<TicketVenta> tickets;
            if (esAdmin(req)) {
                tickets = ticketRepository.listar();
            } else {
                tickets = ticketRepository.listarPorVendedor(usernameOpt.get());
            }
            req.setAttribute("tickets", tickets);
            getServletContext().getRequestDispatcher("/tickets.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al consultar tickets en base de datos.", e);
        }
    }

    private boolean esAdmin(HttpServletRequest req) {
        Object rol = req.getSession().getAttribute("rol");
        return rol != null && "ADMIN".equalsIgnoreCase(rol.toString());
    }
}
