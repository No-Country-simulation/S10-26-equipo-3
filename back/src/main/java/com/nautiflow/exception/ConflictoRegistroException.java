package com.nautiflow.exception;

/**
 * Se lanza cuando un registro entra en conflicto con datos ya existentes, por ejemplo un correo duplicado.
 */
public class ConflictoRegistroException extends RuntimeException {
    /**
     * Crea la excepción con el mensaje indicado.
     */
    public ConflictoRegistroException(String message) {
        super(message);
    }
}
