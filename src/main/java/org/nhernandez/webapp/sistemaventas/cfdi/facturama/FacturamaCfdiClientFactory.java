package org.nhernandez.webapp.sistemaventas.cfdi.facturama;

import org.nhernandez.webapp.sistemaventas.cfdi.CfdiCredentials;
import org.springframework.stereotype.Component;

@Component
public class FacturamaCfdiClientFactory {

    public FacturamaCfdiApiClient crear(CfdiCredentials credenciales) {
        String baseUrl = credenciales.sandbox()
                ? "https://apisandbox.facturama.mx"
                : "https://api.facturama.mx";
        return new FacturamaCfdiApiClient(baseUrl, credenciales.username(), credenciales.password());
    }
}
