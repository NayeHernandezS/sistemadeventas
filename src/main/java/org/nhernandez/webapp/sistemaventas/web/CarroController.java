package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.*;
import org.nhernandez.webapp.sistemaventas.services.CfdiTimbradoService;
import org.nhernandez.webapp.sistemaventas.services.ClienteService;
import org.nhernandez.webapp.sistemaventas.services.DatosFiscalesNegocioService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ProductoService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.services.VentaService;
import org.nhernandez.webapp.sistemaventas.util.FacturaDatosUtil;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
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

    private final ProductoService productoService;
    private final Carro carro;
    private final LoginService loginService;
    private final VentaService ventaService;
    private final DatosFiscalesNegocioService datosFiscalesNegocioService;
    private final CfdiTimbradoService cfdiTimbradoService;
    private final ClienteService clienteService;

    public CarroController(@ProductoServicePrincipal ProductoService productoService,
                             Carro carro,
                             LoginService loginService,
                             VentaService ventaService,
                             DatosFiscalesNegocioService datosFiscalesNegocioService,
                             CfdiTimbradoService cfdiTimbradoService,
                             ClienteService clienteService) {
        this.productoService = productoService;
        this.carro = carro;
        this.loginService = loginService;
        this.ventaService = ventaService;
        this.datosFiscalesNegocioService = datosFiscalesNegocioService;
        this.cfdiTimbradoService = cfdiTimbradoService;
        this.clienteService = clienteService;
    }

    @GetMapping("/ver")
    public String verCarro(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        if (tenant != null) {
            model.addAttribute("clientes", clienteService.listarActivos(tenant));
            cargarPrefillFactura(req, model, tenant);
        }
        model.addAttribute("esAdmin", RolUtil.esAdmin(req));
        model.addAttribute("cfdiTimbradoDisponible", cfdiTimbradoService.disponible());
        return "carro";
    }

    @GetMapping("/agregar")
    public String agregar(HttpServletRequest req) {
        Long id = Long.parseLong(req.getParameter("id"));
        String tenant = TenantUtil.getTenantOwner(req);
        Optional<Producto> producto = productoService.porIdPorOwner(id, tenant);
        if (producto.isEmpty()) {
            req.getSession().setAttribute("mensajeError", "Producto no encontrado.");
            return "redirect:/productos";
        }
        int cantidadNueva = carro.getCantidadEnCarro(id) + 1;
        try {
            ventaService.validarStock(tenant, id, cantidadNueva);
        } catch (ServiceJdbcException e) {
            req.getSession().setAttribute("mensajeError", e.getMessage());
            return "redirect:/productos";
        }
        carro.addItemCarro(new ItemCarro(1, producto.get()));
        return "redirect:/carro/ver";
    }

    @PostMapping("/actualizar")
    public String actualizar(HttpServletRequest req) {
        updateProductos(req, carro);
        updateCantidades(req, carro);
        String tenant = TenantUtil.getTenantOwner(req);
        try {
            ventaService.validarStockCarrito(tenant, carro.getItems());
        } catch (ServiceJdbcException e) {
            req.getSession().setAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/carro/ver";
    }

    @PostMapping("/finalizar")
    public void finalizar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        long clienteId = parseLongParam(req.getParameter("clienteId"), 0L);
        String rfc = limpiar(req.getParameter("rfcFactura"));
        String razonSocial = limpiar(req.getParameter("razonSocial"));
        String emailFactura = limpiar(req.getParameter("emailFactura"));
        String direccion = limpiar(req.getParameter("direccionFactura"));
        String usoCfdi = limpiar(req.getParameter("usoCfdi"));
        String codigoPostalReceptor = limpiar(req.getParameter("codigoPostalReceptor"));

        if (requiereFactura) {
            String error = validarDatosFactura(rfc, razonSocial, codigoPostalReceptor);
            if (error != null) {
                req.getSession().setAttribute("mensajeError", error);
                resp.sendRedirect(req.getContextPath() + "/carro/ver");
                return;
            }
        }

        String tenant = TenantUtil.getTenantOwner(req);
        if (clienteId > 0 && clienteService.porId(tenant, clienteId).isEmpty()) {
            req.getSession().setAttribute("mensajeError", "El cliente seleccionado no es valido.");
            resp.sendRedirect(req.getContextPath() + "/carro/ver");
            return;
        }

        TicketVenta ticket = construirTicket(carro, usernameOpt.get(), tenant);
        Factura factura = null;
        if (requiereFactura) {
            factura = new Factura();
            if (clienteId > 0) {
                factura.setClienteId(clienteId);
            }
            factura.setFolioFactura(generarFolioFactura());
            factura.setRfc(rfc.toUpperCase());
            factura.setRazonSocial(razonSocial);
            factura.setEmail(emailFactura.isEmpty() ? null : emailFactura);
            factura.setDireccion(direccion.isEmpty() ? null : direccion);
            factura.setUsoCfdi(usoCfdi.isEmpty() ? null : usoCfdi);
            factura.setCodigoPostalReceptor(codigoPostalReceptor.isEmpty() ? null : codigoPostalReceptor);
            factura.setFechaEmision(LocalDateTime.now());
        }

        try {
            ventaService.registrarVenta(ticket, factura);
        } catch (ServiceJdbcException e) {
            req.getSession().setAttribute("mensajeError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/carro/ver");
            return;
        }

        carro.vaciar();
        String msg = "Venta finalizada. Ticket " + ticket.getFolio() + " generado.";
        if (requiereFactura && factura != null) {
            if (factura.estaTimbrada()) {
                msg += " CFDI timbrado (UUID " + factura.getCfdiUuid() + ").";
            } else if ("ERROR".equalsIgnoreCase(factura.getCfdiEstado())) {
                msg += " Factura registrada; el timbrado CFDI fallo: " + factura.getCfdiMensaje();
            } else if (cfdiTimbradoService.disponible()) {
                msg += " Factura registrada; revisa el estado del CFDI en Tickets.";
            } else {
                msg += " Comprobante informativo emitido; consultalo en Tickets.";
            }
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
                    int cant = Integer.parseInt(cantidad);
                    if (cant <= 0) {
                        carro.removeProducto(id);
                    } else {
                        carro.updateCantidad(id, cant);
                    }
                }
            }
        }
    }

    private String limpiar(String s) {
        return s == null ? "" : s.trim();
    }

    private String validarDatosFactura(String rfc, String razonSocial, String codigoPostalReceptor) {
        if (razonSocial.isBlank()) {
            return "Para facturar indique la razón social o nombre del cliente.";
        }
        String errorRfc = FacturaDatosUtil.validarRfcObligatorio(rfc);
        if (errorRfc != null) {
            return errorRfc.replace("Indica el RFC.", "Para facturar indique el RFC del cliente.");
        }
        if (cfdiTimbradoService.disponible()) {
            if (codigoPostalReceptor.isBlank() || !codigoPostalReceptor.matches("\\d{5}")) {
                return "Para timbrar CFDI indique el codigo postal del receptor (5 digitos).";
            }
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

    private void cargarPrefillFactura(HttpServletRequest req, Model model, String tenant) {
        long clienteId = parseLongParam(req.getParameter("clienteId"), 0L);
        if (clienteId > 0) {
            clienteService.porId(tenant, clienteId).ifPresent(cliente -> {
                model.addAttribute("facturaDefaults", prefillDesdeCliente(cliente));
                model.addAttribute("clienteIdSeleccionado", clienteId);
                model.addAttribute("prefillOrigen", "cliente");
            });
            return;
        }
        datosFiscalesNegocioService.consultar(tenant).ifPresent(d -> {
            model.addAttribute("facturaDefaults", d);
            model.addAttribute("prefillOrigen", "perfil");
        });
    }

    private static DatosFiscalesNegocio prefillDesdeCliente(Cliente cliente) {
        DatosFiscalesNegocio prefill = new DatosFiscalesNegocio();
        prefill.setRfc(cliente.getRfc());
        prefill.setRazonSocial(cliente.nombreFiscal());
        prefill.setEmail(cliente.getEmail());
        prefill.setUsoCfdi(cliente.getUsoCfdi());
        prefill.setCodigoPostal(cliente.getCodigoPostal());
        return prefill;
    }

    private static long parseLongParam(String value, long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
