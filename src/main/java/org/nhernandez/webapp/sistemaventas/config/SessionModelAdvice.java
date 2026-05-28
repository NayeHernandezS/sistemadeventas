package org.nhernandez.webapp.sistemaventas.config;

import org.nhernandez.webapp.sistemaventas.models.Carro;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone el carro de sesion a las vistas JSP como ${carro}.
 */
@ControllerAdvice
public class SessionModelAdvice {

    private final Carro carro;

    public SessionModelAdvice(Carro carro) {
        this.carro = carro;
    }

    @ModelAttribute("carro")
    public Carro carro() {
        return carro;
    }
}
