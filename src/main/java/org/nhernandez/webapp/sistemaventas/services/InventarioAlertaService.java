package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioAlertaService {

    @Value("${inventario.stock.minimo:5}")
    private int stockMinimo;

    public int getStockMinimo() {
        return stockMinimo;
    }

    public boolean esAgotado(Producto producto) {
        return producto != null && producto.getExistencias() <= 0;
    }

    public boolean esStockBajo(Producto producto) {
        return producto != null
                && producto.getExistencias() > 0
                && producto.getExistencias() <= stockMinimo;
    }

    public boolean requiereAlerta(Producto producto) {
        return esAgotado(producto) || esStockBajo(producto);
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
        return (int) productos.stream().filter(this::esStockBajo).count();
    }

    public int contarConAlerta(List<Producto> productos) {
        if (productos == null) {
            return 0;
        }
        return (int) productos.stream().filter(this::requiereAlerta).count();
    }
}
