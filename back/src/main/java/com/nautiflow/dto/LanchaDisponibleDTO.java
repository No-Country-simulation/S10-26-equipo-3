package com.nautiflow.dto;

import java.util.List;

/**
 * Embarcación disponible para un viaje, con su capacidad, zonas, verificación y contacto del lanchero.
 *
 * @param id Identificador del registro.
 * @param nombre Nombre del registro.
 * @param capacidad Capacidad máxima de pasajeros.
 * @param zonasDisponibles Nombres de las zonas cubiertas por la embarcación.
 * @param verificada Indica si la embarcación está verificada y puede operar.
 * @param estadoOperativo Estado operativo de la embarcación o del lanchero.
 * @param whatsappLanchero Número de WhatsApp del lanchero.
 */
public record LanchaDisponibleDTO(
        Long id,
        String nombre,
        Integer capacidad,
        List<String> zonasDisponibles,
        boolean verificada,
        String estadoOperativo,
        String whatsappLanchero
) {
}
