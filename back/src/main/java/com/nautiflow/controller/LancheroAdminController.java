package com.nautiflow.controller;

import com.nautiflow.dto.EstadoOperativoRequest;
import com.nautiflow.dto.LanchaDisponibleDTO;
import com.nautiflow.dto.LancheroAdminDTO;
import com.nautiflow.dto.PaginaLancherosDTO;
import com.nautiflow.service.AdminWorkflowService;
import com.nautiflow.service.ViajeWorkflowService;
import com.nautiflow.util.EstadoSolicitudFiltro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Expone la consulta de lancheros, el cambio de estado operativo y la búsqueda de embarcaciones disponibles para el administrador.
 */
@RestController
@RequestMapping("/admin")
public class LancheroAdminController {
    private final AdminWorkflowService adminWorkflowService;
    private final ViajeWorkflowService viajeWorkflowService;

    /**
     * Inyecta los servicios de administración de lancheros y de viajes.
     */
    public LancheroAdminController(
            AdminWorkflowService adminWorkflowService,
            ViajeWorkflowService viajeWorkflowService
    ) {
        this.adminWorkflowService = adminWorkflowService;
        this.viajeWorkflowService = viajeWorkflowService;
    }

    /**
     * Lista los lancheros con paginación y filtro opcional por estado de solicitud.
     */
    @GetMapping("/lancheros")
    @Operation(tags = "Lancheros")
    public PaginaLancherosDTO lancheros(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminWorkflowService.listarLancheros(EstadoSolicitudFiltro.parse(estado), page, size);
    }

    /**
     * Cambia el estado operativo de un lanchero aprobado que no esté en viaje.
     */
    @PatchMapping("/lancheros/{id}/estado-viaje")
    @Operation(tags = "Lancheros")
    public LancheroAdminDTO actualizarEstadoOperativoLanchero(
            @PathVariable Long id,
            @Valid @RequestBody EstadoOperativoRequest solicitud
    ) {
        return adminWorkflowService.actualizarEstadoOperativo(id, solicitud);
    }

    /**
     * Busca embarcaciones disponibles para una capacidad, ruta y hora determinadas.
     */
    @GetMapping("/lancheros/disponibles")
    @Operation(
            tags = "Lancheros",
            summary = "Buscar embarcaciones operativas para una ruta y hora"
    )
    public List<LanchaDisponibleDTO> lanchasDisponibles(
            @Parameter(schema = @Schema(type = "integer", example = "4"))
            @RequestParam(name = "capacidad", required = false) Integer capacidad,
            @Parameter(schema = @Schema(type = "integer", example = "1"))
            @RequestParam(name = "zonaOrigen", required = false) String zonaOrigen,
            @Parameter(schema = @Schema(type = "integer", example = "3"))
            @RequestParam(name = "zonaDestino", required = false) String zonaDestino,
            @Parameter(hidden = true)
            @RequestParam(name = "zona_origen", required = false) String zonaOrigenId,
            @Parameter(hidden = true)
            @RequestParam(name = "zona_destino", required = false) String zonaDestinoId,
            @Parameter(schema = @Schema(type = "string", pattern = "^([01][0-9]|2[0-3]):[0-5][0-9]$", example = "23:00"))
            @RequestParam(name = "hora") String hora
    ) {
        LocalTime horaSeleccionada;
        try {
            horaSeleccionada = LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora debe tener formato HH:mm.");
        }
        return viajeWorkflowService.buscarDisponibles(
                capacidad,
                combinarFiltroZona(zonaOrigen, zonaOrigenId, "origen"),
                combinarFiltroZona(zonaDestino, zonaDestinoId, "destino"),
                horaSeleccionada
        );
    }

    /**
     * Combina los parámetros de zona por nombre e ID, rechazando que se envíen ambos.
     */
    private String combinarFiltroZona(String nombre, String id, String tipo) {
        if (nombre != null && id != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Indique la zona de " + tipo + " usando solo uno de sus parámetros."
            );
        }
        return id == null ? nombre : id;
    }
}
