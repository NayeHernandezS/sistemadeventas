package org.nhernandez.webapp.sistemaventas.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
            case "servicios_profesionales" -> List.of("Consultoria", "Tramites", "Capacitacion", "Asesoria", "General");
            default -> List.of("General");
        };
    }

    /** Categorias sugeridas al registrar servicios (complementan las de producto). */
    public static List<String> categoriasServicioParaTipoNegocio(String tipoNegocio) {
        String tipo = tipoNegocio == null ? "" : tipoNegocio.trim().toLowerCase(Locale.ROOT);
        return switch (tipo) {
            case "belleza" -> List.of("Unas", "Estetica", "Spa", "Paquetes");
            case "ferreteria" -> List.of("Instalaciones", "Reparaciones", "Mantenimiento", "Mano de obra", "Cotizaciones");
            case "tecnologia" -> List.of("Reparaciones", "Mantenimiento", "Instalaciones", "Soporte");
            case "abarrotes" -> List.of("Entregas", "Mayoreo", "Servicios varios");
            case "ropa" -> List.of("Arreglos", "Asesoria");
            case "papeleria" -> List.of("Impresion", "Encuadernacion", "Diseno");
            case "farmacia" -> List.of("Consulta", "Entregas");
            case "restaurante" -> List.of("Domicilio", "Eventos", "Reservaciones", "Servicios varios");
            case "regalo" -> List.of("Arreglos", "Entregas");
            case "servicios_profesionales" -> List.of("Legal", "Contabilidad", "Consultoria", "Tramites", "Capacitacion");
            default -> List.of("Servicios", "Consultoria", "Instalaciones", "Mantenimiento", "Mano de obra");
        };
    }

    /** Producto + servicio, sin duplicar nombres. */
    public static List<String> todasCategoriasParaTipoNegocio(String tipoNegocio) {
        LinkedHashSet<String> unicas = new LinkedHashSet<>();
        unicas.addAll(paraTipoNegocio(tipoNegocio));
        unicas.addAll(categoriasServicioParaTipoNegocio(tipoNegocio));
        return new ArrayList<>(unicas);
    }
}
