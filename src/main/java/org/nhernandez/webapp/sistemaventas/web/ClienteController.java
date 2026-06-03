package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.models.Cliente;
import org.nhernandez.webapp.sistemaventas.services.ClienteService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ClienteController {

    private final ClienteService service;
    private final LoginService auth;

    public ClienteController(ClienteService service, LoginService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping("/clientes")
    public String listar(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        model.addAttribute("clientes", service.listarActivos(tenant));
        model.addAttribute("username", auth.getUsername(req));
        model.addAttribute("esAdmin", RolUtil.esAdmin(req));
        return "clientes";
    }

    @GetMapping("/clientes/form")
    public String formularioGet(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede gestionar clientes.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long id = parseLong(req.getParameter("id"), 0L);
        if (id > 0) {
            service.porId(tenant, id).ifPresent(c -> model.addAttribute("cliente", c));
        }
        if (!model.containsAttribute("cliente")) {
            model.addAttribute("cliente", new Cliente());
        }
        return "formCliente";
    }

    @PostMapping("/clientes/form")
    public String formularioPost(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede gestionar clientes.");
            return null;
        }

        String tenant = TenantUtil.getTenantOwner(req);
        long id = parseLong(req.getParameter("id"), 0L);

        Cliente cliente = new Cliente();
        cliente.setNombre(req.getParameter("nombre"));
        cliente.setRfc(req.getParameter("rfc"));
        cliente.setRazonSocial(req.getParameter("razon_social"));
        cliente.setEmail(req.getParameter("email"));
        cliente.setCodigoPostal(req.getParameter("codigo_postal"));
        cliente.setUsoCfdi(req.getParameter("uso_cfdi"));

        Map<String, String> errores = new HashMap<>();

        if (id > 0) {
            Optional<Cliente> existente = service.porId(tenant, id);
            if (existente.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cliente no pertenece a tu cuenta.");
                return null;
            }
            cliente.setId(id);
        }

        if (errores.isEmpty()) {
            try {
                service.guardar(tenant, cliente);
                return "redirect:/clientes";
            } catch (ServiceJdbcException e) {
                errores.put("general", e.getMessage());
            }
        }

        model.addAttribute("errores", errores);
        model.addAttribute("cliente", cliente);
        return "formCliente";
    }

    @GetMapping("/clientes/eliminar")
    public void eliminar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo el administrador puede eliminar clientes.");
            return;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long id = parseLong(req.getParameter("id"), 0L);
        if (id > 0) {
            try {
                service.desactivar(tenant, id);
                resp.sendRedirect(req.getContextPath() + "/clientes");
            } catch (ServiceJdbcException e) {
                req.getSession().setAttribute("mensajeError", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/clientes");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Error el id es null, se debe enviar como parametro en la url!");
        }
    }

    private long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
