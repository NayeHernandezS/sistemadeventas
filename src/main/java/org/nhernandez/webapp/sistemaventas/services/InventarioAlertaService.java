package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.util.UnidadMedidaUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioAlertaService {

    private final PreferenciasTenantService preferenciasTenantService;

    @Value("${inventario.stock.minimo:5}")
    private int stockMinimoGlobal;

    public InventarioAlertaService(PreferenciasTenantService preferenciasTenantService) {
        this.preferenciasTenantService = preferenciasTenantService;
    }

    public int getStockMinimo() {
        return stockMinimoGlobal;
    }

    public int getStockMinimo(String tenantOwner) {
        return preferenciasTenantService.resolverStockMinimo(tenantOwner, stockMinimoGlobal);
    }

    public boolean esAgotado(Producto producto) {
        return producto != null && producto.esProducto() && producto.getExistencias() <= 0;
    }

    public boolean esStockBajo(Producto producto) {
        return esStockBajo(producto, stockMinimoGlobal);
    }

    public boolean esStockBajo(Producto producto, String tenantOwner) {
        return esStockBajo(producto, getStockMinimo(tenantOwner));
    }

    public boolean esStockBajo(Producto producto, int umbral) {
        if (producto == null || !producto.esProducto() || umbral <= 0) {
            return false;
        }
        int existencias = producto.getExistencias();
        if (existencias <= 0) {
            return false;
        }
        int umbralBase = UnidadMedidaUtil.umbralAUnidadBase(umbral, producto.getUnidadMedida());
        return existencias <= umbralBase;
    }

    public boolean requiereAlerta(Producto producto) {
        return requiereAlerta(producto, stockMinimoGlobal);
    }

    public boolean requiereAlerta(Producto producto, String tenantOwner) {
        return esAgotado(producto) || esStockBajo(producto, tenantOwner);
    }

    public boolean requiereAlerta(Producto producto, int umbral) {
        return esAgotado(producto) || esStockBajo(producto, umbral);
    }

    public int contarAgotados(List<Producto> productos) {
        if (productos == null) {
            return 0;
        }
        return (int) productos.stream().filter(this::esAgotado).count();
    }

    public int contarStockBajo(List<Producto> productos) {
        if (productos == null) {
            return 0;
        }
        return (int) productos.stream().filter(p -> esStockBajo(p, stockMinimoGlobal)).count();
    }

    public int contarStockBajo(List<Producto> productos, String tenantOwner) {
        int umbral = getStockMinimo(tenantOwner);
        if (productos == null) {
            return 0;
        }
        return (int) productos.stream().filter(p -> esStockBajo(p, umbral)).count();
    }

    public int contarConAlerta(List<Producto> productos) {
        if (productos == null) {
            return 0;
        }
        return (int) productos.stream().filter(p -> requiereAlerta(p, stockMinimoGlobal)).count();
    }

    public int contarConAlerta(List<Producto> productos, String tenantOwner) {
        if (productos == null) {
            return 0;
        }
        return (int) productos.stream().filter(p -> requiereAlerta(p, tenantOwner)).count();
    }
}
