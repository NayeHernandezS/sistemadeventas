package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.SuscripcionCorreoTipo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Asuntos y cuerpos de correos de retencion de suscripcion.
 */
public final class SuscripcionVencimientoCorreoPlantilla {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private SuscripcionVencimientoCorreoPlantilla() {
    }

    public record Correo(String asunto, String cuerpo) {
    }

    public static Correo construir(String appNombre, String baseUrl, String username,
                                   LocalDateTime fechaFin, boolean enPrueba, String tipoCodigo) {
        String fechaTexto = fechaFin.format(FORMATO);
        String renovar = baseUrl + "/suscripcion";
        String tipoCuenta = enPrueba ? "periodo de prueba" : "suscripcion";

        if (SuscripcionCorreoTipo.esVencido(tipoCodigo)) {
            String asunto = appNombre + " - Tu " + tipoCuenta + " ha vencido";
            String cuerpo = """
                    Hola %s,

                    Tu %s de %s vencio el %s. El acceso al sistema esta limitado hasta que renueves.

                    Renueva aqui: %s

                    Si tienes dudas, usa el modulo de Soporte en la aplicacion.
                    """.formatted(username, tipoCuenta, appNombre, fechaTexto, renovar);
            return new Correo(asunto, cuerpo);
        }

        int dias = parsearDiasAviso(tipoCodigo);
        String asunto = dias == 0
                ? appNombre + " - Tu " + tipoCuenta + " vence hoy"
                : appNombre + " - Tu " + tipoCuenta + " vence en " + dias + " dia(s)";
        String cuerpo = """
                Hola %s,

                Tu %s de %s vence el %s.

                Renueva en: %s

                Puedes pagar con Mercado Pago o solicitar pago manual desde esa pagina.
                Si ya renovaste, ignora este mensaje.
                """.formatted(username, tipoCuenta, appNombre, fechaTexto, renovar);
        return new Correo(asunto, cuerpo);
    }

    static int parsearDiasAviso(String tipoCodigo) {
        if (tipoCodigo == null || !tipoCodigo.startsWith("AVISO_")) {
            return 0;
        }
        try {
            return Integer.parseInt(tipoCodigo.substring("AVISO_".length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
