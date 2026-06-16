package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.configs.ProductoServicePrincipal;
import org.nhernandez.webapp.sistemaventas.models.ListaCompraHoy;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.ProductoCompraSugerida;
import org.nhernandez.webapp.sistemaventas.models.ProductoVentaRanking;
import org.nhernandez.webapp.sistemaventas.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ListaCompraServiceImpl implements ListaCompraService {

    private static final int DIAS_VENTAS = 7;
    private static final int TOP_VENTAS_CONSULTA = 50;
    private static final int MULTIPLICADOR_OBJETIVO = 2;

    private final ProductoService productoService;
    private final InventarioAlertaService inventarioAlertaService;
    private final TicketRepository ticketRepository;

    public ListaCompraServiceImpl(@ProductoServicePrincipal ProductoService productoService,
                                  InventarioAlertaService inventarioAlertaService,
                                  TicketRepository ticketRepository) {
        this.productoService = productoService;
        this.inventarioAlertaService = inventarioAlertaService;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public ListaCompraHoy generar(String tenantOwner) {
        return generar(tenantOwner, null);
    }

    @Override
    public ListaCompraHoy generar(String tenantOwner, Integer limiteVista) {
        int umbral = inventarioAlertaService.getStockMinimo(tenantOwner);
        List<Producto> catalogo = productoService.listarPorOwner(tenantOwner);
        Map<Long, Integer> ventas7d = ventasUltimosDias(tenantOwner, DIAS_VENTAS);

        List<ProductoCompraSugerida> items = new ArrayList<>();
        for (Producto producto : catalogo) {
            if (!producto.esProducto() || !inventarioAlertaService.requiereAlerta(producto, umbral)) {
                continue;
            }
            items.add(construirItem(producto, umbral, ventas7d));
        }

        items.sort(Comparator
                .comparing(ProductoCompraSugerida::isAgotado).reversed()
                .thenComparingInt(ProductoCompraSugerida::getExistencias)
                .thenComparing(Comparator.comparingInt(ProductoCompraSugerida::getUnidadesVendidas7d).reversed())
                .thenComparing(ProductoCompraSugerida::getNombre, String.CASE_INSENSITIVE_ORDER));

        ListaCompraHoy lista = new ListaCompraHoy();
        lista.setFecha(LocalDate.now().toString());
        lista.setStockMinimo(umbral);
        lista.setTotalProductos(items.size());
        lista.setTotalUnidadesSugeridas(items.stream().mapToInt(ProductoCompraSugerida::getCantidadSugerida).sum());
        lista.setCostoEstimadoTotal(items.stream().mapToInt(ProductoCompraSugerida::getCostoEstimadoReposicion).sum());

        if (limiteVista != null && limiteVista > 0 && items.size() > limiteVista) {
            lista.setProductos(items.subList(0, limiteVista));
        } else {
            lista.setProductos(items);
        }
        return lista;
    }

    private ProductoCompraSugerida construirItem(Producto producto, int umbral, Map<Long, Integer> ventas7d) {
        int existencias = producto.getExistencias();
        int stockObjetivo = umbral * MULTIPLICADOR_OBJETIVO;
        int cantidadSugerida = Math.max(stockObjetivo - existencias, 1);
        int unidades7d = ventas7d.getOrDefault(producto.getId(), 0);
        int precioCompra = Math.max(producto.getPrecioCompra(), 0);

        ProductoCompraSugerida item = new ProductoCompraSugerida();
        item.setProductoId(producto.getId());
        item.setNombre(producto.getNombre());
        item.setSku(producto.getSku());
        item.setCategoria(producto.getCategoria() != null ? producto.getCategoria().getNombre() : "");
        item.setExistencias(existencias);
        item.setStockMinimo(umbral);
        item.setNivelAlerta(inventarioAlertaService.esAgotado(producto) ? "AGOTADO" : "STOCK_BAJO");
        item.setCantidadSugerida(cantidadSugerida);
        item.setUnidadesVendidas7d(unidades7d);
        item.setPrecioCompra(precioCompra);
        item.setCostoEstimadoReposicion(precioCompra > 0 ? cantidadSugerida * precioCompra : 0);
        return item;
    }

    private Map<Long, Integer> ventasUltimosDias(String tenantOwner, int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.minusDays(dias - 1L).atStartOfDay();
        LocalDateTime finExclusivo = hoy.plusDays(1).atStartOfDay();
        try {
            List<ProductoVentaRanking> ranking = ticketRepository.topProductosVendidosPorTenant(
                    tenantOwner, inicio, finExclusivo, TOP_VENTAS_CONSULTA);
            Map<Long, Integer> mapa = new HashMap<>();
            for (ProductoVentaRanking r : ranking) {
                if (r.getProductoId() != null) {
                    mapa.put(r.getProductoId(), r.getUnidadesVendidas());
                }
            }
            return mapa;
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }
}
