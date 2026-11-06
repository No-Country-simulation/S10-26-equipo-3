package com.nautiflow.dto;

import java.time.OffsetDateTime;

/**
 * Resumen de un lanchero para el listado administrativo: identificación, nombre, fecha de registro y estados.
 *
 * @param id Identificador del registro.
 * @param nombreCompleto Nombre completo de la persona.
 * @param fechaRegistro Fecha y hora en que se registró la solicitud.
 * @param estadoSolicitud Estado de la solicitud de registro.
 * @param estadoViaje Estado operativo del lanchero.
 */
public record LancheroAdminDTO(
        Long id,
        String nombreCompleto,
        OffsetDateTime fechaRegistro,
        String estadoSolicitud,
        String estadoViaje
) {
}
