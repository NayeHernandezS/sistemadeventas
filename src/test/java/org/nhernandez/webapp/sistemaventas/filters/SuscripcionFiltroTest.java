package org.nhernandez.webapp.sistemaventas.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.nhernandez.webapp.sistemaventas.config.JdbcConnectionHolder;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.nhernandez.webapp.sistemaventas.util.RolUtil;
import org.nhernandez.webapp.sistemaventas.util.TenantUtil;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SuscripcionFiltroTest {

    @Mock
    private LoginService loginService;

    @Mock
    private SuscripcionService suscripcionService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    private SuscripcionFiltro filtro;

    @BeforeEach
    void setUp() {
        filtro = new SuscripcionFiltro(loginService, suscripcionService);
        lenient().when(request.getContextPath()).thenReturn("");
        lenient().when(request.getSession()).thenReturn(session);
        lenient().when(session.getAttribute(TenantUtil.SESSION_TENANT)).thenReturn("tienda1");
        lenient().when(session.getAttribute("username")).thenReturn(null);
        lenient().when(session.getAttribute("rol")).thenReturn(null);
        JdbcConnectionHolder.bind(mock(Connection.class));
    }

    @AfterEach
    void tearDown() {
        JdbcConnectionHolder.clear();
    }

    @Test
    void rutaExenta_loginGet_noRequiereSuscripcion() {
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getMethod()).thenReturn("GET");

        assertTrue(SuscripcionFiltro.esRutaExenta(request));
    }

    @Test
    void rutaPermitidaSinPlan_soporteCuandoPlanVencido() {
        when(request.getRequestURI()).thenReturn("/soporte");
        when(session.getAttribute("rol")).thenReturn(RolUtil.ROL_VENDEDOR);

        assertTrue(SuscripcionFiltro.rutaPermitidaSinPlan("/soporte", request));
    }

    @Test
    void rutaPermitidaSinPlan_perfilPermitidoSinPlanActivo() {
        when(session.getAttribute("rol")).thenReturn(RolUtil.ROL_ADMIN);

        assertTrue(SuscripcionFiltro.rutaPermitidaSinPlan("/perfil", request));
    }

    @Test
    void planVencido_bloqueaVentasYRedirigeAdmin() throws Exception {
        when(request.getRequestURI()).thenReturn("/productos");
        when(request.getMethod()).thenReturn("GET");
        when(loginService.getUsername(request)).thenReturn(Optional.of("tienda1"));
        when(session.getAttribute("rol")).thenReturn(RolUtil.ROL_ADMIN);
        when(suscripcionService.tieneAccesoActivo("tienda1")).thenReturn(false);

        filtro.doFilter(request, response, chain);

        verify(response).sendRedirect("/suscripcion?requierePago=1");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void planVencido_permitePerfil() throws Exception {
        when(request.getRequestURI()).thenReturn("/perfil");
        when(request.getMethod()).thenReturn("GET");
        when(loginService.getUsername(request)).thenReturn(Optional.of("vendedor1"));
        when(session.getAttribute("rol")).thenReturn(RolUtil.ROL_VENDEDOR);
        when(suscripcionService.tieneAccesoActivo("tienda1")).thenReturn(false);

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void planVencido_bloqueaReportesYRedirigeVendedor() throws Exception {
        when(request.getRequestURI()).thenReturn("/reportes");
        when(request.getMethod()).thenReturn("GET");
        when(loginService.getUsername(request)).thenReturn(Optional.of("vendedor1"));
        when(session.getAttribute("rol")).thenReturn(RolUtil.ROL_VENDEDOR);
        when(suscripcionService.tieneAccesoActivo("tienda1")).thenReturn(false);

        filtro.doFilter(request, response, chain);

        verify(response).sendRedirect("/?sinPlan=1");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void planVencido_permiteSuscripcion() throws Exception {
        when(request.getRequestURI()).thenReturn("/suscripcion");
        when(request.getMethod()).thenReturn("GET");
        when(loginService.getUsername(request)).thenReturn(Optional.of("tienda1"));
        when(session.getAttribute("rol")).thenReturn(RolUtil.ROL_ADMIN);
        when(suscripcionService.tieneAccesoActivo("tienda1")).thenReturn(false);

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void planActivo_permiteRecursosDelSistema() throws Exception {
        when(request.getRequestURI()).thenReturn("/reportes");
        when(request.getMethod()).thenReturn("GET");
        when(loginService.getUsername(request)).thenReturn(Optional.of("tienda1"));
        when(session.getAttribute("rol")).thenReturn(RolUtil.ROL_ADMIN);
        when(suscripcionService.tieneAccesoActivo("tienda1")).thenReturn(true);

        filtro.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }
}
