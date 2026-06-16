package org.nhernandez.webapp.sistemaventas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RecuperacionCorreoService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacionCorreoService.class);

    private final Optional<JavaMailSender> mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${recuperacion.mail.from:noreply@fusiondigital.com}")
    private String mailFrom;

    @Value("${app.nombre:FUSION DIGITAL}")
    private String appNombre;

    public RecuperacionCorreoService(Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * @return enlace visible en pantalla cuando SMTP no esta configurado (modo demo/desarrollo)
     */
    public Optional<String> enviarEnlaceRecuperacion(String destino, String enlace) {
        if (!correoHabilitado()) {
            log.info("Recuperacion de contraseña (modo demo) para {}: {}", destino, enlace);
            return Optional.of(enlace);
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(mailFrom);
        mensaje.setTo(destino);
        mensaje.setSubject("Restablecer contraseña - " + appNombre);
        mensaje.setText("""
                Recibimos una solicitud para restablecer tu contraseña.

                Abre este enlace (valido por tiempo limitado):
                %s

                Si no solicitaste este cambio, ignora este correo.
                """.formatted(enlace));
        mailSender.ifPresent(sender -> sender.send(mensaje));
        return Optional.empty();
    }

    /**
     * Envio de prueba desde panel plataforma. Devuelve error descriptivo si falla SMTP.
     */
    public Optional<ResultadoEnvioCorreo> enviarPrueba(String destino) {
        if (!correoHabilitado()) {
            return Optional.of(ResultadoEnvioCorreo.error(
                    "SMTP no configurado. Completa SMTP_HOST, credenciales y MAIL_FROM."));
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(mailFrom);
        mensaje.setTo(destino);
        mensaje.setSubject("Prueba de correo - " + appNombre);
        mensaje.setText("""
                Este es un correo de prueba de %s.

                Si lo recibes, SMTP esta configurado correctamente.
                Los avisos de suscripcion y la recuperacion de contraseña usaran el mismo servidor.
                """.formatted(appNombre));
        try {
            mailSender.ifPresent(sender -> sender.send(mensaje));
            log.info("Correo de prueba enviado a {}", destino);
            return Optional.of(ResultadoEnvioCorreo.ok());
        } catch (MailException e) {
            log.error("Fallo envio de correo de prueba a {}: {}", destino, e.getMessage());
            return Optional.of(ResultadoEnvioCorreo.error(
                    "No se pudo enviar: " + mensajeErrorAmigable(e)));
        }
    }

    public boolean correoHabilitado() {
        return mailHost != null && !mailHost.isBlank() && mailSender.isPresent();
    }

    public void enviarTexto(String destino, String asunto, String cuerpo) {
        if (!correoHabilitado()) {
            log.info("Correo (modo demo) para {} — {}: {}", destino, asunto, cuerpo.lines().findFirst().orElse(""));
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(mailFrom);
        mensaje.setTo(destino);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        try {
            mailSender.ifPresent(sender -> sender.send(mensaje));
        } catch (MailException e) {
            log.error("Fallo envio de correo a {} ({}): {}", destino, asunto, e.getMessage());
        }
    }

    private static String mensajeErrorAmigable(MailException e) {
        String msg = e.getMessage();
        if (msg == null) {
            return "error de conexion SMTP";
        }
        if (msg.contains("Authentication failed") || msg.contains("535")) {
            return "autenticacion rechazada (revisa SMTP_USER y SMTP_PASSWORD; en Gmail usa contraseña de aplicacion)";
        }
        if (msg.contains("Could not connect")) {
            return "no se pudo conectar al servidor (revisa SMTP_HOST y SMTP_PORT)";
        }
        if (msg.contains("timed out") || msg.contains("Timed out")) {
            return "tiempo de espera agotado al conectar con SMTP (revisa credenciales y puerto 587)";
        }
        return msg.length() > 200 ? msg.substring(0, 200) + "…" : msg;
    }
}
