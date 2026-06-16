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
import org.nhernandez.webapp.sistemaventas.util.SkuUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
            model.addAttribute("clientes", listarClientesSeguro(tenant));
            cargarPrefillFacturaSeguro(req, model, tenant);
        }
        model.addAttribute("esAdmin", RolUtil.esAdmin(req));
        model.addAttribute("cfdiTimbradoDisponible", cfdiTimbradoDisponibleSeguro(tenant));
        return "carro";
    }

    private List<Cliente> listarClientesSeguro(String tenant) {
        try {
            return clienteService.listarActivos(tenant);
        } catch (ServiceJdbcException e) {
            return List.of();
        }
    }

    private void cargarPrefillFacturaSeguro(HttpServletRequest req, Model model, String tenant) {
        try {
            cargarPrefillFactura(req, model, tenant);
        } catch (ServiceJdbcException ignored) {
            // Prefill opcional: el carro debe mostrarse aunque falle la consulta fiscal
        }
    }

    private boolean cfdiTimbradoDisponibleSeguro(String tenant) {
        if (tenant == null || tenant.isBlank()) {
            return false;
        }
        try {
            return cfdiTimbradoService.disponible(tenant);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @GetMapping("/agregar")
    public String agregar(HttpServletRequest req) {
        try {
            long id = parseLongParam(req.getParameter("id"), 0L);
            if (id <= 0) {
                req.getSession().setAttribute("mensajeError", "Producto no valido.");
                return redirigirTrasEscaneo(req.getParameter("origen"));
            }
            String tenant = TenantUtil.getTenantOwner(req);
            Optional<Producto> producto = productoService.porIdPorOwner(id, tenant);
            if (producto.isEmpty()) {
                req.getSession().setAttribute("mensajeError", "Producto no encontrado.");
                return redirigirTrasEscaneo(req.getParameter("origen"));
            }
            ResultadoEscaneoCarro resultado = agregarProductoAlCarro(req, producto.get());
            if (resultado.ok()) {
                req.getSession().setAttribute("mensajeExito", resultado.mensaje());
            } else {
                req.getSession().setAttribute("mensajeError", resultado.mensaje());
            }
            return redirigirTrasEscaneo(req.getParameter("origen"));
        } catch (ServiceJdbcException e) {
            req.getSession().setAttribute("mensajeError", e.getMessage());
            return redirigirTrasEscaneo(req.getParameter("origen"));
        }
    }

    @GetMapping("/api/agregar")
    @ResponseBody
    public EscaneoCarroRespuesta agregarApi(HttpServletRequest req,
                                            @RequestParam long id,
                                            @RequestParam(required = false) String origen) {
        try {
            if (id <= 0) {
                return respuestaCarro(false, "Producto no valido.", null);
            }
            String tenant = TenantUtil.getTenantOwner(req);
            Optional<Producto> producto = productoService.porIdPorOwner(id, tenant);
            if (producto.isEmpty()) {
                return respuestaCarro(false, "Producto no encontrado.", null);
            }
            ResultadoEscaneoCarro resultado = agregarProductoAlCarro(req, producto.get());
            return respuestaCarro(resultado.ok(), resultado.mensaje(), producto.get());
        } catch (ServiceJdbcException e) {
            return respuestaCarro(false, e.getMessage(), null);
        }
    }

    @GetMapping("/agregar-por-sku")
    public String agregarPorSku(HttpServletRequest req,
                                @RequestParam(required = false) String sku,
                                @RequestParam(required = false) String origen) {
        ResultadoEscaneoCarro resultado = procesarEscaneo(req, sku, origen);
        if (resultado.ok()) {
            req.getSession().setAttribute("mensajeExito", resultado.mensaje());
        } else {
            req.getSession().setAttribute("mensajeError", resultado.mensaje());
        }
        return redirigirTrasEscaneo(origen);
    }

    @GetMapping("/api/agregar-por-sku")
    @ResponseBody
    public EscaneoCarroRespuesta agregarPorSkuApi(HttpServletRequest req,
                                                   @RequestParam String sku,
                                                   @RequestParam(required = false) String origen) {
        ResultadoEscaneoCarro resultado = procesarEscaneo(req, sku, origen);
        Producto producto = null;
        if (resultado.ok() && resultado.productoId() != null) {
            String tenant = TenantUtil.getTenantOwner(req);
            producto = productoService.porIdPorOwner(resultado.productoId(), tenant).orElse(null);
        }
        return respuestaCarro(resultado.ok(), resultado.mensaje(), producto);
    }

    private ResultadoEscaneoCarro procesarEscaneo(HttpServletRequest req, String skuParam, String origen) {
        String sku = SkuUtil.normalizar(skuParam);
        if (sku == null) {
            return ResultadoEscaneoCarro.error("Escanea o escribe un codigo de barras / SKU.");
        }
        if (!SkuUtil.longitudValida(sku)) {
            return ResultadoEscaneoCarro.error(
                    "El codigo debe tener maximo " + SkuUtil.LONGITUD_MAXIMA + " caracteres.");
        }

        String tenant = TenantUtil.getTenantOwner(req);
        Optional<Producto> producto = productoService.porSkuPorOwner(sku, tenant);
        if (producto.isEmpty()) {
            return ResultadoEscaneoCarro.error("No hay producto con codigo: " + sku);
        }

        Producto p = producto.get();
        Long id = p.getId();
        int cantidadNueva = carro.getCantidadEnCarro(id) + 1;
        try {
            ventaService.validarStock(tenant, id, cantidadNueva);
        } catch (ServiceJdbcException e) {
            return ResultadoEscaneoCarro.error(e.getMessage());
        }

        carro.addItemCarro(new ItemCarro(1, p));
        return ResultadoEscaneoCarro.exito(p.getNombre() + " agregado al carro.", p.getNombre(), p.getId());
    }

    private record ResultadoEscaneoCarro(boolean ok, String mensaje, String productoNombre, Long productoId) {

        static ResultadoEscaneoCarro exito(String mensaje, String productoNombre, Long productoId) {
            return new ResultadoEscaneoCarro(true, mensaje, productoNombre, productoId);
        }

        static ResultadoEscaneoCarro error(String mensaje) {
            return new ResultadoEscaneoCarro(false, mensaje, null, null);
        }
    }

    private ResultadoEscaneoCarro agregarProductoAlCarro(HttpServletRequest req, Producto producto) {
        Long id = producto.getId();
        String tenant = TenantUtil.getTenantOwner(req);
        int cantidadNueva = carro.getCantidadEnCarro(id) + 1;
        try {
            ventaService.validarStock(tenant, id, cantidadNueva);
        } catch (ServiceJdbcException e) {
            return ResultadoEscaneoCarro.error(e.getMessage());
        }
        carro.addItemCarro(new ItemCarro(1, producto));
        return ResultadoEscaneoCarro.exito(producto.getNombre() + " agregado al carro.", producto.getNombre(), id);
    }

    private EscaneoCarroRespuesta respuestaCarro(boolean ok, String mensaje, Producto producto) {
        Long productoId = producto != null ? producto.getId() : null;
        int cantidadProducto = productoId != null ? carro.getCantidadEnCarro(productoId) : 0;
        int precioUnitario = producto != null ? producto.getPrecio() : 0;
        int importeLinea = cantidadProducto * precioUnitario;
        String nombre = producto != null ? producto.getNombre() : null;
        return new EscaneoCarroRespuesta(
                ok,
                mensaje,
                nombre,
                carro.getTotal(),
                carro.getItems().size(),
                productoId,
                cantidadProducto,
                importeLinea,
                precioUnitario
        );
    }

    private static String redirigirTrasEscaneo(String origen) {
        if ("productos".equals(origen)) {
            return "redirect:/productos#catalogo";
        }
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
        if ("productos".equals(req.getParameter("origen"))) {
            if (req.getSession().getAttribute("mensajeError") == null) {
                req.getSession().setAttribute("mensajeExito", "Carro actualizado.");
            }
            return "redirect:/productos#catalogo";
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
        String nombreCliente = normalizarNombreCliente(req.getParameter("nombreCliente"));

        String tenant = TenantUtil.getTenantOwner(req);

        if (requiereFactura) {
            String error = validarDatosFactura(tenant, rfc, razonSocial, codigoPostalReceptor);
            if (error != null) {
                req.getSession().setAttribute("mensajeError", error);
                resp.sendRedirect(req.getContextPath() + "/carro/ver");
                return;
            }
        }

        if (clienteId > 0 && clienteService.porId(tenant, clienteId).isEmpty()) {
            req.getSession().setAttribute("mensajeError", "El cliente seleccionado no es valido.");
            resp.sendRedirect(req.getContextPath() + "/carro/ver");
            return;
        }

        TicketVenta ticket = construirTicket(carro, usernameOpt.get(), tenant, nombreCliente);
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
            } else if (cfdiTimbradoService.disponible(tenant)) {
                msg += " Factura registrada; revisa el estado del CFDI en Tickets.";
            } else {
                msg += " Comprobante informativo emitido; consultalo en Tickets.";
            }
        }
        req.getSession().setAttribute("mensajeTicket", msg);
        resp.sendRedirect(req.getContextPath() + "/tickets/ver?id=" + ticket.getId());
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

    private static String normalizarNombreCliente(String raw) {
        if (raw == null) {
            return null;
        }
        String limpio = raw.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > 200) {
            return limpio.substring(0, 200);
        }
        return limpio;
    }

    private String validarDatosFactura(String tenant, String rfc, String razonSocial, String codigoPostalReceptor) {
        if (razonSocial.isBlank()) {
            return "Para facturar indique la razón social o nombre del cliente.";
        }
        String errorRfc = FacturaDatosUtil.validarRfcObligatorio(rfc);
        if (errorRfc != null) {
            return errorRfc.replace("Indica el RFC.", "Para facturar indique el RFC del cliente.");
        }
        if (cfdiTimbradoService.disponible(tenant)) {
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

    private TicketVenta construirTicket(Carro carro, String username, String tenantOwner, String nombreCliente) {
        TicketVenta ticket = new TicketVenta();
        ticket.setFolio(generarFolio());
        ticket.setFechaVenta(LocalDateTime.now());
        ticket.setUsernameVendedor(username);
        ticket.setTenantOwner(tenantOwner);
        ticket.setTotal(carro.getTotal());
        if (nombreCliente != null && !nombreCliente.isBlank()) {
            ticket.setNombreCliente(nombreCliente);
        }

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
                model.addAttribute("nombreClientePrefill", cliente.getNombre());
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
