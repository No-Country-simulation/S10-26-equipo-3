package com.nautiflow.service;

/**
 * Datos de un correo transaccional: destinatario, asunto, texto plano y contenido HTML.
 *
 * @param destinatario Correo del destinatario.
 * @param asunto Asunto del correo.
 * @param texto Versión en texto plano del correo.
 * @param html Versión HTML del correo.
 */
public record CorreoTransaccional(String destinatario, String asunto, String texto, String html) {
}
