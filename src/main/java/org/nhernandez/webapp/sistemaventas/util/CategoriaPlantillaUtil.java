package org.nhernandez.webapp.sistemaventas.util;

import java.util.List;
import java.util.Locale;

public final class CategoriaPlantillaUtil {

    private CategoriaPlantillaUtil() {
    }

    public static List<String> paraTipoNegocio(String tipoNegocio) {
        String tipo = tipoNegocio == null ? "" : tipoNegocio.trim().toLowerCase(Locale.ROOT);
        return switch (tipo) {
            case "ferreteria" -> List.of("Herramientas", "Tornilleria", "Pinturas", "Electricidad", "Plomeria");
            case "abarrotes" -> List.of("Abarrotes", "Bebidas", "Lacteos", "Limpieza", "Higiene");
            case "ropa" -> List.of("Dama", "Caballero", "Ninos", "Calzado", "Accesorios");
            case "tecnologia" -> List.of("Computo", "Celulares", "Audio", "Accesorios", "Redes");
            case "papeleria" -> List.of("Cuadernos", "Escritura", "Oficina", "Arte", "Escolar");
            case "farmacia" -> List.of("Medicamentos", "Higiene", "Bebe", "Vitaminas", "Primeros auxilios");
            case "restaurante" -> List.of("Comida", "Bebidas", "Postres", "Insumos", "Desechables");
            case "belleza" -> List.of("Cabello", "Maquillaje", "Cuidado personal", "Accesorios", "Promociones");
            case "regalo" -> List.of("Detalles", "Temporada", "Envolturas", "Peluches", "Varios");
            default -> List.of("General");
        };
    }
}
