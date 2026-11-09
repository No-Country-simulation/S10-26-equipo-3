package com.nautiflow.dto;

import java.time.OffsetDateTime;

/**
 * Datos de un viaje que aún no ha finalizado.
 *
 * @param id Identificador del registro.
 * @param idLancha Identificador de la lancha.
 * @param idLanchero Identificador del lanchero.
 * @param lancheroNombreCompleto Nombre completo del lanchero.
 * @param zonaOrigen Nombre de la zona de origen.
 * @param zonaDestino Nombre de la zona de destino.
 * @param fechaHoraInicio Fecha y hora de inicio del viaje.
 * @param fechaHoraFin Fecha y hora de finalización del viaje.
 * @param cantidadPasajeros Cantidad de pasajeros del viaje.
 * @param turistaNombreCompleto Nombre completo del turista.
 * @param turistaWhatsapp Número de WhatsApp del turista.
 */
public record ViajeEnCursoDTO(
        Long id,
        Long idLancha,
        Long idLanchero,
        String lancheroNombreCompleto,
        String zonaOrigen,
        String zonaDestino,
        OffsetDateTime fechaHoraInicio,
        OffsetDateTime fechaHoraFin,
        Integer cantidadPasajeros,
        String turistaNombreCompleto,
        String turistaWhatsapp
) {
}
