package org.nhernandez.webapp.sistemaventas.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Indica si un tenant puede contratar un plan segun su uso actual de vendedores y productos.
 */
public class PlanContratibilidad {

    private final String planCodigo;
    private final boolean contratable;
    private final List<String> motivos;
    private final int vendedoresUsados;
    private final int productosUsados;
    private final int maxVendedores;
    private final int maxProductos;

    public PlanContratibilidad(String planCodigo, boolean contratable, List<String> motivos,
                               int vendedoresUsados, int productosUsados,
                               int maxVendedores, int maxProductos) {
        this.planCodigo = planCodigo;
        this.contratable = contratable;
        this.motivos = Collections.unmodifiableList(new ArrayList<>(motivos));
        this.vendedoresUsados = vendedoresUsados;
        this.productosUsados = productosUsados;
        this.maxVendedores = maxVendedores;
        this.maxProductos = maxProductos;
    }

    public String getPlanCodigo() {
        return planCodigo;
    }

    public boolean isContratable() {
        return contratable;
    }

    public List<String> getMotivos() {
        return motivos;
    }

    public int getVendedoresUsados() {
        return vendedoresUsados;
    }

    public int getProductosUsados() {
        return productosUsados;
    }

    public int getMaxVendedores() {
        return maxVendedores;
    }

    public int getMaxProductos() {
        return maxProductos;
    }

    public int getExcesoVendedores() {
        return Math.max(0, vendedoresUsados - maxVendedores);
    }

    public int getExcesoProductos() {
        return Math.max(0, productosUsados - maxProductos);
    }
}
