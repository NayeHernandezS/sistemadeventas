package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.Cliente;
import org.nhernandez.webapp.sistemaventas.repositories.ClienteRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteServiceImpl service;

    @Test
    void guardar_persisteClienteNormalizado() throws SQLException {
        Cliente entrada = new Cliente();
        entrada.setNombre("  Juan Perez  ");
        entrada.setRfc(" xaxx010101000 ");
        entrada.setUsoCfdi("g03");

        service.guardar("tienda1", entrada);

        verify(repository).guardar(any(Cliente.class));
    }

    @Test
    void guardar_rechazaNombreVacio() {
        Cliente entrada = new Cliente();
        entrada.setNombre("   ");

        assertThrows(ServiceJdbcException.class, () -> service.guardar("tienda1", entrada));
    }

    @Test
    void guardar_rechazaRfcInvalido() {
        Cliente entrada = new Cliente();
        entrada.setNombre("Cliente");
        entrada.setRfc("ABC");

        assertThrows(ServiceJdbcException.class, () -> service.guardar("tienda1", entrada));
    }

    @Test
    void guardar_rechazaCodigoPostalInvalido() {
        Cliente entrada = new Cliente();
        entrada.setNombre("Cliente");
        entrada.setCodigoPostal("123");

        assertThrows(ServiceJdbcException.class, () -> service.guardar("tienda1", entrada));
    }

    @Test
    void listarActivos_devuelveDelTenant() throws SQLException {
        Cliente c = new Cliente();
        c.setNombre("Ana");
        when(repository.listarActivosPorTenant("tienda1")).thenReturn(List.of(c));

        assertEquals(1, service.listarActivos("tienda1").size());
    }

    @Test
    void porId_consultaRepositorio() throws SQLException {
        Cliente c = new Cliente();
        c.setId(1L);
        when(repository.porIdPorTenant(1L, "tienda1")).thenReturn(c);

        Optional<Cliente> result = service.porId("tienda1", 1L);

        assertEquals(1L, result.orElseThrow().getId());
    }

    @Test
    void desactivar_llamaRepositorio() throws SQLException {
        service.desactivar("tienda1", 5L);

        verify(repository).desactivarPorTenant(5L, "tienda1");
    }
}
