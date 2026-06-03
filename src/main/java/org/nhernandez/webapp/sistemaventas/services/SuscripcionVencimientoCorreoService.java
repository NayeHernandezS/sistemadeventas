package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.models.SuscripcionCorreoTipo;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.SuscripcionCorreoEnviadoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.SuscripcionRepository;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Correos transaccionales de retencion: avisos antes del vencimiento y aviso de plan vencido.
 * Requiere SMTP configurado ({@link RecuperacionCorreoService#correoHabilitado()}).
 */
@Service
public class SuscripcionVencimientoCorreoService {

    private static final Logger log = LoggerFactory.getLogger(SuscripcionVencimientoCorreoService.class);
    private static final int[] DIAS_AVISO_CANDIDATOS = {7, 3, 1, 0};

    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioReposository usuarioRepository;
    private final RecuperacionCorreoService correoService;
    private final SuscripcionCorreoEnviadoRepository correoEnviadoRepository;

    @Value("${app.nombre:FUSION DIGITAL}")
    private String appNombre;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    @Value("${suscripcion.aviso.dias:7}")
    private int diasAvisoUmbral;

    public SuscripcionVencimientoCorreoService(SuscripcionRepository suscripcionRepository,
                                               UsuarioReposository usuarioRepository,
                                               RecuperacionCorreoService correoService,
                                               SuscripcionCorreoEnviadoRepository correoEnviadoRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.correoService = correoService;
        this.correoEnviadoRepository = correoEnviadoRepository;
    }

    public boolean correoHabilitado() {
        return correoService.correoHabilitado();
    }

    @Scheduled(cron = "${suscripcion.aviso.cron:0 0 8 * * *}", zone = "America/Mexico_City")
    public void enviarAvisosProgramados() {
        int enviados = procesarAvisos();
        if (enviados > 0) {
            log.info("Correos de suscripcion enviados: {}", enviados);
        }
    }

    /**
     * Ejecucion manual desde panel plataforma (pruebas o recuperacion tras caida SMTP).
     */
    public int procesarAvisosManual() {
        return procesarAvisos();
    }

    int procesarAvisos() {
        if (!correoService.correoHabilitado()) {
            log.debug("SMTP no configurado; omitiendo avisos de suscripcion por correo");
            return 0;
        }
        int total = 0;
        for (int dias : diasParaEnviar()) {
            String tipoCodigo = SuscripcionCorreoTipo.codigoAviso(dias);
            try {
                List<Suscripcion> porVencer = suscripcionRepository.listarVigentesQueVencenEn(dias);
                for (Suscripcion s : porVencer) {
                    if (enviarSiCorresponde(s, tipoCodigo)) {
                        total++;
                    }
                }
            } catch (SQLException e) {
                log.error("Error listando suscripciones que vencen en {} dias", dias, e);
            }
        }
        try {
            List<Suscripcion> vencidasAyer = suscripcionRepository.listarConFechaFinEnDiaPasado(1);
            for (Suscripcion s : vencidasAyer) {
                if (enviarSiCorresponde(s, SuscripcionCorreoTipo.VENCIDO)) {
                    total++;
                }
            }
        } catch (SQLException e) {
            log.error("Error listando suscripciones vencidas ayer", e);
        }
        return total;
    }

    private boolean enviarSiCorresponde(Suscripcion suscripcion, String tipoCodigo) {
        if (suscripcion.getFechaFin() == null || suscripcion.getUsername() == null) {
            return false;
        }
        LocalDate ref = suscripcion.getFechaFin().toLocalDate();
        try {
            if (correoEnviadoRepository.yaEnviado(suscripcion.getUsername(), tipoCodigo, ref)) {
                return false;
            }
            Usuario admin = usuarioRepository.porUsername(suscripcion.getUsername());
            if (admin == null || admin.getEmail() == null || admin.getEmail().isBlank()) {
                return false;
            }
            var plantilla = SuscripcionVencimientoCorreoPlantilla.construir(
                    appNombre, baseUrlRenovacion(), admin.getUsername(),
                    suscripcion.getFechaFin(), suscripcion.isEnPeriodoPrueba(), tipoCodigo);
            correoService.enviarTexto(admin.getEmail(), plantilla.asunto(), plantilla.cuerpo());
            correoEnviadoRepository.registrar(suscripcion.getUsername(), tipoCodigo, ref);
            log.info("Correo {} enviado a {} ({})", tipoCodigo, admin.getEmail(), ref);
            return true;
        } catch (SQLException e) {
            log.error("Error enviando correo {} a tenant {}", tipoCodigo, suscripcion.getUsername(), e);
            return false;
        }
    }

    List<Integer> diasParaEnviar() {
        int umbral = diasAvisoUmbral > 0 ? diasAvisoUmbral : 7;
        List<Integer> dias = new ArrayList<>();
        for (int candidato : DIAS_AVISO_CANDIDATOS) {
            if (candidato <= umbral) {
                dias.add(candidato);
            }
        }
        if (umbral > 7 && !dias.contains(umbral)) {
            dias.add(0, umbral);
        }
        return dias;
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
