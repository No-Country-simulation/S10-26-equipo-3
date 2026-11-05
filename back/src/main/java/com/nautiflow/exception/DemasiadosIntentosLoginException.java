package com.nautiflow.exception;

/**
 * Se lanza cuando una dirección IP supera el máximo de intentos fallidos de inicio de sesión y debe esperar antes de volver a intentarlo.
 */
public class DemasiadosIntentosLoginException extends RuntimeException {
    private final long segundosRestantes;

    /**
     * Crea la excepción con los segundos que faltan para que se levante el bloqueo.
     *
     * @param segundosRestantes segundos restantes de bloqueo, siempre mayor o igual a uno.
     */
    public DemasiadosIntentosLoginException(long segundosRestantes) {
        super("Demasiados intentos de inicio de sesión. Inténtelo de nuevo más tarde.");
        this.segundosRestantes = Math.max(1, segundosRestantes);
    }

    /**
     * Obtiene los segundos que faltan para que se permita un nuevo intento.
     *
     * @return segundos restantes de bloqueo.
     */
    public long getSegundosRestantes() {
        return segundosRestantes;
    }
}
