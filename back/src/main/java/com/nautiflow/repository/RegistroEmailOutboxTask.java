package com.nautiflow.repository;

public record RegistroEmailOutboxTask(
        long id,
        long idLanchero,
        String correo,
        int intento,
        String payloadJson,
        String idempotencyKey
) {
    public RegistroEmailOutboxTask(long id, long idLanchero, String correo, int intento) {
        this(id, idLanchero, correo, intento, null, null);
    }
}
