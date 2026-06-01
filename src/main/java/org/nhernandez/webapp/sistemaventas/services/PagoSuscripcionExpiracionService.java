package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.repositories.PagoSuscripcionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Expira solicitudes de pago PENDIENTE que superaron el plazo configurado (SPEI/OXXO/manual).
 */
@Service
public class PagoSuscripcionExpiracionService {

    private static final Logger log = LoggerFactory.getLogger(PagoSuscripcionExpiracionService.class);

    private final PagoSuscripcionRepository pagoRepository;

    @Value("${suscripcion.pago.pendiente.dias.manual:30}")
    private int diasManual;

    @Value("${suscripcion.pago.pendiente.dias.mercadopago:15}")
    private int diasMercadoPago;

    public PagoSuscripcionExpiracionService(PagoSuscripcionRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    public int getDiasManual() {
        return diasManual > 0 ? diasManual : 30;
    }

    public int getDiasMercadoPago() {
        return diasMercadoPago > 0 ? diasMercadoPago : 15;
    }

    @Scheduled(cron = "${suscripcion.pago.expiracion.cron:0 30 3 * * *}", zone = "America/Mexico_City")
    public void expirarPagosPendientes() {
        int dm = diasManual > 0 ? diasManual : 30;
        int dmp = diasMercadoPago > 0 ? diasMercadoPago : 15;
        LocalDateTime limiteManual = LocalDateTime.now().minusDays(dm);
        LocalDateTime limiteMp = LocalDateTime.now().minusDays(dmp);
        try {
            int expirados = pagoRepository.expirarPendientesAnterioresA(limiteManual, limiteMp);
            if (expirados > 0) {
                log.info("Pagos PENDIENTE expirados: {} (manual >{} dias, MP >{} dias)", expirados, dm, dmp);
            }
        } catch (SQLException e) {
            log.error("Error expirando pagos pendientes", e);
        }
    }

    public int expirarAhora() throws SQLException {
        int dm = diasManual > 0 ? diasManual : 30;
        int dmp = diasMercadoPago > 0 ? diasMercadoPago : 15;
        return pagoRepository.expirarPendientesAnterioresA(
                LocalDateTime.now().minusDays(dm),
                LocalDateTime.now().minusDays(dmp));
    }
}
