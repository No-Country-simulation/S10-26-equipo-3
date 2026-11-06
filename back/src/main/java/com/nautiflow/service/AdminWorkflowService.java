package com.nautiflow.service;

import com.nautiflow.dto.DecisionSolicitudDTO;
import com.nautiflow.dto.EstadoOperativoRequest;
import com.nautiflow.dto.LancheroAdminDTO;
import com.nautiflow.dto.PaginaLancherosDTO;
import com.nautiflow.dto.RechazoSolicitudDTO;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.util.FechaHoraUtc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.List;

/**
 * Servicio que ejecuta las decisiones administrativas sobre solicitudes y el estado operativo de los lancheros.
 */
@Service
public class AdminWorkflowService {
    private final LancheroRepository lancheroRepository;
    private final RegistroNotificacionService notificacionService;
    private final Clock clock;

    /**
     * Inyecta el repositorio de lancheros, el servicio de notificaciones y el reloj de negocio.
     */
    public AdminWorkflowService(
            LancheroRepository lancheroRepository,
            RegistroNotificacionService notificacionService,
            Clock clock
    ) {
        this.lancheroRepository = lancheroRepository;
        this.notificacionService = notificacionService;
        this.clock = clock;
    }

    /**
     * Aprueba una solicitud pendiente, activa al lanchero, registra la fecha de decisión y envía el correo de aprobación.
     */
    @Transactional
    public DecisionSolicitudDTO aprobarSolicitud(Long id) {
        Lanchero lanchero = obtenerLancheroParaActualizar(id);
        validarPendiente(lanchero);
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        lanchero.setFechaDecision(FechaHoraUtc.ahora(clock));
        lancheroRepository.saveAndFlush(lanchero);
        notificacionService.enviarDecisionAprobada(
                lanchero.getIdLanchero(), lanchero.getNombreCompleto(), lanchero.getCorreo()
        );
        return new DecisionSolicitudDTO(id, lanchero.getEstadoSolicitud().name(), "Solicitud aprobada.");
    }

    /**
     * Rechaza una solicitud pendiente, guarda el motivo y notifica al lanchero por correo.
     */
    @Transactional
    public RechazoSolicitudDTO rechazarSolicitud(Long id, String motivo) {
        if (motivo != null && motivo.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo no puede superar 500 caracteres.");
        }

        String motivoNormalizado = StringUtils.hasText(motivo) ? motivo.strip() : null;
        Lanchero lanchero = obtenerLancheroParaActualizar(id);
        validarPendiente(lanchero);
        lanchero.setEstadoSolicitud(EstadoSolicitud.RECHAZADO);
        lanchero.setMotivoRechazo(motivoNormalizado);
        lanchero.setFechaDecision(FechaHoraUtc.ahora(clock));
        lancheroRepository.saveAndFlush(lanchero);
        notificacionService.enviarDecisionRechazada(
                lanchero.getIdLanchero(),
                lanchero.getNombreCompleto(),
                lanchero.getCorreo(),
                lanchero.getMotivoRechazo()
        );
        return new RechazoSolicitudDTO(id, lanchero.getEstadoSolicitud().name(), lanchero.getMotivoRechazo());
    }

    /**
     * Lista lancheros con paginación y filtro opcional por estado de solicitud.
     */
    @Transactional(readOnly = true)
    public PaginaLancherosDTO listarLancheros(EstadoSolicitud estado, int page, int size) {
        PaginacionValidator.validar(page, size);
        Sort sort = Sort.by(Sort.Direction.DESC, "fechaRegistro")
                .and(Sort.by(Sort.Direction.DESC, "idLanchero"));
        PageRequest request = PageRequest.of(page, size, sort);
        Page<Lanchero> resultados = estado == null
                ? lancheroRepository.findAll(request)
                : lancheroRepository.findByEstadoSolicitud(estado, request);
        List<LancheroAdminDTO> content = resultados.getContent().stream()
                .map(this::toAdminDTO)
                .toList();
        return new PaginaLancherosDTO(
                content,
                resultados.getNumber(),
                resultados.getSize(),
                resultados.getTotalElements(),
                resultados.getTotalPages()
        );
    }

    /**
     * Cambia el estado operativo de un lanchero aprobado que no esté en viaje.
     */
    @Transactional
    public LancheroAdminDTO actualizarEstadoOperativo(Long id, EstadoOperativoRequest solicitud) {
        Lanchero lanchero = obtenerLancheroParaActualizar(id);
        if (lanchero.getEstadoSolicitud() != EstadoSolicitud.APROBADO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede cambiar el estado operativo de un lanchero aprobado."
            );
        }
        if (lanchero.getEstadoViaje() == EstadoOperativoLanchero.EN_VIAJE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede cambiar el estado operativo mientras el lanchero está en viaje."
            );
        }
        lanchero.setEstadoViaje(EstadoOperativoLanchero.valueOf(solicitud.estadoViaje().name()));
        lancheroRepository.save(lanchero);
        return toAdminDTO(lanchero);
    }

    /**
     * Busca y bloquea al lanchero para evitar decisiones concurrentes.
     */
    private Lanchero obtenerLancheroParaActualizar(Long id) {
        return lancheroRepository.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchero no encontrado."));
    }

    /**
     * Verifica que la solicitud aún esté pendiente antes de aprobarla o rechazarla.
     */
    private void validarPendiente(Lanchero lanchero) {
        if (lanchero.getEstadoSolicitud() != EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya fue decidida.");
        }
    }

    /**
     * Convierte un lanchero en el DTO que consume el panel administrativo.
     */
    private LancheroAdminDTO toAdminDTO(Lanchero lanchero) {
        return new LancheroAdminDTO(
                lanchero.getIdLanchero(),
                lanchero.getNombreCompleto(),
                lanchero.getFechaRegistro(),
                lanchero.getEstadoSolicitud().name(),
                lanchero.getEstadoViaje().name()
        );
    }
}
