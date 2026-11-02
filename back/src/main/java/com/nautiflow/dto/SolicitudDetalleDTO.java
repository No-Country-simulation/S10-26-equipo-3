package com.nautiflow.dto;

import java.time.OffsetDateTime;
import java.util.List;

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
    public record EmbarcacionDTO(Long id, String nombre, Integer capacidadPasajeros, List<String> zonas) {
    }

    public record DocumentoDTO(String tipo, String url) {
    }
}
