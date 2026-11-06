package com.nautiflow.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Petición para cambiar el estado operativo de un lanchero a ACTIVO o INACTIVO.
 *
 * @param estadoViaje Estado operativo del lanchero.
 */
public record EstadoOperativoRequest(@NotNull EstadoOperativo estadoViaje) {
    /**
     * Estados que el administrador puede asignar manualmente a un lanchero. EN_VIAJE no se permite por esta vía.
     */
    public enum EstadoOperativo {
        ACTIVO,
        INACTIVO
    }
}
