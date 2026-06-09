package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TipoItem;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.util.ServicioPlantillaUtil;
import org.nhernandez.webapp.sistemaventas.util.SkuUtil;
import org.nhernandez.webapp.sistemaventas.util.SugerenciaServicio;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OnboardingService {

    private final PreferenciasTenantService preferenciasTenantService;
    private final PlanLimiteService planLimiteService;
    private final CategoriaService categoriaService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    public OnboardingService(PreferenciasTenantService preferenciasTenantService,
                             PlanLimiteService planLimiteService,
                             CategoriaService categoriaService,
                             @ProductoServicePrincipal ProductoService productoService,
                             UsuarioService usuarioService) {
        this.preferenciasTenantService = preferenciasTenantService;
        this.planLimiteService = planLimiteService;
        this.categoriaService = categoriaService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
    }

    public boolean requiereOnboarding(String tenantAdmin) {
        if (tenantAdmin == null || tenantAdmin.isBlank()) {
            return false;
        }
        if (preferenciasTenantService.onboardingCompletado(tenantAdmin)) {
            return false;
        }
        if (planLimiteService.contarProductos(tenantAdmin) > 0) {
            preferenciasTenantService.marcarOnboardingCompletado(tenantAdmin);
            return false;
        }
        return true;
    }

    public void completar(String tenantAdmin) {
        preferenciasTenantService.marcarOnboardingCompletado(tenantAdmin);
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
