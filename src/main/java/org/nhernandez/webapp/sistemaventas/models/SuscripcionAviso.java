package org.nhernandez.webapp.sistemaventas.models;

import java.time.LocalDateTime;

/**
 * Aviso in-app sobre proximidad de vencimiento de suscripcion.
 */
public class SuscripcionAviso {

    private final int diasRestantes;
    private final LocalDateTime fechaFin;
    private final String nivel;
    private final String mensaje;

    public SuscripcionAviso(int diasRestantes, LocalDateTime fechaFin, String nivel, String mensaje) {
        this.diasRestantes = diasRestantes;
        this.fechaFin = fechaFin;
        this.nivel = nivel;
        this.mensaje = mensaje;
    }

    public int getDiasRestantes() {
        return diasRestantes;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public String getNivel() {
        return nivel;
    }

    public String getMensaje() {
        return mensaje;
    }
}
