package com.nautiflow.exception;

/**
 * Se lanza cuando un archivo supera el tamaño máximo permitido.
 */
public class ArchivoDemasiadoGrandeException extends RuntimeException {
    /**
     * Crea la excepción con el mensaje indicado.
     */
    public ArchivoDemasiadoGrandeException(String message) {
        super(message);
    }
}
