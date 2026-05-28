package org.nhernandez.webapp.sistemaventas.configs;

import org.springframework.beans.factory.annotation.Qualifier;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, PARAMETER,TYPE, CONSTRUCTOR})
public @interface ProductoServicePrincipal {
}
