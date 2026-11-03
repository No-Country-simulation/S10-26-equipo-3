package com.nautiflow.service;

public class ResendEmailException extends RuntimeException {
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

    public Category getCategory() {
        return category;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public String getErrorCode() {
        return "RESEND_" + category.name();
    }

    public String getProviderErrorCode() {
        return providerErrorCode;
    }

    public String getProviderMessage() {
        return providerMessage;
    }

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
