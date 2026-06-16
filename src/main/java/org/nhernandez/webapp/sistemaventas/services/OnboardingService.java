package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.ActivacionNegocioEstado;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TicketItem;
import org.nhernandez.webapp.sistemaventas.models.TicketVenta;
import org.nhernandez.webapp.sistemaventas.models.TipoItem;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.nhernandez.webapp.sistemaventas.util.ServicioPlantillaUtil;
import org.nhernandez.webapp.sistemaventas.util.SkuUtil;
import org.nhernandez.webapp.sistemaventas.util.SugerenciaServicio;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OnboardingService {

    private static final DateTimeFormatter FOLIO_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int PRODUCTOS_VENTA_PRACTICA = 12;

    private final PreferenciasTenantService preferenciasTenantService;
    private final PlanLimiteService planLimiteService;
    private final CategoriaService categoriaService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final TicketRepository ticketRepository;
    private final VentaService ventaService;

    public OnboardingService(PreferenciasTenantService preferenciasTenantService,
                             PlanLimiteService planLimiteService,
                             CategoriaService categoriaService,
                             @ProductoServicePrincipal ProductoService productoService,
                             UsuarioService usuarioService,
                             TicketRepository ticketRepository,
                             VentaService ventaService) {
        this.preferenciasTenantService = preferenciasTenantService;
        this.planLimiteService = planLimiteService;
        this.categoriaService = categoriaService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.ticketRepository = ticketRepository;
        this.ventaService = ventaService;
    }

    public boolean requiereOnboarding(String tenantAdmin) {
        if (tenantAdmin == null || tenantAdmin.isBlank()) {
            return false;
        }
        return !preferenciasTenantService.onboardingCompletado(tenantAdmin);
    }

    public void completar(String tenantAdmin) {
        preferenciasTenantService.marcarOnboardingCompletado(tenantAdmin);
    }

    public boolean tienePrimeraVenta(String tenantAdmin) {
        if (tenantAdmin == null || tenantAdmin.isBlank()) {
            return false;
        }
        try {
            return ticketRepository.contarActivosPorTenant(tenantAdmin) > 0;
        } catch (Exception e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    public ActivacionNegocioEstado estadoActivacion(String tenantAdmin) {
        ActivacionNegocioEstado estado = new ActivacionNegocioEstado();
        int productos = planLimiteService.contarProductos(tenantAdmin);
        estado.setTotalProductos(productos);
        estado.setCatalogoListo(productos > 0);
        estado.setPrimeraVentaRegistrada(tienePrimeraVenta(tenantAdmin));
        estado.setOnboardingCompletado(preferenciasTenantService.onboardingCompletado(tenantAdmin));
        try {
            estado.setTotalVentas(ticketRepository.contarActivosPorTenant(tenantAdmin));
        } catch (Exception e) {
            estado.setTotalVentas(0);
        }
        return estado;
    }

    public boolean puedeSaltarPasoProducto(String tenantAdmin) {
        return planLimiteService.contarProductos(tenantAdmin) > 0;
    }

    public List<Producto> productosParaVentaPractica(String tenantAdmin) {
        return productoService.listarPorOwner(tenantAdmin).stream()
                .filter(p -> p.getPrecio() > 0)
                .sorted(Comparator
                        .comparing(Producto::esServicio)
                        .thenComparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER))
                .limit(PRODUCTOS_VENTA_PRACTICA)
                .collect(Collectors.toList());
    }

    public TicketVenta registrarVentaPractica(String tenantAdmin, String username, long productoId) {
        Producto producto = productoService.porIdPorOwner(productoId, tenantAdmin)
                .orElseThrow(() -> new ServiceJdbcException("Producto no encontrado en tu catalogo.", null));

        TicketItem item = new TicketItem();
        item.setProductoId(producto.getId());
        item.setNombreProducto(producto.getNombre());
        item.setPrecioUnitario(producto.getPrecio());
        item.setCantidad(1);
        item.setImporte(producto.getPrecio());

        TicketVenta ticket = new TicketVenta();
        ticket.setFolio(generarFolioTicket());
        ticket.setFechaVenta(LocalDateTime.now());
        ticket.setUsernameVendedor(username);
        ticket.setTenantOwner(tenantAdmin);
        ticket.setTotal(producto.getPrecio());
        ticket.setItems(List.of(item));

        return ventaService.registrarVenta(ticket, null);
    }

    public Optional<Usuario> datosAdmin(String tenantAdmin) {
        return usuarioService.porUsername(tenantAdmin);
    }

    public List<Categoria> categorias(String tenantAdmin) {
        return categoriaService.listarPorOwner(tenantAdmin);
    }

    public void asegurarCategoriasPlantilla(String tenantAdmin, String tipoNegocio) {
        categoriaService.asegurarCategoriasPlantilla(tenantAdmin, tipoNegocio);
    }

    public List<SugerenciaServicio> sugerenciasServicio(String tipoNegocio) {
        return ServicioPlantillaUtil.sugerenciasParaRubro(tipoNegocio);
    }

    public TipoItem tipoItemPorDefecto(String tipoNegocio) {
        return TipoNegocioUtil.predominanServicios(tipoNegocio) ? TipoItem.SERVICIO : TipoItem.PRODUCTO;
    }

    public Map<String, String> validarPrimerItem(String tipoItemStr, String nombre, String sku,
                                                  String precioStr, String existenciasStr, Long categoriaId) {
        TipoItem tipoItem = TipoItem.porCodigo(tipoItemStr).orElse(TipoItem.PRODUCTO);
        Map<String, String> errores = new HashMap<>();
        if (nombre == null || nombre.isBlank()) {
            errores.put("nombre", "El nombre es requerido");
        }
        boolean esServicio = tipoItem == TipoItem.SERVICIO;
        if (!esServicio) {
            if (sku == null || sku.isBlank()) {
                errores.put("sku", "El SKU es requerido");
            } else if (sku.length() > SkuUtil.LONGITUD_MAXIMA) {
                errores.put("sku", "Maximo " + SkuUtil.LONGITUD_MAXIMA + " caracteres");
            }
        } else if (sku != null && sku.length() > SkuUtil.LONGITUD_MAXIMA) {
            errores.put("sku", "Maximo " + SkuUtil.LONGITUD_MAXIMA + " caracteres");
        }
        int precio = parsearEntero(precioStr, 0);
        if (precio <= 0) {
            errores.put("precio", "Indica un precio mayor a 0");
        }
        if (!esServicio) {
            int existencias = parsearEntero(existenciasStr, -1);
            if (existencias < 0) {
                errores.put("existencias", "Las existencias no pueden ser negativas");
            }
        }
        if (categoriaId == null || categoriaId <= 0) {
            errores.put("categoria", "Selecciona una categoria");
        }
        return errores;
    }

    /** Compatibilidad con tests existentes. */
    public Map<String, String> validarPrimerProducto(String nombre, String sku, String precioStr,
                                                      String existenciasStr, Long categoriaId) {
        return validarPrimerItem(TipoItem.PRODUCTO.name(), nombre, sku, precioStr, existenciasStr, categoriaId);
    }

    public void guardarPrimerItem(String tenantAdmin, TipoItem tipoItem, String nombre, String sku,
                                  int precio, int existencias, Long categoriaId) {
        Producto producto = new Producto();
        producto.setOwnerUsername(tenantAdmin);
        producto.setNombre(nombre.trim());
        producto.setSku(sku != null && !sku.isBlank() ? sku.trim() : null);
        producto.setPrecio(precio);
        producto.setTipoItem(tipoItem);
        producto.setExistencias(tipoItem == TipoItem.SERVICIO ? 0 : existencias);
        producto.setFechaRegistro(LocalDate.now());
        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);
        producto.setCategoria(categoria);
        productoService.guardar(producto);
    }

    public void guardarPrimerProducto(String tenantAdmin, String nombre, String sku, int precio,
                                      int existencias, Long categoriaId) {
        guardarPrimerItem(tenantAdmin, TipoItem.PRODUCTO, nombre, sku, precio, existencias, categoriaId);
    }

    public String etiquetaTipoNegocio(String tipoNegocio) {
        return TipoNegocioUtil.etiqueta(tipoNegocio);
    }

    public boolean importaCatalogoProductos(String tipoNegocio) {
        return TipoNegocioUtil.importaCatalogoProductos(tipoNegocio);
    }

    public boolean predominanServicios(String tipoNegocio) {
        return TipoNegocioUtil.predominanServicios(tipoNegocio);
    }

    public boolean tieneOpcionServicios(String tipoNegocio) {
        return TipoNegocioUtil.tieneOpcionServicios(tipoNegocio);
    }

    private static String generarFolioTicket() {
        String timestamp = FOLIO_TS.format(LocalDateTime.now());
        String sufijo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TCK-" + timestamp + "-" + sufijo;
    }

    private static int parsearEntero(String valor, int defecto) {
        if (valor == null || valor.isBlank()) {
            return defecto;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }
}
