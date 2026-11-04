package com.nautiflow.exception;

/**
 * Se lanza cuando la configuración de Cloudflare R2 está incompleta y no es posible operar con archivos.
 */
public class AlmacenamientoNoConfiguradoException extends RuntimeException {
    /**
     * Crea la excepción con el mensaje indicado.
     */
    public AlmacenamientoNoConfiguradoException(String message) {
        super(message);
    }
}
