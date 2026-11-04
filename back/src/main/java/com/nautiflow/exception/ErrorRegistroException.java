package com.nautiflow.exception;

/**
 * Se lanza cuando falla el proceso de registro de un lanchero por una causa no controlada.
 */
public class ErrorRegistroException extends RuntimeException {
    /**
     * Crea la excepción con el mensaje y la causa indicados.
     */
    public ErrorRegistroException(String message, Throwable cause) {
        super(message, cause);
    }
}
