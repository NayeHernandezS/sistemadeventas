package org.nhernandez.webapp.sistemaventas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhernandez.webapp.sistemaventas.models.Categoria;
import org.nhernandez.webapp.sistemaventas.models.PlanSuscripcion;
import org.nhernandez.webapp.sistemaventas.repositories.CategoriaRepository;
import org.nhernandez.webapp.sistemaventas.repositories.ProductoRepository;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoPlantillaServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private PlanLimiteService planLimiteService;

    private CatalogoPlantillaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CatalogoPlantillaServiceImpl(
                productoRepository, categoriaRepository, planLimiteService);
    }

    @Test
    void importarCatalogoInicial_noImportaSiYaHayProductos() throws SQLException {
        when(productoRepository.contarPorOwner("tienda1")).thenReturn(5);

        ResultadoImportacionCatalogo resultado = service.importarCatalogoInicial("tienda1", "abarrotes");

        assertEquals(0, resultado.importados());
        verify(productoRepository, never()).guardar(any());
    }

    @Test
    void importarCatalogoInicial_importa30ProductosDeAbarrotes() throws SQLException {
        when(productoRepository.contarPorOwner("tienda1")).thenReturn(0);
        when(planLimiteService.planActivo("tienda1")).thenReturn(PlanSuscripcion.EMPRENDEDOR);
        when(productoRepository.existeSkuPorOwner(eq("tienda1"), any())).thenReturn(false);
        when(categoriaRepository.listarPorOwner("tienda1")).thenReturn(List.of(
                categoria(1L, "Bebidas"),
                categoria(2L, "Lacteos"),
                categoria(3L, "Abarrotes"),
                categoria(4L, "Limpieza"),
                categoria(5L, "Higiene")
        ));

        ResultadoImportacionCatalogo resultado = service.importarCatalogoInicial("tienda1", "abarrotes");

        assertEquals(36, resultado.importados());
        assertEquals(36, resultado.totalPlantilla());
        verify(productoRepository, times(36)).guardar(any());
    }

    private static Categoria categoria(long id, String nombre) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }
}
