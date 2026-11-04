package com.nautiflow.service;

/**
 * Excepción de Resend con categoría, estado HTTP, reintentabilidad y detalles seguros del proveedor.
 */
public class ResendEmailException extends RuntimeException {
    /**
     * Categorías de error de Resend usadas para decidir si una notificación se puede reintentar.
     */
    public enum Category {
        CONFIGURATION,
        CONNECTION,
        TIMEOUT,
        AUTHENTICATION,
        SENDER_NOT_VERIFIED,
        RECIPIENT_INVALID,
        RATE_LIMIT,
        PROVIDER_TEMPORARY,
        PROVIDER_PERMANENT
    }

    private final Category category;
    private final int httpStatus;
    private final boolean retryable;
    private final String providerErrorCode;
    private final String providerMessage;

    /**
     * Construye la excepción con la categoría, el estado HTTP, la reintentabilidad y los detalles del proveedor.
     */
    ResendEmailException(
            Category category,
            int httpStatus,
            boolean retryable,
            String message,
            Throwable cause,
            String providerErrorCode,
            String providerMessage
    ) {
        super(message, cause);
        this.category = category;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
        this.providerErrorCode = providerErrorCode;
        this.providerMessage = providerMessage;
    }

    /**
     * Obtiene la categoría del error.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Obtiene el código HTTP devuelto por Resend (0 si no hubo respuesta).
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * Indica si el error se puede reintentar.
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Devuelve un código estable para guardar en el outbox, por ejemplo RESEND_TIMEOUT.
     */
    public String getErrorCode() {
        return "RESEND_" + category.name();
    }

    /**
     * Obtiene el código de error entregado por Resend.
     */
    public String getProviderErrorCode() {
        return providerErrorCode;
    }

    /**
     * Obtiene el mensaje de error entregado por Resend, ya saneado.
     */
    public String getProviderMessage() {
        return providerMessage;
    }

    /**
     * Devuelve la cadena de tipos de las causas para facilitar el diagnóstico.
     */
    public String getCauseTypes() {
        StringBuilder types = new StringBuilder();
        java.util.Set<Throwable> visited =
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        Throwable cause = getCause();
        while (cause != null && visited.add(cause)) {
            if (!types.isEmpty()) {
                types.append(" -> ");
            }
            types.append(cause.getClass().getSimpleName());
            cause = cause.getCause();
        }
        return types.isEmpty() ? "none" : types.toString();
    }
}
