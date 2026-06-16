package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.ListaCompraHoy;
import org.nhernandez.webapp.sistemaventas.models.MovimientoInventario;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TipoMovimientoInventario;
import org.nhernandez.webapp.sistemaventas.services.InventarioMovimientoService;
import org.nhernandez.webapp.sistemaventas.services.ListaCompraService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ProductoService;
import org.nhernandez.webapp.sistemaventas.services.ServiceJdbcException;
import org.nhernandez.webapp.sistemaventas.util.ListaCompraCsvExporter;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProductoService productoService;
    private final InventarioMovimientoService inventarioMovimientoService;
    private final ListaCompraService listaCompraService;
    private final ListaCompraCsvExporter listaCompraCsvExporter;
    private final LoginService loginService;

    public InventarioController(@ProductoServicePrincipal ProductoService productoService,
                                InventarioMovimientoService inventarioMovimientoService,
                                ListaCompraService listaCompraService,
                                ListaCompraCsvExporter listaCompraCsvExporter,
                                LoginService loginService) {
        this.productoService = productoService;
        this.inventarioMovimientoService = inventarioMovimientoService;
        this.listaCompraService = listaCompraService;
        this.listaCompraCsvExporter = listaCompraCsvExporter;
        this.loginService = loginService;
    }

    @GetMapping("/comprar-hoy")
    public String comprarHoy(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (loginService.getUsername(req).isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesion para consultar la lista de compra.");
            return null;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        ListaCompraHoy lista = listaCompraService.generar(tenant);
        model.addAttribute("lista", lista);
        model.addAttribute("esAdmin", RolUtil.esAdmin(req));
        return "comprarHoy";
    }

    @GetMapping("/comprar-hoy/export")
    public void exportarComprarHoy(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (loginService.getUsername(req).isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesion para exportar la lista de compra.");
            return;
        }
        String tenant = TenantUtil.getTenantOwner(req);
        ListaCompraHoy lista = listaCompraService.generar(tenant);
        byte[] csv = listaCompraCsvExporter.exportar(lista);
        String nombre = "comprar-hoy-" + lista.getFecha().replace("-", "") + ".csv";
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
        resp.setContentLength(csv.length);
        resp.getOutputStream().write(csv);
        resp.getOutputStream().flush();
    }

    @GetMapping("/movimientos")
    public String movimientos(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            return denegadoInventario(req, "Solo el administrador puede consultar movimientos de inventario.");
        }
        String tenant = TenantUtil.getTenantOwner(req);
        List<MovimientoInventario> movimientos = inventarioMovimientoService.listarRecientes(tenant, 50);
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("formatoFecha", FORMATO_FECHA);
        return "movimientosInventario";
    }

    @GetMapping("/ajuste")
    public String ajusteGet(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            return denegadoInventario(req, "Solo el administrador puede ajustar inventario.");
        }
        String tenant = TenantUtil.getTenantOwner(req);
        long productoId = parseLong(req.getParameter("id"), 0L);
        if (productoId <= 0) {
            return inventarioConMensaje(req, "Selecciona un producto valido para ajustar.");
        }
        Optional<Producto> producto = productoService.porIdPorOwner(productoId, tenant);
        if (producto.isEmpty()) {
            return inventarioConMensaje(req, "Producto no encontrado en tu inventario.");
        }
        if (producto.get().esServicio()) {
            return inventarioConMensaje(req, "Los servicios no tienen stock que ajustar.");
        }
        model.addAttribute("producto", producto.get());
        model.addAttribute("tiposMovimiento", Arrays.asList(TipoMovimientoInventario.values()));
        return "inventarioAjuste";
    }

    @PostMapping("/ajuste")
    public String ajustePost(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            return denegadoInventario(req, "Solo el administrador puede ajustar inventario.");
        }

        String tenant = TenantUtil.getTenantOwner(req);
        String username = loginService.getUsername(req).orElse("");
        long productoId = parseLong(req.getParameter("productoId"), 0L);
        String tipoStr = req.getParameter("tipo");
        int cantidad = parseInt(req.getParameter("cantidad"), -1);
        String motivo = req.getParameter("motivo");

        Map<String, String> errores = new HashMap<>();
        TipoMovimientoInventario tipo = TipoMovimientoInventario.porCodigo(tipoStr).orElse(null);
        if (tipo == null) {
            errores.put("tipo", "Seleccione un tipo de movimiento valido.");
        }

        Optional<Producto> productoOpt = productoId > 0
                ? productoService.porIdPorOwner(productoId, tenant)
                : Optional.empty();
        if (productoOpt.isEmpty()) {
            return inventarioConMensaje(req, "Producto no encontrado en tu inventario.");
        }

        if (errores.isEmpty() && tipo != null) {
            try {
                inventarioMovimientoService.aplicarMovimiento(
                        tenant, username, productoId, tipo, cantidad, motivo);
                req.getSession().setAttribute("mensajeExito",
                        "Movimiento registrado. Nuevas existencias: "
                                + existenciasTrasMovimiento(productoOpt.get(), tipo, cantidad));
                return "redirect:/crudprod";
            } catch (ServiceJdbcException e) {
                errores.put("general", e.getMessage());
            }
        }

        model.addAttribute("errores", errores);
        model.addAttribute("producto", productoOpt.get());
        model.addAttribute("tiposMovimiento", Arrays.asList(TipoMovimientoInventario.values()));
        model.addAttribute("tipoSeleccionado", tipoStr);
        model.addAttribute("cantidadIngresada", cantidad >= 0 ? cantidad : "");
        model.addAttribute("motivoIngresado", motivo);
        return "inventarioAjuste";
    }

    private static int existenciasTrasMovimiento(Producto producto, TipoMovimientoInventario tipo, int cantidad) {
        int antes = producto.getExistencias();
        return switch (tipo) {
            case ENTRADA -> antes + cantidad;
            case SALIDA -> antes - cantidad;
            case AJUSTE -> cantidad;
        };
    }

    private long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String denegadoInventario(HttpServletRequest req, String mensaje) {
        req.getSession().setAttribute("mensajeError", mensaje);
        return "redirect:/crudprod";
    }

    private static String inventarioConMensaje(HttpServletRequest req, String mensaje) {
        req.getSession().setAttribute("mensajeError", mensaje);
        return "redirect:/crudprod";
    }
}
