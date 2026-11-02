package com.nautiflow.dto;

import java.util.List;

public record PaginaSolicitudesDTO(
        List<SolicitudResumenDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
