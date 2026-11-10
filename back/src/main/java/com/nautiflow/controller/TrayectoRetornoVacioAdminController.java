package com.nautiflow.controller;

import com.nautiflow.dto.CrearTramoVacioRequest;
import com.nautiflow.dto.MensajeTramoVacioDTO;
import com.nautiflow.dto.TramoVacioDTO;
import com.nautiflow.service.TrayectoRetornoVacioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints administrativos para publicar, consultar y gestionar retornos vacíos.
 */
@RestController
@RequestMapping("/admin")
public class TrayectoRetornoVacioAdminController {
    private final TrayectoRetornoVacioService trayectoService;

    public TrayectoRetornoVacioAdminController(TrayectoRetornoVacioService trayectoService) {
        this.trayectoService = trayectoService;
    }

    @PostMapping("/viajes/{viajeId}/retornos-vacios")
    @Operation(tags = "Retornos vacíos", summary = "Publicar el retorno vacío de un viaje")
    public ResponseEntity<TramoVacioDTO> publicar(
            @PathVariable Long viajeId,
            @Valid @RequestBody CrearTramoVacioRequest solicitud
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trayectoService.publicar(viajeId, solicitud));
    }

    @GetMapping("/tramos-vacios")
    @Operation(tags = "Retornos vacíos", summary = "Consultar retornos vacíos disponibles")
    public List<TramoVacioDTO> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String estado
    ) {
        return trayectoService.listar(fechaDesde, fechaHasta, estado);
    }

    @GetMapping("/tramos-vacios/{id}/mensaje")
    @Operation(tags = "Retornos vacíos", summary = "Consultar el mensaje para compartir el retorno")
    public MensajeTramoVacioDTO obtenerMensaje(@PathVariable Long id) {
        return trayectoService.obtenerMensaje(id);
    }

    @PatchMapping("/tramos-vacios/{id}/cancelar")
    @Operation(tags = "Retornos vacíos", summary = "Cancelar un retorno vacío")
    public TramoVacioDTO cancelar(@PathVariable Long id) {
        return trayectoService.cancelar(id);
    }

    @PatchMapping("/tramos-vacios/{id}/finalizar")
    @Operation(tags = "Retornos vacíos", summary = "Finalizar un retorno vacío")
    public TramoVacioDTO finalizar(@PathVariable Long id) {
        return trayectoService.finalizar(id);
    }
}
