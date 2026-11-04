package com.nautiflow.controller;

import com.nautiflow.dto.PaginaResenasDTO;
import com.nautiflow.service.ResenaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la consulta paginada de reseñas para el panel administrativo.
 */
@RestController
@RequestMapping("/admin")
public class ResenaAdminController {
    private final ResenaService resenaService;

    /**
     * Inyecta el servicio de reseñas.
     */
    public ResenaAdminController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    /**
     * Lista las reseñas con paginación para el panel administrativo.
     */
    @GetMapping("/resenas")
    @Operation(tags = "Reseñas")
    public PaginaResenasDTO resenas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return resenaService.listar(page, size);
    }
}
