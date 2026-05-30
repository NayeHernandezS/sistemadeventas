package org.nhernandez.webapp.sistemaventas.config;

import jakarta.servlet.DispatcherType;
import org.nhernandez.webapp.sistemaventas.security.LoginAuthenticationFailureHandler;
import org.nhernandez.webapp.sistemaventas.security.TenantAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TenantAuthenticationSuccessHandler successHandler,
            LoginAuthenticationFailureHandler failureHandler) throws Exception {
        http
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/login.html"),
                                new AntPathRequestMatcher("/login.jsp"),
                                new AntPathRequestMatcher("/registro"),
                                new AntPathRequestMatcher("/registro/**"),
                                new AntPathRequestMatcher("/recuperar"),
                                new AntPathRequestMatcher("/recuperar/**"),
                                new AntPathRequestMatcher("/logout"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/img/**")
                        ).permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/plataforma/**")
                        ).hasRole("SUPER_ADMIN")
                        .requestMatchers(
                                new AntPathRequestMatcher("/usuarios/**"),
                                new AntPathRequestMatcher("/admin/**"),
                                new AntPathRequestMatcher("/categorias/**"),
                                new AntPathRequestMatcher("/productos/form"),
                                new AntPathRequestMatcher("/productos/form/**"),
                                new AntPathRequestMatcher("/productos/eliminar"),
                                new AntPathRequestMatcher("/productos/eliminar/**")
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/acceso-denegado")
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login.html")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
