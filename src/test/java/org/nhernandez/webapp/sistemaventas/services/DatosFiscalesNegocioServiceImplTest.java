package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.DatosFiscalesNegocio;
import org.nhernandez.webapp.sistemaventas.repositories.DatosFiscalesNegocioRepository;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosFiscalesNegocioServiceImplTest {

    @Mock
    private DatosFiscalesNegocioRepository repository;

    @InjectMocks
    private DatosFiscalesNegocioServiceImpl service;

    @Test
    void guardar_persisteDatosNormalizados() throws SQLException {
        DatosFiscalesNegocio entrada = new DatosFiscalesNegocio();
        entrada.setRfc(" xaxx010101000 ");
        entrada.setRazonSocial(" Cliente Demo ");
        entrada.setUsoCfdi("G03");

        service.guardar("tienda1", entrada);

        verify(repository).guardar(any(DatosFiscalesNegocio.class));
    }

    @Test
    void guardar_rechazaRfcInvalido() {
        DatosFiscalesNegocio entrada = new DatosFiscalesNegocio();
        entrada.setRfc("ABC");
        entrada.setRazonSocial("Cliente");

        assertThrows(ServiceJdbcException.class, () -> service.guardar("tienda1", entrada));
    }

    @Test
    void guardar_permiteVaciarDatos() throws SQLException {
        service.guardar("tienda1", new DatosFiscalesNegocio());

        verify(repository).guardar(any(DatosFiscalesNegocio.class));
    }

    @Test
    void consultar_devuelveDatosDelTenant() throws SQLException {
        DatosFiscalesNegocio datos = new DatosFiscalesNegocio();
        datos.setTenantUsername("tienda1");
        datos.setRfc("XAXX010101000");
        when(repository.porTenant("tienda1")).thenReturn(datos);

        Optional<DatosFiscalesNegocio> result = service.consultar("tienda1");

        assertEquals("XAXX010101000", result.orElseThrow().getRfc());
    }

    @Test
    void guardar_rechazaRazonSocialFaltanteConRfc() {
        DatosFiscalesNegocio entrada = new DatosFiscalesNegocio();
        entrada.setRfc("XAXX010101000");

        assertThrows(ServiceJdbcException.class, () -> service.guardar("tienda1", entrada));
    }
}
