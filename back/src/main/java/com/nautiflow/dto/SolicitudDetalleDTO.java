package com.nautiflow.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Detalle completo de una solicitud de registro: datos del lanchero, embarcación y documentos.
 *
 * @param id Identificador del registro.
 * @param nombreCompleto Nombre completo de la persona.
 * @param correo Correo electrónico de contacto.
 * @param whatsapp Número de WhatsApp de contacto.
 * @param estado Estado actual del registro.
 * @param fechaRegistro Fecha y hora en que se registró la solicitud.
 * @param fechaDecision Fecha y hora en que se tomó la decisión.
 * @param motivoRechazo Motivo por el cual se rechazó la solicitud.
 * @param embarcacion Datos de la embarcación asociada.
 * @param documentos Documentos asociados a la solicitud.
 */
public record SolicitudDetalleDTO(
        Long id,
        String nombreCompleto,
        String correo,
        String whatsapp,
        String estado,
        OffsetDateTime fechaRegistro,
        OffsetDateTime fechaDecision,
        String motivoRechazo,
        EmbarcacionDTO embarcacion,
        List<DocumentoDTO> documentos
) {
    /**
     * Datos resumidos de la embarcación asociada a una solicitud.
     *
     * @param id Identificador del registro.
     * @param nombre Nombre del registro.
     * @param capacidadPasajeros Capacidad máxima de pasajeros de la embarcación.
     * @param zonas Nombres de las zonas asociadas.
     */
    public record EmbarcacionDTO(Long id, String nombre, Integer capacidadPasajeros, List<String> zonas) {
    }

    /**
     * Tipo y URL temporal de un documento de la solicitud.
     *
     * @param tipo Tipo o categoría del dato.
     * @param url URL temporal para consultar el archivo.
     */
    public record DocumentoDTO(String tipo, String url) {
    }
}
