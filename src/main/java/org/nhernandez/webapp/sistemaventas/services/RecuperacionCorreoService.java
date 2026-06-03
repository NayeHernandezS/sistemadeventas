package org.nhernandez.webapp.sistemaventas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
        mailSender.ifPresent(sender -> sender.send(mensaje));
    }
}
