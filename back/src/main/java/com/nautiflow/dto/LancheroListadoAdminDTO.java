package com.nautiflow.dto;

import java.time.OffsetDateTime;

/**
 * Resumen de lanchero para el listado administrativo.
 *
 * @param id Identificador del lanchero.
 * @param nombreCompleto Nombre completo de la persona.
 * @param whatsapp Número de contacto del lanchero.
 * @param nombreLancha Nombre de la lancha asociada, o null si no tiene.
 * @param fechaRegistro Fecha y hora de registro.
 * @param estadoSolicitud Estado de la solicitud.
 * @param estadoViaje Estado operativo.
 */
public record LancheroListadoAdminDTO(
        Long id,
        String nombreCompleto,
        String whatsapp,
        String nombreLancha,
        OffsetDateTime fechaRegistro,
        String estadoSolicitud,
        String estadoViaje
) {
}
