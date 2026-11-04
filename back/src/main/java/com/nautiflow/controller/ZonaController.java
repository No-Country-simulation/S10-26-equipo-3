package com.nautiflow.controller;

import com.nautiflow.dto.ZonaDTO;
import com.nautiflow.service.ZonaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expone el catálogo público de zonas disponibles.
 */
@RestController
@Tag(name = "Zonas")
public class ZonaController {
    private final ZonaService zonaService;

    /**
     * Inyecta el servicio de zonas.
     */
    public ZonaController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    /**
     * Devuelve el catálogo de zonas ordenado alfabéticamente.
     */
    @GetMapping("/zonas")
    public List<ZonaDTO> listar() {
        return zonaService.listar();
    }
}
