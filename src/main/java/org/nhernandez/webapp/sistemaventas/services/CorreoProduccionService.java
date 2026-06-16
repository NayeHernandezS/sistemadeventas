package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.CorreoEstado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class CorreoProduccionService {

    private static final ExecutorService CORREO_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final RecuperacionCorreoService correoService;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${spring.mail.username:}")
    private String smtpUser;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${recuperacion.mail.from:}")
    private String mailFrom;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    @Value("${smtp.envio.timeout-segundos:12}")
    private int envioTimeoutSegundos;

    public CorreoProduccionService(RecuperacionCorreoService correoService) {
        this.correoService = correoService;
    }

    public CorreoEstado evaluar() {
        CorreoEstado estado = new CorreoEstado();
        boolean hostOk = smtpHost != null && !smtpHost.isBlank();
        estado.setSmtpConfigurado(hostOk && correoService.correoHabilitado());
        estado.setSmtpHost(hostOk ? smtpHost.trim() : null);

        boolean fromOk = mailFrom != null && !mailFrom.isBlank();
        estado.setRemitenteConfigurado(fromOk);
        estado.setMailFrom(fromOk ? mailFrom.trim() : null);

        String base = baseUrlEfectiva();
        estado.setAppBaseUrl(base);
        estado.setBaseUrlHttps(base != null);

        List<String> advertencias = new ArrayList<>();
        if (!hostOk) {
            advertencias.add("Configura SMTP_HOST en Railway (o .env local). Sin SMTP, los avisos solo aparecen en la app.");
        } else {
            if (smtpUser == null || smtpUser.isBlank()) {
                advertencias.add("SMTP_USER vacio: la mayoria de proveedores lo requieren (Gmail, SendGrid, SES).");
            }
            if (smtpPassword == null || smtpPassword.isBlank()) {
                advertencias.add("SMTP_PASSWORD vacio.");
            }
            if (!correoService.correoHabilitado()) {
                advertencias.add("Spring no pudo inicializar el cliente SMTP. Revisa host, puerto y credenciales.");
            }
        }
        if (!fromOk) {
            advertencias.add("Configura MAIL_FROM con un remitente autorizado en tu proveedor SMTP.");
        }
        if (base == null) {
            advertencias.add("APP_BASE_URL debe ser HTTPS publico para enlaces en correos de suscripcion y recuperacion.");
        }
        estado.setAdvertencias(advertencias);
        return estado;
    }

    public String enviarCorreoPrueba(String destino) {
        if (destino == null || destino.isBlank()) {
            return "Indica un correo de destino.";
        }
        String email = destino.trim();
        if (!email.contains("@") || email.length() > 150) {
            return "Correo de destino no valido.";
        }
        Future<Optional<ResultadoEnvioCorreo>> futuro =
                CORREO_EXECUTOR.submit(() -> correoService.enviarPrueba(email));
        try {
            Optional<ResultadoEnvioCorreo> resultado = futuro.get(envioTimeoutSegundos, TimeUnit.SECONDS);
            return resultado
                    .filter(r -> !r.exito())
                    .map(ResultadoEnvioCorreo::mensaje)
                    .orElse("Correo de prueba enviado a " + email + ". Revisa bandeja y spam.");
        } catch (TimeoutException e) {
            futuro.cancel(true);
            return "Tiempo agotado (" + envioTimeoutSegundos + " s) al conectar con SMTP. "
                    + "En Gmail usa contraseña de aplicacion en SMTP_PASSWORD (no tu clave normal).";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            futuro.cancel(true);
            return "Envio interrumpido. Intenta de nuevo.";
        } catch (ExecutionException e) {
            return "Error al enviar: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }

    private String baseUrlEfectiva() {
        if (appBaseUrl == null || appBaseUrl.isBlank()) {
            return null;
        }
        String base = appBaseUrl.trim();
        if (!base.toLowerCase().startsWith("https://")) {
            return null;
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
