package com.nautiflow.exception;

/**
 * Se lanza cuando los datos o archivos de una solicitud no cumplen las reglas del negocio.
 */
public class SolicitudInvalidaException extends RuntimeException {
    /**
     * Crea la excepción con el mensaje indicado.
     */
    public SolicitudInvalidaException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con el mensaje indicado.
     */
    public SolicitudInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
