package org.nhernandez.webapp.sistemaventas.config;

import org.nhernandez.webapp.sistemaventas.filters.ConexionFilter;
import org.nhernandez.webapp.sistemaventas.filters.SuscripcionFiltro;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ConexionFilter> conexionFilterRegistration(javax.sql.DataSource dataSource) {
        FilterRegistrationBean<ConexionFilter> registration =
                new FilterRegistrationBean<>(new ConexionFilter(dataSource));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        return registration;
    }

    /**
     * Debe ejecutarse despues del filtro de Spring Security ({@code -100}).
     */
    @Bean
    public FilterRegistrationBean<SuscripcionFiltro> suscripcionFiltroRegistration(
            LoginService loginService,
            SuscripcionService suscripcionService) {
        FilterRegistrationBean<SuscripcionFiltro> registration =
                new FilterRegistrationBean<>(new SuscripcionFiltro(loginService, suscripcionService));
        registration.addUrlPatterns(
                "/carro/*",
                "/productos",
                "/productos.html",
                "/crudprod",
                "/crudprod.html",
                "/productos/form",
                "/productos/form/*",
                "/productos/eliminar",
                "/productos/eliminar/*",
                "/categorias",
                "/categorias/*",
                "/tickets",
                "/reportes",
                "/reportes/export",
                "/factura",
                "/factura/pdf",
                "/devoluciones",
                "/devoluciones/*",
                "/suscripcion",
                "/suscripcion/*",
                "/usuarios",
                "/usuarios/*",
                "/admin/pagos",
                "/admin/pagos/*"
        );
        registration.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER + 1);
        return registration;
    }
}
