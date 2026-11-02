package com.nautiflow.controller;

import com.nautiflow.dto.ZonaDTO;
import com.nautiflow.service.ZonaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
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
