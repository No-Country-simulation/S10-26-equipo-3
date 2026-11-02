package com.nautiflow.dto;

import java.util.List;

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
