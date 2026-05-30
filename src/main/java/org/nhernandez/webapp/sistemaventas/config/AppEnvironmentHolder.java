package org.nhernandez.webapp.sistemaventas.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Permite leer propiedades de Spring ({@code application.properties}, {@code .env})
 * desde utilidades estaticas sin duplicar la carga del classpath.
 */
@Component
public class AppEnvironmentHolder implements ApplicationContextAware {

    private static Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        environment = applicationContext.getEnvironment();
    }

    public static String getProperty(String key, String defaultValue) {
        if (environment == null) {
            return defaultValue;
        }
        return environment.getProperty(key, defaultValue);
    }
}
