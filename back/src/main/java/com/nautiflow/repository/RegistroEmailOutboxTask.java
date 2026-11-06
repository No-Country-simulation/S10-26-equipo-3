package com.nautiflow.repository;

/**
 * Tarea de correo pendiente que el worker reclama del outbox.
 *
 * @param id Identificador del registro.
 * @param idLanchero Identificador del lanchero.
 * @param correo Correo electrónico de contacto.
 * @param intento Número de intento de envío.
 * @param payloadJson JSON del correo pendiente de enviar.
 * @param idempotencyKey Clave que evita enviar dos veces el mismo correo.
 */
public record RegistroEmailOutboxTask(
        long id,
        long idLanchero,
        String correo,
        int intento,
        String payloadJson,
        String idempotencyKey
) {
    /**
     * Crea una tarea con payload y clave de idempotencia nulos.
     */
    public RegistroEmailOutboxTask(long id, long idLanchero, String correo, int intento) {
        this(id, idLanchero, correo, intento, null, null);
    }
}
