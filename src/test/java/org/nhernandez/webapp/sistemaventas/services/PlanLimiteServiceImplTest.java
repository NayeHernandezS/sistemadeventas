package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.models.Suscripcion;
import org.nhernandez.webapp.sistemaventas.models.Usuario;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;
import org.nhernandez.webapp.sistemaventas.repositories.UsuarioReposository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanLimiteServiceImplTest {

    @Mock
    private SuscripcionService suscripcionService;

    @Mock
    private UsuarioReposository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private PlanLimiteServiceImpl planLimiteService;

    private Suscripcion suscripcionNegocio;

    @BeforeEach
    void setUp() {
        suscripcionNegocio = new Suscripcion();
        suscripcionNegocio.setUsername("tienda1");
        suscripcionNegocio.setPlanCodigo("NEGOCIO");
        suscripcionNegocio.setFechaFin(LocalDateTime.now().plusMonths(1));
    }

    @Test
    void planActivo_usaPlanDeLaSuscripcion() {
        when(suscripcionService.consultar("tienda1")).thenReturn(Optional.of(suscripcionNegocio));

        PlanSuscripcion plan = planLimiteService.planActivo("tienda1");

        assertEquals(PlanSuscripcion.NEGOCIO, plan);
        assertEquals(5, plan.getMaxVendedores());
    }

    @Test
    void validarNuevoVendedor_rechazaCuandoSeAlcanzaElLimite() throws SQLException {
        when(suscripcionService.consultar("tienda1")).thenReturn(Optional.of(suscripcionNegocio));
        when(usuarioRepository.listarPorAdminOwner("tienda1"))
                .thenReturn(vendedores(PlanSuscripcion.NEGOCIO.getMaxVendedores()));

        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> planLimiteService.validarNuevoVendedor("tienda1"));

        assertTrue(ex.getMessage().contains("Plan Negocio"));
        assertTrue(ex.getMessage().contains("5 vendedores"));
    }

    @Test
    void validarNuevoProducto_rechazaCuandoSeAlcanzaElLimite() throws SQLException {
        when(suscripcionService.consultar("tienda1")).thenReturn(Optional.of(suscripcionNegocio));
        when(productoRepository.contarPorOwner("tienda1"))
                .thenReturn(PlanSuscripcion.NEGOCIO.getMaxProductos());

        ServiceJdbcException ex = assertThrows(ServiceJdbcException.class,
                () -> planLimiteService.validarNuevoProducto("tienda1"));

        assertTrue(ex.getMessage().contains("500 productos"));
    }

    @Test
    void validarNuevoVendedor_permiteCuandoHayCupo() throws SQLException {
        when(suscripcionService.consultar("tienda1")).thenReturn(Optional.of(suscripcionNegocio));
        when(usuarioRepository.listarPorAdminOwner("tienda1"))
                .thenReturn(vendedores(PlanSuscripcion.NEGOCIO.getMaxVendedores() - 1));

        planLimiteService.validarNuevoVendedor("tienda1");
    }

    private static List<Usuario> vendedores(int cantidad) {
        return IntStream.range(0, cantidad)
                .mapToObj(i -> {
                    Usuario u = new Usuario();
                    u.setUsername("vendedor" + i);
                    return u;
                })
                .toList();
    }
}
