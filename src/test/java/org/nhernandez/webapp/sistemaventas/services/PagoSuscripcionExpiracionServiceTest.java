package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.repositories.PagoSuscripcionRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoSuscripcionExpiracionServiceTest {

    @Mock
    private PagoSuscripcionRepository pagoRepository;

    @InjectMocks
    private PagoSuscripcionExpiracionService expiracionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(expiracionService, "diasManual", 30);
        ReflectionTestUtils.setField(expiracionService, "diasMercadoPago", 15);
    }

    @Test
    void expirarAhora_delegaEnRepositorio() throws SQLException {
        when(pagoRepository.expirarPendientesAnterioresA(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3);

        int expirados = expiracionService.expirarAhora();

        assertEquals(3, expirados);
        verify(pagoRepository).expirarPendientesAnterioresA(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}
