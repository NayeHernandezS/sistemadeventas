package org.nhernandez.webapp.sistemaventas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ServicioPlantillaUtil {

    private ServicioPlantillaUtil() {
    }

    public static List<SugerenciaServicio> sugerenciasParaRubro(String tipoNegocio) {
        String rubro = normalizar(tipoNegocio);
        return switch (rubro) {
            case "belleza" -> List.of(
                    s("Corte caballero", "Cabello"),
                    s("Corte dama", "Cabello"),
                    s("Tinte y color", "Cabello"),
                    s("Peinado para evento", "Cabello"),
                    s("Manicure", "Unas"),
                    s("Pedicure", "Unas"),
                    s("Maquillaje", "Estetica"),
                    s("Depilacion", "Estetica"),
                    s("Masaje relajante", "Spa"),
                    s("Paquete novia", "Paquetes")
            );
            case "ferreteria" -> List.of(
                    s("Instalacion a domicilio", "Instalaciones"),
                    s("Reparacion de herramienta", "Reparaciones"),
                    s("Mantenimiento preventivo", "Mantenimiento"),
                    s("Corte de tuberia", "Mano de obra"),
                    s("Soldadura", "Mano de obra"),
                    s("Cotizacion de obra", "Cotizaciones")
            );
            case "tecnologia" -> List.of(
                    s("Reparacion de computadora", "Reparaciones"),
                    s("Reparacion de celular", "Reparaciones"),
                    s("Mantenimiento de equipo", "Mantenimiento"),
                    s("Instalacion de software", "Instalaciones"),
                    s("Configuracion de red", "Instalaciones"),
                    s("Recuperacion de datos", "Soporte"),
                    s("Diagnostico sin compromiso", "Soporte")
            );
            case "abarrotes" -> List.of(
                    s("Entrega a domicilio", "Entregas"),
                    s("Arma de pedido mayoreo", "Mayoreo"),
                    s("Empaque de regalo", "Servicios varios")
            );
            case "ropa" -> List.of(
                    s("Arreglo de bastilla", "Arreglos"),
                    s("Ajuste de pretina", "Arreglos"),
                    s("Personal shopper", "Asesoria")
            );
            case "papeleria" -> List.of(
                    s("Impresion y copiado", "Impresion"),
                    s("Engargolado", "Encuadernacion"),
                    s("Diseno de invitacion", "Diseno")
            );
            case "farmacia" -> List.of(
                    s("Toma de presion", "Consulta"),
                    s("Aplicacion de inyeccion", "Consulta"),
                    s("Entrega de medicamento", "Entregas")
            );
            case "restaurante" -> List.of(
                    s("Servicio a domicilio", "Domicilio"),
                    s("Banquete / evento", "Eventos"),
                    s("Reservacion de mesa", "Reservaciones"),
                    s("Empaque para llevar", "Servicios varios")
            );
            case "regalo" -> List.of(
                    s("Arreglo de regalo", "Arreglos"),
                    s("Envoltura premium", "Arreglos"),
                    s("Entrega express", "Entregas")
            );
            case "servicios_profesionales" -> List.of(
                    s("Consultoria legal", "Legal"),
                    s("Elaboracion de contrato", "Legal"),
                    s("Representacion legal", "Legal"),
                    s("Consultoria contable", "Contabilidad"),
                    s("Declaracion de impuestos", "Contabilidad"),
                    s("Consultoria de negocios", "Consultoria"),
                    s("Capacitacion empresarial", "Capacitacion"),
                    s("Tramite notarial", "Tramites")
            );
            default -> List.of(
                    s("Servicio general", "Servicios"),
                    s("Consultoria", "Consultoria"),
                    s("Instalacion", "Instalaciones"),
                    s("Mantenimiento", "Mantenimiento"),
                    s("Mano de obra", "Mano de obra")
            );
        };
    }

    public static List<String> nombresParaRubro(String tipoNegocio) {
        return sugerenciasParaRubro(tipoNegocio).stream()
                .map(SugerenciaServicio::nombre)
                .toList();
    }

    private static SugerenciaServicio s(String nombre, String categoria) {
        return new SugerenciaServicio(nombre, categoria);
    }

    private static String normalizar(String tipoNegocio) {
        if (tipoNegocio == null || tipoNegocio.isBlank()) {
            return "otro";
        }
        return tipoNegocio.trim().toLowerCase(Locale.ROOT);
    }
}
