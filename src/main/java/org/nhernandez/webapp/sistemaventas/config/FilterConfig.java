package org.nhernandez.webapp.sistemaventas.config;

import org.nhernandez.webapp.sistemaventas.filters.AdminFiltro;
import org.nhernandez.webapp.sistemaventas.filters.AdminProductoFiltro;
import org.nhernandez.webapp.sistemaventas.filters.ConexionFilter;
import org.nhernandez.webapp.sistemaventas.filters.LoginFiltro;
import org.nhernandez.webapp.sistemaventas.filters.SuscripcionFiltro;
import org.nhernandez.webapp.sistemaventas.services.LoginService;
import org.nhernandez.webapp.sistemaventas.services.SuscripcionService;
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

    @Bean
    public FilterRegistrationBean<LoginFiltro> loginFiltroRegistration(LoginService loginService) {
        FilterRegistrationBean<LoginFiltro> registration =
                new FilterRegistrationBean<>(new LoginFiltro(loginService));
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
                "/tickets",
                "/reportes",
                "/factura",
                "/suscripcion",
                "/admin/pagos"
        );
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 15);
        return registration;
    }

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
                "/tickets",
                "/reportes",
                "/factura",
                "/suscripcion",
                "/usuarios",
                "/usuarios/*"
        );
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 25);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AdminFiltro> adminFiltroRegistration(LoginService loginService) {
        FilterRegistrationBean<AdminFiltro> registration =
                new FilterRegistrationBean<>(new AdminFiltro(loginService));
        registration.addUrlPatterns("/usuarios", "/usuarios/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 35);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AdminProductoFiltro> adminProductoFiltroRegistration(LoginService loginService) {
        FilterRegistrationBean<AdminProductoFiltro> registration =
                new FilterRegistrationBean<>(new AdminProductoFiltro(loginService));
        registration.addUrlPatterns(
                "/productos/form",
                "/productos/form/*",
                "/productos/eliminar",
                "/productos/eliminar/*"
        );
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 45);
        return registration;
    }
}
