package com.nautiflow.exception;

/**
 * Se lanza cuando falla una operación contra el servicio de almacenamiento de archivos.
 */
public class ErrorAlmacenamientoException extends RuntimeException {
    /**
     * Crea la excepción con el mensaje y la causa indicados.
     */
    public ErrorAlmacenamientoException(String message, Throwable cause) {
        super(message, cause);
    }
}
