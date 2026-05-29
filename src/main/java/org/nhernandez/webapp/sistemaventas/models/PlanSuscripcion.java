package org.nhernandez.webapp.sistemaventas.models;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Planes comerciales del SaaS (precios MXN/mes, soporte incluido por la creadora).
 */
public enum PlanSuscripcion {

    EMPRENDEDOR(
            "EMPRENDEDOR",
            "Plan Emprendedor",
            new BigDecimal("149.00"),
            2,
            150,
            "Ideal para tiendas pequeñas que inician. Soporte por solicitud en la app."
    ),
    NEGOCIO(
            "NEGOCIO",
            "Plan Negocio",
            new BigDecimal("249.00"),
            5,
            500,
            "Para negocios con mas vendedores e inventario. Soporte prioritario."
    ),
    PRO(
            "PRO",
            "Plan Pro",
            new BigDecimal("399.00"),
            15,
            2000,
            "Maximo alcance: mas equipo y catalogo amplio. Soporte prioritario."
    );

    private final String codigo;
    private final String nombre;
    private final BigDecimal precioMensual;
    private final int maxVendedores;
    private final int maxProductos;
    private final String descripcion;

    PlanSuscripcion(String codigo, String nombre, BigDecimal precioMensual,
                    int maxVendedores, int maxProductos, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioMensual = precioMensual;
        this.maxVendedores = maxVendedores;
        this.maxProductos = maxProductos;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecioMensual() {
        return precioMensual;
    }

    public int getMaxVendedores() {
        return maxVendedores;
    }

    public int getMaxProductos() {
        return maxProductos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static List<PlanSuscripcion> todos() {
        return List.of(values());
    }

    public static Optional<PlanSuscripcion> porCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(p -> p.codigo.equals(normalizado))
                .findFirst();
    }

    public static PlanSuscripcion porCodigoODefault(String codigo) {
        return porCodigo(codigo).orElse(EMPRENDEDOR);
    }
}
