package com.nautiflow.controller;

import com.nautiflow.dto.AdminDashboardDTO;
import com.nautiflow.dto.CrearViajeRequest;
import com.nautiflow.dto.DecisionSolicitudDTO;
import com.nautiflow.dto.EstadoOperativoRequest;
import com.nautiflow.dto.LanchaDisponibleDTO;
import com.nautiflow.dto.LancheroAdminDTO;
import com.nautiflow.dto.PaginaLancherosDTO;
import com.nautiflow.dto.PaginaSolicitudesDTO;
import com.nautiflow.dto.PaginaResenasDTO;
import com.nautiflow.dto.PaginaViajesDTO;
import com.nautiflow.dto.PaginaSeguimientoViajesDTO;
import com.nautiflow.dto.RechazoSolicitudDTO;
import com.nautiflow.dto.RechazarSolicitudRequest;
import com.nautiflow.dto.SolicitudDetalleDTO;
import com.nautiflow.dto.ViajeAdminDTO;
import com.nautiflow.dto.ViajeFinalizadoDTO;
import com.nautiflow.service.AdminService;
import com.nautiflow.service.AdminWorkflowService;
import com.nautiflow.service.ResenaService;
import com.nautiflow.service.ViajeWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {
    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "APROBADO", "RECHAZADO", "TODOS");

    private final AdminService adminService;
    private final AdminWorkflowService adminWorkflowService;
    private final ViajeWorkflowService viajeWorkflowService;
    private final ResenaService resenaService;

    public AdminController(
            AdminService adminService,
            AdminWorkflowService adminWorkflowService,
            ViajeWorkflowService viajeWorkflowService,
            ResenaService resenaService
    ) {
        this.adminService = adminService;
        this.adminWorkflowService = adminWorkflowService;
        this.viajeWorkflowService = viajeWorkflowService;
        this.resenaService = resenaService;
    }

    @GetMapping("/dashboard")
    @Operation(tags = "Dashboard")
    public AdminDashboardDTO dashboard() {
        return adminService.obtenerDashboard();
    }

    @GetMapping("/solicitudes")
    @Operation(tags = "Solicitudes")
    public PaginaSolicitudesDTO solicitudes(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String estadoNormalizado = estado == null ? null : estado.toUpperCase(Locale.ROOT);
        if (estadoNormalizado != null && !ESTADOS.contains(estadoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado no válido. Valores permitidos: PENDIENTE, APROBADO, RECHAZADO, TODOS."
            );
        }
        return adminService.listarSolicitudes(estadoNormalizado, page, size);
    }

    @GetMapping("/solicitudes/{id}")
    @Operation(tags = "Solicitudes")
    public SolicitudDetalleDTO detalle(@PathVariable Long id) {
        return adminService.obtenerDetalle(id);
    }

    @PatchMapping("/solicitudes/{id}/aprobar")
    @Operation(tags = "Solicitudes")
    public DecisionSolicitudDTO aprobar(@PathVariable Long id) {
        return adminWorkflowService.aprobarSolicitud(id);
    }

    @PatchMapping("/solicitudes/{id}/rechazar")
    @Operation(tags = "Solicitudes")
    public RechazoSolicitudDTO rechazar(
            @PathVariable Long id,
            @Valid @RequestBody RechazarSolicitudRequest solicitud
    ) {
        return adminWorkflowService.rechazarSolicitud(id, solicitud.motivo());
    }

    @GetMapping("/lancheros")
    @Operation(tags = "Lancheros")
    public PaginaLancherosDTO lancheros(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String estadoNormalizado = estado == null ? null : estado.toUpperCase(Locale.ROOT);
        if (estadoNormalizado != null && !ESTADOS.contains(estadoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado no válido. Valores permitidos: PENDIENTE, APROBADO, RECHAZADO, TODOS."
            );
        }
        return adminWorkflowService.listarLancheros(estadoNormalizado, page, size);
    }

    @PatchMapping("/lancheros/{id}/estado-viaje")
    @Operation(tags = "Lancheros")
    public LancheroAdminDTO actualizarEstadoOperativoLanchero(
            @PathVariable Long id,
            @Valid @RequestBody EstadoOperativoRequest solicitud
    ) {
        return adminWorkflowService.actualizarEstadoOperativo(id, solicitud);
    }

    @GetMapping("/lancheros/disponibles")
    @Operation(
            tags = "Lancheros",
            summary = "Buscar embarcaciones operativas para una ruta y hora",
            description = "Solo incluye lancheros aprobados y activos sin un viaje incompatible en curso. "
                    + "La fecha es la actual según el Clock configurado; no se registran reservas futuras ni duraciones."
    )
    public java.util.List<LanchaDisponibleDTO> lanchasDisponibles(
            @RequestParam(required = false) Integer capacidad,
            @RequestParam(name = "zonaOrigen", required = false) String zonaOrigen,
            @RequestParam(name = "zonaDestino", required = false) String zonaDestino,
            @RequestParam(name = "zona_origen", required = false) String zonaOrigenId,
            @RequestParam(name = "zona_destino", required = false) String zonaDestinoId,
            @RequestParam String hora
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

    @PostMapping("/viajes")
    @Operation(tags = "Viajes")
    public ResponseEntity<ViajeAdminDTO> crearViaje(@Valid @RequestBody CrearViajeRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(viajeWorkflowService.crearEIniciar(solicitud));
    }

    @GetMapping("/viajes")
    @Operation(tags = "Viajes")
    public PaginaViajesDTO viajesEnCurso(
            @RequestParam(defaultValue = "EN_VIAJE") String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (!"EN_VIAJE".equals(estado.toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El único estado admitido es EN_VIAJE.");
        }
        return viajeWorkflowService.listarEnCurso(page, size);
    }

    @GetMapping("/viajes/seguimiento")
    @Operation(
            tags = "Viajes",
            summary = "Consultar el historial y estado operativo de los viajes",
            description = "Incluye viajes en curso y finalizados; la última ubicación confirmada se deriva "
                    + "del destino del viaje finalizado más reciente de cada embarcación."
    )
    public PaginaSeguimientoViajesDTO seguimientoViajes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return viajeWorkflowService.listarSeguimiento(page, size);
    }

    private String combinarFiltroZona(String nombre, String id, String tipo) {
        if (nombre != null && id != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Indique la zona de " + tipo + " usando solo uno de sus parámetros."
            );
        }
        return id == null ? nombre : id;
    }

    @PatchMapping("/viajes/{id}/finalizar")
    @Operation(tags = "Viajes")
    public ViajeFinalizadoDTO finalizarViaje(@PathVariable Long id) {
        return viajeWorkflowService.finalizar(id);
    }

    @GetMapping("/resenas")
    @Operation(tags = "Reseñas")
    public PaginaResenasDTO resenas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return resenaService.listar(page, size);
    }
}
