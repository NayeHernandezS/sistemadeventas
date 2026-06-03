package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.repositories.CategoriaRepository;
import org.nhernandez.webapp.sistemaventas.util.CategoriaPlantillaUtil;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
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
    private final CategoriaRepository categoriaRepository;

    public OnboardingService(PreferenciasTenantService preferenciasTenantService,
                             PlanLimiteService planLimiteService,
                             CategoriaService categoriaService,
                             @ProductoServicePrincipal ProductoService productoService,
                             UsuarioService usuarioService,
                             CategoriaRepository categoriaRepository) {
        this.preferenciasTenantService = preferenciasTenantService;
        this.planLimiteService = planLimiteService;
        this.categoriaService = categoriaService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
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
        try {
            categoriaRepository.crearSugeridasSiNoExisten(
                    tenantAdmin, CategoriaPlantillaUtil.paraTipoNegocio(tipoNegocio));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    public Map<String, String> validarPrimerProducto(String nombre, String sku, String precioStr,
                                                      String existenciasStr, Long categoriaId) {
        Map<String, String> errores = new HashMap<>();
        if (nombre == null || nombre.isBlank()) {
            errores.put("nombre", "El nombre es requerido");
        }
        if (sku == null || sku.isBlank()) {
            errores.put("sku", "El SKU es requerido");
        } else if (sku.length() > 10) {
            errores.put("sku", "Maximo 10 caracteres");
        }
        int precio = parsearEntero(precioStr, 0);
        if (precio <= 0) {
            errores.put("precio", "Indica un precio mayor a 0");
        }
        int existencias = parsearEntero(existenciasStr, -1);
        if (existencias < 0) {
            errores.put("existencias", "Las existencias no pueden ser negativas");
        }
        if (categoriaId == null || categoriaId <= 0) {
            errores.put("categoria", "Selecciona una categoria");
        }
        return errores;
    }

    public void guardarPrimerProducto(String tenantAdmin, String nombre, String sku, int precio,
                                        int existencias, Long categoriaId) {
        Producto producto = new Producto();
        producto.setOwnerUsername(tenantAdmin);
        producto.setNombre(nombre.trim());
        producto.setSku(sku.trim());
        producto.setPrecio(precio);
        producto.setExistencias(existencias);
        producto.setFechaRegistro(LocalDate.now());
        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);
        producto.setCategoria(categoria);
        productoService.guardar(producto);
    }

    public String etiquetaTipoNegocio(String tipoNegocio) {
        return TipoNegocioUtil.etiqueta(tipoNegocio);
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
