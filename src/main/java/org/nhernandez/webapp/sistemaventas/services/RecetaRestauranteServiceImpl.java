package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.PlatilloCostoResumen;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.Receta;
import org.nhernandez.webapp.sistemaventas.models.RecetaLinea;
import org.nhernandez.webapp.sistemaventas.repositories.RecetaRepository;
import org.nhernandez.webapp.sistemaventas.util.RecetaRestauranteUtil;
import org.nhernandez.webapp.sistemaventas.util.TipoNegocioUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RecetaRestauranteServiceImpl implements RecetaRestauranteService {

    private final RecetaRepository recetaRepository;
    private final ProductoService productoService;

    public RecetaRestauranteServiceImpl(RecetaRepository recetaRepository,
                                        @ProductoServicePrincipal ProductoService productoService) {
        this.recetaRepository = recetaRepository;
        this.productoService = productoService;
    }

    @Override
    public boolean aplicaParaTenant(String tipoNegocio) {
        return TipoNegocioUtil.esRestaurante(tipoNegocio);
    }

    @Override
    public List<PlatilloCostoResumen> listarResumenPlatillos(String tenantOwner) {
        List<Producto> platillos = listarPlatillos(tenantOwner);
        List<PlatilloCostoResumen> resumen = new ArrayList<>();
        for (Producto platillo : platillos) {
            resumen.add(construirResumen(tenantOwner, platillo));
        }
        resumen.sort(Comparator.comparing(PlatilloCostoResumen::getNombre, String.CASE_INSENSITIVE_ORDER));
        return resumen;
    }

    @Override
    public List<Producto> listarInsumos(String tenantOwner) {
        return productoService.listarPorOwner(tenantOwner).stream()
                .filter(RecetaRestauranteUtil::esInsumo)
                .sorted(Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public List<Producto> listarPlatillos(String tenantOwner) {
        return productoService.listarPorOwner(tenantOwner).stream()
                .filter(RecetaRestauranteUtil::esPlatillo)
                .sorted(Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public List<RecetaLinea> lineasConCosto(String tenantOwner, Long productoId) {
        try {
            Optional<Receta> receta = recetaRepository.porProductoId(tenantOwner, productoId);
            if (receta.isEmpty()) {
                return List.of();
            }
            List<RecetaLinea> lineas = recetaRepository.listarLineasPorReceta(receta.get().getId());
            for (RecetaLinea linea : lineas) {
                productoService.porIdPorOwner(linea.getInsumoProductoId(), tenantOwner).ifPresent(insumo -> {
                    linea.setInsumoNombre(insumo.getNombre());
                    linea.setCostoLinea(RecetaRestauranteUtil.calcularCostoLinea(insumo, linea.getCantidad()));
                });
            }
            return lineas;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Producto> platilloPorId(String tenantOwner, Long productoId) {
        if (productoId == null || productoId <= 0) {
            return Optional.empty();
        }
        return productoService.porIdPorOwner(productoId, tenantOwner)
                .filter(RecetaRestauranteUtil::esPlatillo);
    }

    @Override
    public void guardarReceta(String tenantOwner, Long productoId, List<RecetaLinea> lineas) {
        Producto platillo = platilloPorId(tenantOwner, productoId)
                .orElseThrow(() -> new IllegalArgumentException("El platillo no existe o no pertenece a tu cuenta"));
        List<RecetaLinea> lineasValidas = validarLineas(tenantOwner, lineas);
        try {
            Receta receta = recetaRepository.porProductoId(tenantOwner, platillo.getId())
                    .orElseGet(() -> {
                        try {
                            Long id = recetaRepository.crearReceta(tenantOwner, platillo.getId());
                            Receta nueva = new Receta();
                            nueva.setId(id);
                            nueva.setTenantOwner(tenantOwner);
                            nueva.setProductoId(platillo.getId());
                            return nueva;
                        } catch (SQLException e) {
                            throw new ServiceJdbcException(e.getMessage(), e);
                        }
                    });
            recetaRepository.eliminarLineasPorReceta(receta.getId());
            for (RecetaLinea linea : lineasValidas) {
                recetaRepository.insertarLinea(
                        receta.getId(),
                        linea.getInsumoProductoId(),
                        linea.getCantidad(),
                        linea.getUnidad());
            }
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void eliminarReceta(String tenantOwner, Long productoId) {
        try {
            recetaRepository.eliminarRecetaPorProducto(tenantOwner, productoId);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    private PlatilloCostoResumen construirResumen(String tenantOwner, Producto platillo) {
        List<RecetaLinea> lineas = lineasConCosto(tenantOwner, platillo.getId());
        int costo = lineas.stream().mapToInt(RecetaLinea::getCostoLinea).sum();
        PlatilloCostoResumen resumen = new PlatilloCostoResumen();
        resumen.setProductoId(platillo.getId());
        resumen.setNombre(platillo.getNombre());
        if (platillo.getCategoria() != null) {
            resumen.setCategoria(platillo.getCategoria().getNombre());
        }
        resumen.setPrecioVenta(platillo.getPrecio());
        resumen.setCostoReceta(costo);
        resumen.setMargenPesos(RecetaRestauranteUtil.calcularMargenPesos(platillo.getPrecio(), costo));
        resumen.setMargenPorcentaje(RecetaRestauranteUtil.calcularMargenPorcentaje(platillo.getPrecio(), costo));
        resumen.setCantidadLineas(lineas.size());
        resumen.setTieneReceta(!lineas.isEmpty());
        return resumen;
    }

    private List<RecetaLinea> validarLineas(String tenantOwner, List<RecetaLinea> lineas) {
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un ingrediente a la receta");
        }
        List<RecetaLinea> validas = new ArrayList<>();
        Set<Long> insumosUsados = new HashSet<>();
        for (RecetaLinea linea : lineas) {
            if (linea == null || linea.getInsumoProductoId() == null || linea.getInsumoProductoId() <= 0) {
                continue;
            }
            if (!insumosUsados.add(linea.getInsumoProductoId())) {
                throw new IllegalArgumentException("No repitas el mismo insumo en la receta");
            }
            BigDecimal cantidad = linea.getCantidad();
            if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad de cada ingrediente debe ser mayor a cero");
            }
            Producto insumo = productoService.porIdPorOwner(linea.getInsumoProductoId(), tenantOwner)
                    .orElse(null);
            if (insumo == null || !RecetaRestauranteUtil.esInsumo(insumo)) {
                throw new IllegalArgumentException("Solo puedes usar productos de categoria Insumos o Desechables");
            }
            String unidad = linea.getUnidad();
            if (unidad == null || unidad.isBlank()) {
                unidad = "pza";
            }
            RecetaLinea valida = new RecetaLinea();
            valida.setInsumoProductoId(linea.getInsumoProductoId());
            valida.setCantidad(cantidad);
            valida.setUnidad(unidad.trim());
            validas.add(valida);
        }
        if (validas.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un ingrediente valido a la receta");
        }
        return validas;
    }
}
