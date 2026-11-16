package com.finanzasapp.finanzasapp_backend.exception;

/** Se lanza cuando se rompe una regla de negocio. Se convierte en HTTP 400. */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
