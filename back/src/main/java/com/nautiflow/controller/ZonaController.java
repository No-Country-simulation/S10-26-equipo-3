package com.nautiflow.controller;

import com.nautiflow.dto.ZonaDTO;
import com.nautiflow.service.ZonaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Zonas", description = "Consulta de zonas disponibles.")
public class ZonaController {
    private final ZonaService zonaService;

    public ZonaController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    @GetMapping("/zonas")
    public List<ZonaDTO> listar() {
        return zonaService.listar();
    }
}
