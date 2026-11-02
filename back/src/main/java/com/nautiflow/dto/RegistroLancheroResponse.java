package com.nautiflow.dto;

public record RegistroLancheroResponse(
        Long idLanchero,
        Long idLancha,
        String estado,
        String mensaje
) {
}
