package org.nhernandez.webapp.sistemaventas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Comprueba configuracion minima de produccion (HTTPS, BD, SMTP, Mercado Pago).
 */
@Component
@Profile("prod")
public class DeployStartupChecker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DeployStartupChecker.class);

    @Value("${deploy.validar-al-inicio:true}")
    private boolean validarAlInicio;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${mercadopago.webhook-secret:}")
    private String mpWebhookSecret;

    @Value("${mercadopago.access-token:}")
    private String mpAccessToken;

    private final Environment environment;

    public DeployStartupChecker(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!validarAlInicio) {
            return;
        }
        log.info("Perfil prod activo — comprobando configuracion de despliegue…");

        if (dbPassword == null || dbPassword.isBlank()) {
            log.error("DB_PASSWORD vacia. La app no puede operar en produccion sin base de datos.");
        }

        if (appBaseUrl == null || appBaseUrl.isBlank()) {
            log.error("APP_BASE_URL no configurada. Obligatoria para Mercado Pago, recuperacion de contraseña y correos.");
        } else if (!appBaseUrl.trim().toLowerCase().startsWith("https://")) {
            log.warn("APP_BASE_URL no usa HTTPS ({}). Mercado Pago y cookies seguras requieren https:// en produccion.",
                    appBaseUrl);
        } else {
            log.info("APP_BASE_URL: {}", appBaseUrl.trim());
        }

        if (smtpHost == null || smtpHost.isBlank()) {
            log.warn("SMTP no configurado (SMTP_HOST). Recuperacion de contraseña y avisos de vencimiento quedan en modo demo.");
        } else {
            log.info("SMTP configurado: {}", smtpHost);
        }

        if (mpAccessToken != null && !mpAccessToken.isBlank()) {
            if (mpWebhookSecret == null || mpWebhookSecret.isBlank()) {
                log.warn("MERCADOPAGO_WEBHOOK_SECRET vacio. En produccion configura la firma de webhooks en el panel MP.");
            } else {
                log.info("Mercado Pago: token y webhook secret presentes.");
            }
        }

        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            log.info("Despliegue listo. Documentacion: deploy/DEPLOY.md");
        }
    }
}
