package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.services.InventarioAlertaService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ProductoService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductoController {

    private final ProductoService service;
    private final LoginService auth;
    private final InventarioAlertaService inventarioAlertaService;

    public ProductoController(@ProductoServicePrincipal ProductoService service,
                              LoginService auth,
                              InventarioAlertaService inventarioAlertaService) {
        this.service = service;
        this.auth = auth;
        this.inventarioAlertaService = inventarioAlertaService;
    }

    @GetMapping({"/productos", "/productos.html"})
    public String listarVentas(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        List<Producto> productos = service.listarPorOwner(tenant);
        model.addAttribute("productos", productos);
        model.addAttribute("username", auth.getUsername(req));
        agregarAlertasStock(model, productos);
        return "listar";
    }

    @GetMapping({"/crudprod", "/crudprod.html"})
    public String inventario(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        boolean esAdmin = RolUtil.esAdmin(req);
        List<Producto> productos = service.listarPorOwner(tenant);
        model.addAttribute("productos", productos);
        model.addAttribute("username", auth.getUsername(req));
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("soloLectura", !esAdmin);
        agregarAlertasStock(model, productos);
        return "inventario";
    }

    @GetMapping("/productos/form")
    public String formularioGet(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo el administrador puede editar productos.");
            return null;
        }
        long id = parseLong(req.getParameter("id"), 0L);
        Producto producto = new Producto();
        producto.setCategoria(new Categoria());
        String tenant = TenantUtil.getTenantOwner(req);
        if (id > 0) {
            service.porIdPorOwner(id, tenant).ifPresent(p -> model.addAttribute("producto", p));
        }
        if (!model.containsAttribute("producto")) {
            model.addAttribute("producto", producto);
        }
        model.addAttribute("categorias", service.listarCategoria(tenant));
        return "form";
    }

    @PostMapping("/productos/form")
    public String formularioPost(HttpServletRequest req, Model model, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo el administrador puede guardar productos.");
            return null;
        }

        String tenant = TenantUtil.getTenantOwner(req);
        String nombre = req.getParameter("nombre");
        Integer precio = parseInt(req.getParameter("precio"), 0);
        int existencias = parseInt(req.getParameter("existencias"), 0);
        String sku = req.getParameter("sku");
        String fechaStr = req.getParameter("fecha_registro");
        Long categoriaId = parseLong(req.getParameter("categoria"), 0L);
        long id = parseLong(req.getParameter("id"), 0L);

        Map<String, String> errores = validarProducto(nombre, sku, fechaStr, precio, existencias, categoriaId);
        LocalDate fecha = parseFecha(fechaStr);

        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setSku(sku);
        producto.setPrecio(precio);
        producto.setExistencias(existencias);
        producto.setFechaRegistro(fecha);
        producto.setOwnerUsername(tenant);

        if (id > 0 && service.porIdPorOwner(id, tenant).isEmpty()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Producto no pertenece a tu cuenta.");
            return null;
        }

        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);
        producto.setCategoria(categoria);

        if (errores.isEmpty()) {
            service.guardar(producto);
            return "redirect:/crudprod";
        }
        model.addAttribute("errores", errores);
        model.addAttribute("categorias", service.listarCategoria(tenant));
        model.addAttribute("producto", producto);
        return "form";
    }

    @GetMapping("/productos/eliminar")
    public String eliminar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!RolUtil.esAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo el administrador puede eliminar productos.");
            return null;
        }
        long id = parseLong(req.getParameter("id"), 0L);
        String tenant = TenantUtil.getTenantOwner(req);
        if (id > 0) {
            service.eliminarPorOwner(id, tenant);
            return "redirect:/crudprod";
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                "Error el id es null, se debe enviar como parametro en la url!");
        return null;
    }

    private Map<String, String> validarProducto(String nombre, String sku, String fechaStr,
                                                Integer precio, int existencias, Long categoriaId) {
        Map<String, String> errores = new HashMap<>();
        if (nombre == null || nombre.isBlank()) {
            errores.put("nombre", "el nombre es requerido!");
        }
        if (sku == null || sku.isBlank()) {
            errores.put("sku", "el sku es requerido!");
        } else if (sku.length() > 10) {
            errores.put("sku", "el sku debe tener max 10 caracteres!");
        }
        if (fechaStr == null || fechaStr.isBlank()) {
            errores.put("fecha_registro", "la fecha es requerida");
        }
        if (precio.equals(0)) {
            errores.put("precio", "el precio es requerido!");
        }
        if (existencias < 0) {
            errores.put("existencias", "las existencias no pueden ser negativas");
        }
        if (categoriaId.equals(0L)) {
            errores.put("categoria", "la categoria es requerida!");
        }
        return errores;
    }

    private void agregarAlertasStock(Model model, List<Producto> productos) {
        model.addAttribute("stockMinimo", inventarioAlertaService.getStockMinimo());
        model.addAttribute("cantidadAgotados", inventarioAlertaService.contarAgotados(productos));
        model.addAttribute("cantidadStockBajo", inventarioAlertaService.contarStockBajo(productos));
        model.addAttribute("cantidadConAlerta", inventarioAlertaService.contarConAlerta(productos));
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

    private LocalDate parseFecha(String fechaStr) {
        try {
            return LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
