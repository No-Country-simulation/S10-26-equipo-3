package com.nautiflow.service;

import java.time.OffsetDateTime;

/**
 * Evento que se publica cuando se crea un viaje, para que la notificación se envíe después del commit.
 *
 * @param idViaje Identificador del viaje.
 * @param idLanchero Identificador del lanchero.
 * @param correoLanchero Correo electrónico del lanchero.
 * @param nombreLanchero Nombre completo del lanchero.
 * @param zonaOrigen Nombre de la zona de origen.
 * @param zonaDestino Nombre de la zona de destino.
 * @param fechaHoraInicio Fecha y hora de inicio del viaje.
 * @param cantidadPasajeros Cantidad de pasajeros del viaje.
 * @param turistaNombreCompleto Nombre completo del turista.
 * @param turistaWhatsapp Número de WhatsApp del turista.
 */
public record ViajeProgramadoEvent(
        Long idViaje,
        Long idLanchero,
        String correoLanchero,
        String nombreLanchero,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraInicio,
        Integer cantidadPasajeros,
        String turistaNombreCompleto,
        String turistaWhatsapp
) {
}
