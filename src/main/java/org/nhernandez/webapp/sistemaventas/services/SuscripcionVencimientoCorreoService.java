package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.SuscripcionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Envia correos de aviso de vencimiento de suscripcion (requiere SMTP configurado).
 */
@Service
public class SuscripcionVencimientoCorreoService {

    private static final Logger log = LoggerFactory.getLogger(SuscripcionVencimientoCorreoService.class);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int[] DIAS_AVISO = {7, 3, 1, 0};

    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioReposository usuarioRepository;
    private final RecuperacionCorreoService correoService;

    @Value("${app.nombre:Sistema de Ventas}")
    private String appNombre;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    public SuscripcionVencimientoCorreoService(SuscripcionRepository suscripcionRepository,
                                               UsuarioReposository usuarioRepository,
                                               RecuperacionCorreoService correoService) {
        this.suscripcionRepository = suscripcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.correoService = correoService;
    }

    @Scheduled(cron = "${suscripcion.aviso.cron:0 0 8 * * *}", zone = "America/Mexico_City")
    public void enviarAvisosProgramados() {
        if (!correoService.correoHabilitado()) {
            return;
        }
        for (int dias : DIAS_AVISO) {
            try {
                List<Suscripcion> porVencer = suscripcionRepository.listarVigentesQueVencenEn(dias);
                for (Suscripcion s : porVencer) {
                    enviarAviso(s, dias);
                }
            } catch (SQLException e) {
                log.error("Error listando suscripciones que vencen en {} dias", dias, e);
            }
        }
    }

    private void enviarAviso(Suscripcion suscripcion, int dias) {
        try {
            Usuario admin = usuarioRepository.porUsername(suscripcion.getUsername());
            if (admin == null || admin.getEmail() == null || admin.getEmail().isBlank()) {
                return;
            }
            String asunto = dias == 0
                    ? appNombre + " - Tu suscripcion vence hoy"
                    : appNombre + " - Tu suscripcion vence en " + dias + " dia(s)";
            String cuerpo = """
                    Hola %s,

                    Tu suscripcion de %s vence el %s.

                    Renueva en: %s/suscripcion

                    Si ya renovaste, ignora este mensaje.
                    """.formatted(
                    admin.getUsername(),
                    appNombre,
                    suscripcion.getFechaFin().format(FORMATO),
                    baseUrlRenovacion());
            correoService.enviarTexto(admin.getEmail(), asunto, cuerpo);
            log.info("Aviso de vencimiento enviado a {} ({} dias)", admin.getEmail(), dias);
        } catch (SQLException e) {
            log.error("Error enviando aviso a tenant {}", suscripcion.getUsername(), e);
        }
    }

    private String baseUrlRenovacion() {
        if (appBaseUrl == null || appBaseUrl.isBlank()) {
            return "(configura APP_BASE_URL)";
        }
        String base = appBaseUrl.trim();
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }
}
