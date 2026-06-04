package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.catalogo.CatalogoPlantilla;
import org.nhernandez.webapp.sistemaventas.catalogo.CatalogoPlantillaLoader;
import org.nhernandez.webapp.sistemaventas.catalogo.ProductoPlantilla;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.repositories.CategoriaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.util.CategoriaPlantillaUtil;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class CatalogoPlantillaServiceImpl implements CatalogoPlantillaService {

    private static final int EXISTENCIAS_DEFAULT = 10;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final PlanLimiteService planLimiteService;

    public CatalogoPlantillaServiceImpl(ProductoRepository productoRepository,
                                        CategoriaRepository categoriaRepository,
                                        PlanLimiteService planLimiteService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.planLimiteService = planLimiteService;
    }

    @Override
    public ResultadoImportacionCatalogo importarCatalogoInicial(String tenantOwner, String tipoNegocio) {
        if (tenantOwner == null || tenantOwner.isBlank()) {
            return new ResultadoImportacionCatalogo(0, 0, 0);
        }
        try {
            if (productoRepository.contarPorOwner(tenantOwner) > 0) {
                return new ResultadoImportacionCatalogo(0, 0, 0);
            }

            Optional<CatalogoPlantilla> catalogoOpt = CatalogoPlantillaLoader.cargar(tipoNegocio);
            if (catalogoOpt.isEmpty() || catalogoOpt.get().productos() == null
                    || catalogoOpt.get().productos().isEmpty()) {
                return new ResultadoImportacionCatalogo(0, 0, 0);
            }

            List<ProductoPlantilla> plantillas = catalogoOpt.get().productos();
            categoriaRepository.crearSugeridasSiNoExisten(
                    tenantOwner, CategoriaPlantillaUtil.paraTipoNegocio(tipoNegocio));

            Map<String, Long> categoriasPorNombre = mapaCategorias(tenantOwner);
            PlanSuscripcion plan = planLimiteService.planActivo(tenantOwner);
            int cupo = plan.getMaxProductos();
            int importados = 0;
            int omitidos = 0;

            for (ProductoPlantilla plantilla : plantillas) {
                if (importados >= cupo) {
                    omitidos++;
                    continue;
                }
                if (!plantillaValida(plantilla)) {
                    omitidos++;
                    continue;
                }
                String sku = plantilla.sku().trim();
                if (productoRepository.existeSkuPorOwner(tenantOwner, sku)) {
                    omitidos++;
                    continue;
                }
                Long categoriaId = resolverCategoria(plantilla.categoria(), categoriasPorNombre);
                if (categoriaId == null) {
                    omitidos++;
                    continue;
                }

                Producto producto = new Producto();
                producto.setOwnerUsername(tenantOwner);
                producto.setNombre(plantilla.nombre().trim());
                producto.setSku(sku);
                producto.setPrecio(Math.max(plantilla.precio(), 1));
                producto.setExistencias(plantilla.existencias() >= 0
                        ? plantilla.existencias()
                        : EXISTENCIAS_DEFAULT);
                producto.setFechaRegistro(LocalDate.now());
                Categoria categoria = new Categoria();
                categoria.setId(categoriaId);
                producto.setCategoria(categoria);

                productoRepository.guardar(producto);
                importados++;
            }

            return new ResultadoImportacionCatalogo(importados, omitidos, plantillas.size());
        } catch (SQLException e) {
            throw new ServiceJdbcException("No se pudo importar el catalogo inicial: " + e.getMessage(), e);
        }
    }

    private Map<String, Long> mapaCategorias(String tenantOwner) throws SQLException {
        Map<String, Long> mapa = new HashMap<>();
        for (Categoria categoria : categoriaRepository.listarPorOwner(tenantOwner)) {
            if (categoria.getNombre() != null && categoria.getId() != null) {
                mapa.put(normalizarNombre(categoria.getNombre()), categoria.getId());
            }
        }
        return mapa;
    }

    private static Long resolverCategoria(String nombreCategoria, Map<String, Long> categoriasPorNombre) {
        if (nombreCategoria != null && !nombreCategoria.isBlank()) {
            Long id = categoriasPorNombre.get(normalizarNombre(nombreCategoria));
            if (id != null) {
                return id;
            }
        }
        Long general = categoriasPorNombre.get(normalizarNombre("General"));
        if (general != null) {
            return general;
        }
        return categoriasPorNombre.values().stream().findFirst().orElse(null);
    }

    private static boolean plantillaValida(ProductoPlantilla plantilla) {
        return plantilla != null
                && plantilla.nombre() != null && !plantilla.nombre().isBlank()
                && plantilla.sku() != null && !plantilla.sku().isBlank()
                && plantilla.sku().trim().length() <= 10;
    }

    private static String normalizarNombre(String nombre) {
        return nombre.trim().toLowerCase(Locale.ROOT);
    }
}
