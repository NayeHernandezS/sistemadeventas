package org.nhernandez.webapp.sistemaventas.cfdi;

public class CfdiException extends RuntimeException {

    public CfdiException(String message) {
        super(message);
    }

    public CfdiException(String message, Throwable cause) {
        super(message, cause);
    }
}
