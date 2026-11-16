package com.finanzasapp.finanzasapp_backend.exception;

/** Se lanza cuando un id no existe. Se convierte en HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
