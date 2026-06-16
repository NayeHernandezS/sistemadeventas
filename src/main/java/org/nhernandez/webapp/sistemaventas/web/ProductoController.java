package org.nhernandez.webapp.sistemaventas.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TipoItem;
import org.nhernandez.webapp.sistemaventas.services.CategoriaService;
import org.nhernandez.webapp.sistemaventas.services.CitaServicioService;
import org.nhernandez.webapp.sistemaventas.services.InventarioAlertaService;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.ProductoService;
import org.nhernandez.webapp.sistemaventas.services.UsuarioService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.ServicioPlantillaUtil;
import org.nhernandez.webapp.sistemaventas.util.SkuUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
import org.nhernandez.webapp.sistemaventas.util.UnidadMedidaUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProductoController {

    private final ProductoService service;
    private final LoginService auth;
    private final InventarioAlertaService inventarioAlertaService;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;
    private final CitaServicioService citaServicioService;

    public ProductoController(@ProductoServicePrincipal ProductoService service,
                              LoginService auth,
                              InventarioAlertaService inventarioAlertaService,
                              UsuarioService usuarioService,
                              CategoriaService categoriaService,
                              CitaServicioService citaServicioService) {
        this.service = service;
        this.auth = auth;
        this.inventarioAlertaService = inventarioAlertaService;
        this.usuarioService = usuarioService;
        this.categoriaService = categoriaService;
        this.citaServicioService = citaServicioService;
    }

    @GetMapping({"/productos", "/productos.html"})
    public String listarVentas(HttpServletRequest req, Model model) {
        prepararCatalogoVentas(req, model);
        return "listar";
    }

    @GetMapping("/productos/caja")
    public String cajaMovil(HttpServletRequest req, Model model) {
        prepararCatalogoVentas(req, model);
        return "cajaMovil";
    }

    private void prepararCatalogoVentas(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        boolean esAdmin = RolUtil.esAdmin(req);
        List<Producto> productos = service.listarPorOwner(tenant);
        if (!esAdmin) {
            ocultarCostosInternos(productos);
        }
        model.addAttribute("productos", productos);
        model.addAttribute("username", auth.getUsername(req));
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("logueado", auth.getUsername(req).isPresent());
        agregarAlertasStock(model, productos, tenant);
    }

    @GetMapping({"/crudprod", "/crudprod.html"})
    public String inventario(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        boolean esAdmin = RolUtil.esAdmin(req);
        boolean soloAlerta = "1".equals(req.getParameter("alerta"));
        List<Producto> productos = service.listarPorOwner(tenant);
        if (soloAlerta) {
            int umbral = inventarioAlertaService.getStockMinimo(tenant);
            productos = productos.stream()
                    .filter(p -> inventarioAlertaService.requiereAlerta(p, umbral))
                    .collect(Collectors.toList());
        }
        if (!esAdmin) {
            ocultarCostosInternos(productos);
        }
        model.addAttribute("productos", productos);
        model.addAttribute("soloAlerta", soloAlerta);
        model.addAttribute("logueado", auth.getUsername(req).isPresent());
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("soloLectura", !esAdmin);
        model.addAttribute("mostrarOpcionServicios", negocioConServicios(tenant));
        agregarAlertasStock(model, productos, tenant);
        Object mensajeError = req.getSession().getAttribute("mensajeError");
        if (mensajeError != null) {
            model.addAttribute("mensajeError", mensajeError);
            req.getSession().removeAttribute("mensajeError");
        }
        return "inventario";
    }

    @GetMapping("/productos/servicios")
    public String catalogoServicios(HttpServletRequest req, Model model) {
        String tenant = TenantUtil.getTenantOwner(req);
        if (!negocioConServicios(tenant)) {
            return "redirect:/inicio";
        }
        boolean esAdmin = RolUtil.esAdmin(req);
        model.addAttribute("servicios", citaServicioService.listarServicios(tenant));
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("soloLectura", !esAdmin);
        Object mensajeExito = req.getSession().getAttribute("mensajeExito");
        if (mensajeExito != null) {
            model.addAttribute("mensajeExito", mensajeExito);
            req.getSession().removeAttribute("mensajeExito");
        }
        Object mensajeError = req.getSession().getAttribute("mensajeError");
        if (mensajeError != null) {
            model.addAttribute("mensajeError", mensajeError);
            req.getSession().removeAttribute("mensajeError");
        }
        return "servicios";
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
            TipoItem tipoDefecto = TipoItem.porCodigo(req.getParameter("tipo_item")).orElse(TipoItem.PRODUCTO);
            producto.setTipoItem(tipoDefecto);
            model.addAttribute("producto", producto);
        }
        prepararFormulario(model, tenant);
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
        int precioCompra = parseInt(req.getParameter("precio_compra"), 0);
        int porcentajeGanancia = parseInt(req.getParameter("porcentaje_ganancia"), 0);
        boolean calcularPrecioVenta = "on".equals(req.getParameter("calcular_precio_venta"));
        int existencias = parseExistenciasBase(req.getParameter("existencias_cantidad"),
                req.getParameter("unidad_medida"));
        String unidadMedida = UnidadMedidaUtil.normalizar(req.getParameter("unidad_medida"));
        String sku = req.getParameter("sku");
        String fechaStr = req.getParameter("fecha_registro");
        Long categoriaId = parseLong(req.getParameter("categoria"), 0L);
        long id = parseLong(req.getParameter("id"), 0L);
        TipoItem tipoItem = TipoItem.porCodigo(req.getParameter("tipo_item")).orElse(TipoItem.PRODUCTO);
        if (tipoItem == TipoItem.SERVICIO) {
            existencias = 0;
            unidadMedida = "pza";
            precioCompra = 0;
            porcentajeGanancia = 0;
        }

        if (calcularPrecioVenta && tipoItem != TipoItem.SERVICIO
                && precioCompra > 0 && porcentajeGanancia > 0) {
            Producto borrador = new Producto();
            borrador.setPrecioCompra(precioCompra);
            borrador.setPorcentajeGanancia(porcentajeGanancia);
            int precioSugerido = borrador.calcularPrecioVentaPorGanancia();
            if (precioSugerido > 0) {
                precio = precioSugerido;
            }
        }

        Map<String, String> errores = validarProducto(nombre, sku, fechaStr, precio, precioCompra,
                porcentajeGanancia, existencias, categoriaId, tipoItem);
        LocalDate fecha = parseFecha(fechaStr);

        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setSku(sku != null && !sku.isBlank() ? sku.trim() : null);
        producto.setPrecioCompra(Math.max(precioCompra, 0));
        producto.setPorcentajeGanancia(Math.max(porcentajeGanancia, 0));
        producto.setPrecio(precio);
        producto.setExistencias(existencias);
        producto.setUnidadMedida(unidadMedida);
        producto.setFechaRegistro(fecha);
        producto.setOwnerUsername(tenant);
        producto.setTipoItem(tipoItem);

        if (id > 0 && service.porIdPorOwner(id, tenant).isEmpty()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Producto no pertenece a tu cuenta.");
            return null;
        }

        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);
        producto.setCategoria(categoria);

        if (errores.isEmpty()) {
            service.guardar(producto);
            if (tipoItem == TipoItem.SERVICIO) {
                req.getSession().setAttribute("mensajeExito", "Servicio guardado correctamente.");
                return "redirect:/productos/servicios";
            }
            return "redirect:/crudprod";
        }
        model.addAttribute("errores", errores);
        prepararFormulario(model, tenant);
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
            boolean eraServicio = service.porIdPorOwner(id, tenant)
                    .map(p -> p.getTipoItem() == TipoItem.SERVICIO)
                    .orElse(false);
            service.eliminarPorOwner(id, tenant);
            if (eraServicio || "servicios".equals(req.getParameter("origen"))) {
                req.getSession().setAttribute("mensajeExito", "Servicio eliminado.");
                return "redirect:/productos/servicios";
            }
            return "redirect:/crudprod";
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                "Error el id es null, se debe enviar como parametro en la url!");
        return null;
    }

    private Map<String, String> validarProducto(String nombre, String sku, String fechaStr,
                                                Integer precio, int precioCompra, int porcentajeGanancia,
                                                int existencias, Long categoriaId,
                                                TipoItem tipoItem) {
        Map<String, String> errores = new HashMap<>();
        if (nombre == null || nombre.isBlank()) {
            errores.put("nombre", "el nombre es requerido!");
        }
        boolean esServicio = tipoItem == TipoItem.SERVICIO;
        if (!esServicio) {
            if (sku == null || sku.isBlank()) {
                errores.put("sku", "el sku es requerido!");
            } else if (sku.length() > SkuUtil.LONGITUD_MAXIMA) {
                errores.put("sku", "el sku / codigo de barras debe tener max " + SkuUtil.LONGITUD_MAXIMA + " caracteres!");
            }
        } else if (sku != null && sku.length() > SkuUtil.LONGITUD_MAXIMA) {
            errores.put("sku", "el sku / codigo de barras debe tener max " + SkuUtil.LONGITUD_MAXIMA + " caracteres!");
        }
        if (fechaStr == null || fechaStr.isBlank()) {
            errores.put("fecha_registro", "la fecha es requerida");
        }
        if (precio.equals(0)) {
            errores.put("precio", "el precio de venta es requerido!");
        }
        if (precioCompra < 0) {
            errores.put("precio_compra", "el precio de compra no puede ser negativo");
        }
        if (porcentajeGanancia < 0) {
            errores.put("porcentaje_ganancia", "el porcentaje de ganancia no puede ser negativo");
        }
        if (!esServicio && porcentajeGanancia > 0 && precioCompra <= 0) {
            errores.put("precio_compra", "indica el precio de compra para usar un porcentaje de ganancia");
        }
        if (!esServicio && existencias < 0) {
            errores.put("existencias", "las existencias no pueden ser negativas");
        }
        if (categoriaId.equals(0L)) {
            errores.put("categoria", "la categoria es requerida!");
        }
        return errores;
    }

    private void prepararFormulario(Model model, String tenant) {
        String tipoNegocio = usuarioService.porUsername(tenant)
                .map(u -> u.getTipoNegocio())
                .filter(t -> t != null && !t.isBlank())
                .orElse("otro");
        categoriaService.asegurarCategoriasPlantilla(tenant, tipoNegocio);
        model.addAttribute("categorias", service.listarCategoria(tenant));
        model.addAttribute("tiposItem", TipoItem.values());
        model.addAttribute("tipoNegocio", tipoNegocio);
        model.addAttribute("tipoNegocioEtiqueta", TipoNegocioUtil.etiqueta(tipoNegocio));
        model.addAttribute("sugerenciasServicio", ServicioPlantillaUtil.sugerenciasParaRubro(tipoNegocio));
        model.addAttribute("esRestaurante", TipoNegocioUtil.esRestaurante(tipoNegocio));
        model.addAttribute("unidadesMedida", UnidadMedidaUtil.UNIDADES);
    }

    private boolean negocioConServicios(String tenant) {
        return usuarioService.porUsername(tenant)
                .map(u -> TipoNegocioUtil.tieneOpcionServicios(u.getTipoNegocio()))
                .orElse(false);
    }

    private void ocultarCostosInternos(List<Producto> productos) {
        for (Producto producto : productos) {
            producto.setPrecioCompra(0);
            producto.setPorcentajeGanancia(0);
        }
    }

    private void agregarAlertasStock(Model model, List<Producto> productos, String tenant) {
        model.addAttribute("stockMinimo", inventarioAlertaService.getStockMinimo(tenant));
        model.addAttribute("cantidadAgotados", inventarioAlertaService.contarAgotados(productos));
        model.addAttribute("cantidadStockBajo", inventarioAlertaService.contarStockBajo(productos, tenant));
        model.addAttribute("cantidadConAlerta", inventarioAlertaService.contarConAlerta(productos, tenant));
    }

    private int parseExistenciasBase(String cantidadTexto, String unidad) {
        if (cantidadTexto == null || cantidadTexto.isBlank()) {
            return 0;
        }
        try {
            return UnidadMedidaUtil.aUnidadBase(new BigDecimal(cantidadTexto.trim()), unidad);
        } catch (NumberFormatException e) {
            return 0;
        }
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
