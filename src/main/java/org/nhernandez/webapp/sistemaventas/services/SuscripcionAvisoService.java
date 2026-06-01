package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.models.SuscripcionAviso;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class SuscripcionAvisoService {

    private final SuscripcionService suscripcionService;

    @Value("${suscripcion.aviso.dias:7}")
    private int diasAviso;

    public SuscripcionAvisoService(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    public Optional<SuscripcionAviso> evaluar(String tenantOwner) {
        return suscripcionService.consultar(tenantOwner)
                .filter(Suscripcion::estaVigente)
                .flatMap(this::construirAviso);
    }

    Optional<SuscripcionAviso> construirAviso(Suscripcion suscripcion) {
        if (suscripcion.getFechaFin() == null) {
            return Optional.empty();
        }
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), suscripcion.getFechaFin().toLocalDate());
        int umbral = diasAviso > 0 ? diasAviso : 7;
        if (dias < 0 || dias > umbral) {
            return Optional.empty();
        }
        String nivel = dias <= 1 ? "danger" : (dias <= 3 ? "warning" : "info");
        String mensaje = mensajePara(dias, suscripcion.isEnPeriodoPrueba());
        return Optional.of(new SuscripcionAviso((int) dias, suscripcion.getFechaFin(), nivel, mensaje));
    }

    private static String mensajePara(long dias, boolean enPrueba) {
        String tipo = enPrueba ? "periodo de prueba" : "suscripcion";
        if (dias == 0) {
            return "Tu " + tipo + " vence hoy. Renueva para no perder acceso al sistema.";
        }
        if (dias == 1) {
            return "Tu " + tipo + " vence manana. Renueva ahora para evitar interrupciones.";
        }
        return "Tu " + tipo + " vence en " + dias + " dias. Renueva desde Suscripcion.";
    }
}
