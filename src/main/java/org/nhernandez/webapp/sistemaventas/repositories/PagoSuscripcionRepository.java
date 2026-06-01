package org.nhernandez.webapp.sistemaventas.repositories;
import org.springframework.stereotype.Repository;

import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;

import java.sql.SQLException;
import java.util.List;

public interface PagoSuscripcionRepository {

    void guardar(PagoSuscripcion pago) throws SQLException;

    void actualizarReferenciaMercadoPago(Long id, String preferenceId) throws SQLException;

    PagoSuscripcion porId(Long id) throws SQLException;

    List<PagoSuscripcion> listarPendientes() throws SQLException;

    List<PagoSuscripcion> listarPendientesPorUsername(String username) throws SQLException;

    List<PagoSuscripcion> listarPorUsername(String username) throws SQLException;

    List<PagoSuscripcion> listarPorEstado(String estado) throws SQLException;

    void confirmar(Long id) throws SQLException;

    void confirmarMercadoPago(Long id, String mpPaymentId) throws SQLException;

    /**
     * Marca un pago PENDIENTE como EXPIRADO (expiracion manual por plataforma).
     * @return filas actualizadas (0 si no era pendiente)
     */
    int expirarPorId(Long id) throws SQLException;

    /**
     * Marca como EXPIRADO los pagos PENDIENTE cuya fecha_solicitud es anterior al limite segun canal.
     * @return cantidad de filas actualizadas
     */
    int expirarPendientesAnterioresA(java.time.LocalDateTime limiteManual,
                                     java.time.LocalDateTime limiteMercadoPago) throws SQLException;
}
