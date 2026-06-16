package org.nhernandez.webapp.sistemaventas.cfdi;

public record CfdiCredentials(String username, String password, boolean sandbox) {

    public boolean validas() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}
