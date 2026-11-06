package com.nautiflow.controller;

import com.nautiflow.dto.CrearResenaRequest;
import com.nautiflow.dto.ResenaCreadaDTO;
import com.nautiflow.dto.ResenaViajeDTO;
import com.nautiflow.service.ResenaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints públicos para consultar un viaje por su token de reseña y para registrar la reseña.
 */
@RestController
@Tag(name = "Reseñas públicas")
public class ResenaController {
    private final ResenaService resenaService;

    /**
     * Inyecta el servicio de reseñas.
     */
    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    /**
     * Consulta el viaje asociado al token de reseña y muestra si ya tiene una reseña.
     */
    @GetMapping("/resena/{token}")
    public ResenaViajeDTO consultar(@PathVariable String token) {
        return resenaService.obtenerViaje(token);
    }

    /**
     * Registra la reseña del viaje asociado al token.
     */
    @PostMapping("/resena/{token}")
    public ResponseEntity<ResenaCreadaDTO> crear(
            @PathVariable String token,
            @Valid @RequestBody CrearResenaRequest solicitud
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaService.crear(solicitud, token));
    }
}
