package org.nhernandez.webapp.sistemaventas.services;

import org.nhernandez.webapp.sistemaventas.models.ClienteCuenta;
import org.nhernandez.webapp.sistemaventas.models.PagoSuscripcion;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class PlataformaServiceImpl implements PlataformaService {

    private final UsuarioReposository usuarioRepository;
    private final SuscripcionService suscripcionService;

    public PlataformaServiceImpl(UsuarioReposository usuarioRepository,
                                 SuscripcionService suscripcionService) {
        this.usuarioRepository = usuarioRepository;
        this.suscripcionService = suscripcionService;
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
    public List<PagoSuscripcion> pagosPendientesGlobales() {
        return suscripcionService.pagosPendientes();
    }

    @Override
    public void confirmarPago(Long pagoId) {
        suscripcionService.confirmarPagoPlataforma(pagoId);
    }

    @Override
    public void extenderMeses(String usernameCliente, int meses) {
        suscripcionService.extenderSuscripcionMeses(usernameCliente, meses);
    }
}
