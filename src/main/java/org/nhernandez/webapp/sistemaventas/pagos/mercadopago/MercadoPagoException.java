package org.nhernandez.webapp.sistemaventas.pagos.mercadopago;

public class MercadoPagoException extends RuntimeException {

    public MercadoPagoException(String message) {
        super(message);
    }

    public MercadoPagoException(String message, Throwable cause) {
        super(message, cause);
    }
}
