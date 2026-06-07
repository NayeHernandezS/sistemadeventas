package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.CitaServicio;
import org.nhernandez.webapp.sistemaventas.models.EstadoCita;
import org.nhernandez.webapp.sistemaventas.models.Producto;
import org.nhernandez.webapp.sistemaventas.models.TipoItem;
import org.nhernandez.webapp.sistemaventas.repositories.CitaServicioRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServicioServiceImplTest {

    @Mock
    private CitaServicioRepository citaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CatalogoPlantillaService catalogoPlantillaService;

    private CitaServicioServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CitaServicioServiceImpl(
                citaRepository, productoRepository, clienteService, usuarioService, catalogoPlantillaService);
    }

    @Test
    void validar_rechazaSinServicioNiFecha() {
        CitaServicio cita = new CitaServicio();
        cita.setTenantOwner("tenant1");
        cita.setDuracionMinutos(0);

        Map<String, String> errores = service.validar(cita, false);

        assertTrue(errores.containsKey("productoId"));
        assertTrue(errores.containsKey("fechaHora"));
        assertTrue(errores.containsKey("duracionMinutos"));
    }

    @Test
    void validar_rechazaProductoQueNoEsServicio() throws SQLException {
        CitaServicio cita = citaValidaBase();
        Producto producto = new Producto();
        producto.setTipoItem(TipoItem.PRODUCTO);
        when(productoRepository.porIdPorOwner(10L, "tenant1")).thenReturn(producto);

        Map<String, String> errores = service.validar(cita, false);

        assertEquals("Solo puedes agendar servicios del catalogo", errores.get("productoId"));
    }

    @Test
    void validar_aceptaServicioValido() throws SQLException {
        CitaServicio cita = citaValidaBase();
        Producto producto = new Producto();
        producto.setTipoItem(TipoItem.SERVICIO);
        when(productoRepository.porIdPorOwner(10L, "tenant1")).thenReturn(producto);

        Map<String, String> errores = service.validar(cita, false);

        assertTrue(errores.isEmpty());
    }

    @Test
    void guardar_insertaNuevaCita() throws SQLException {
        CitaServicio cita = citaValidaBase();
        Producto producto = new Producto();
        producto.setTipoItem(TipoItem.SERVICIO);
        when(productoRepository.porIdPorOwner(10L, "tenant1")).thenReturn(producto);
        doAnswer(inv -> {
            cita.setId(1L);
            return null;
        }).when(citaRepository).guardar(cita);
        when(citaRepository.porIdPorTenant(1L, "tenant1")).thenReturn(Optional.of(cita));

        CitaServicio guardada = service.guardar("tenant1", "vendedor1", cita);

        verify(citaRepository).guardar(cita);
        assertEquals(EstadoCita.PROGRAMADA, guardada.getEstado());
        assertTrue(guardada.isEditable());
    }

    @Test
    void cancelar_lanzaSiNoExiste() throws SQLException {
        when(citaRepository.porIdPorTenant(99L, "tenant1")).thenReturn(Optional.empty());

        assertThrows(ServiceJdbcException.class, () -> service.cancelar("tenant1", 99L));
    }

    private static CitaServicio citaValidaBase() {
        CitaServicio cita = new CitaServicio();
        cita.setTenantOwner("tenant1");
        cita.setProductoId(10L);
        cita.setFechaHora(LocalDateTime.of(2026, 6, 3, 10, 0));
        cita.setDuracionMinutos(30);
        return cita;
    }
}
