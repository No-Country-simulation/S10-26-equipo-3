package com.nautiflow.repository;

/**
 * Payload ya persistido de una notificación de registro, junto con su clave de idempotencia.
 *
 * @param json JSON del correo listo para enviar a Resend.
 * @param idempotencyKey Clave que evita enviar dos veces el mismo correo.
 */
public record RegistroEmailOutboxPayload(String json, String idempotencyKey) {
}
