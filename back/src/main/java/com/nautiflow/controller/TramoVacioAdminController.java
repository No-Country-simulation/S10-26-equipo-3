package com.nautiflow.controller;

import com.nautiflow.dto.CrearTramoVacioRequest;
import com.nautiflow.dto.TramoVacioDTO;
import com.nautiflow.service.TramoVacioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class TramoVacioAdminController {
    private final TramoVacioService tramoVacioService;

    public TramoVacioAdminController(TramoVacioService tramoVacioService) {
        this.tramoVacioService = tramoVacioService;
    }

    @PostMapping("/viajes/{id}/retorno-vacio")
    @Operation(tags = "Tramos vacíos", summary = "Publicar el retorno vacío de un viaje")
    public ResponseEntity<TramoVacioDTO> publicar(
            @PathVariable Long id,
            @Valid @RequestBody CrearTramoVacioRequest solicitud
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tramoVacioService.publicar(id, solicitud));
    }

    @GetMapping("/tramos-vacios")
    @Operation(tags = "Tramos vacíos", summary = "Listar los tramos vacíos de hoy en adelante")
    public List<TramoVacioDTO> listar() {
        return tramoVacioService.listarVigentes();
    }

    @DeleteMapping("/tramos-vacios/{id}")
    @Operation(tags = "Tramos vacíos", summary = "Quitar un tramo vacío")
    public ResponseEntity<Void> cerrar(@PathVariable Long id) {
        tramoVacioService.cerrar(id);
        return ResponseEntity.noContent().build();
    }
}
