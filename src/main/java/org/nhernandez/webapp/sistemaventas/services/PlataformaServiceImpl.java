package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ClienteCuenta;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class PlataformaServiceImpl implements PlataformaService {

    private final org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository usuarioRepository;
    private final SuscripcionService suscripcionService;
    private final PagoSuscripcionExpiracionService expiracionService;

    public PlataformaServiceImpl(org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository usuarioRepository,
                                 SuscripcionService suscripcionService,
                                 PagoSuscripcionExpiracionService expiracionService) {
        this.usuarioRepository = usuarioRepository;
        this.suscripcionService = suscripcionService;
        this.expiracionService = expiracionService;
    }

    @Override
    public List<ClienteCuenta> listarClientes() {
        try {
            return usuarioRepository.listarCuentasCliente();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<ClienteCuenta> buscarCliente(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return listarClientes().stream()
                .filter(c -> username.trim().equalsIgnoreCase(c.getUsername()))
                .findFirst();
    }

    @Override
    public List<PagoSuscripcion> pagosDelCliente(String username) {
        return suscripcionService.pagosDelUsuario(username);
    }

    @Override
    public List<PagoSuscripcion> pagosPendientesGlobales() {
        return suscripcionService.pagosPendientes();
    }

    @Override
    public List<PagoSuscripcion> pagosExpiradosGlobales() {
        return suscripcionService.pagosExpirados();
    }

    @Override
    public void confirmarPago(Long pagoId) {
        suscripcionService.confirmarPagoPlataforma(pagoId);
    }

    @Override
    public void expirarPago(Long pagoId) {
        suscripcionService.expirarPagoPlataforma(pagoId);
    }

    @Override
    public int expirarPagosVencidos() {
        try {
            return expiracionService.expirarAhora();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e);
        }
    }

    @Override
    public void extenderMeses(String usernameCliente, int meses) {
        suscripcionService.extenderSuscripcionMeses(usernameCliente, meses);
    }

    @Override
    public void suspenderCuenta(String username) {
        suscripcionService.suspenderCuenta(username);
    }

    @Override
    public void reactivarCuenta(String username) {
        suscripcionService.reactivarCuenta(username);
    }

    @Override
    public void cambiarPlan(String username, String planCodigo) {
        suscripcionService.cambiarPlanPlataforma(username, planCodigo);
    }

    @Override
    public List<PlanSuscripcion> planesDisponibles() {
        return suscripcionService.planesDisponibles();
    }
}
