package com.nautiflow.service;

import com.nautiflow.dto.DecisionSolicitudDTO;
import com.nautiflow.dto.EstadoOperativoRequest;
import com.nautiflow.dto.LancheroAdminDTO;
import com.nautiflow.dto.PaginaLancherosDTO;
import com.nautiflow.dto.RechazoSolicitudDTO;
import com.nautiflow.model.Lanchero;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.time.FechaHoraUtc;
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

@Service
public class AdminWorkflowService {
    private final LancheroRepository lancheroRepository;
    private final RegistroNotificacionService notificacionService;
    private final Clock clock;

    public AdminWorkflowService(
            LancheroRepository lancheroRepository,
            RegistroNotificacionService notificacionService,
            Clock clock
    ) {
        this.lancheroRepository = lancheroRepository;
        this.notificacionService = notificacionService;
        this.clock = clock;
    }

    @Transactional
    public DecisionSolicitudDTO aprobarSolicitud(Long id) {
        Lanchero lanchero = obtenerLancheroParaActualizar(id);
        validarPendiente(lanchero);
        lanchero.setEstadoSolicitud("APROBADO");
        lanchero.setEstadoViaje("ACTIVO");
        lanchero.setFechaDecision(FechaHoraUtc.ahora(clock));
        lancheroRepository.saveAndFlush(lanchero);
        notificacionService.enviarDecisionAprobada(
                lanchero.getIdLanchero(), lanchero.getNombreCompleto(), lanchero.getCorreo()
        );
        return new DecisionSolicitudDTO(id, lanchero.getEstadoSolicitud(), "Solicitud aprobada.");
    }

    @Transactional
    public RechazoSolicitudDTO rechazarSolicitud(Long id, String motivo) {
        if (motivo != null && motivo.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo no puede superar 500 caracteres.");
        }

        String motivoNormalizado = StringUtils.hasText(motivo) ? motivo.strip() : null;
        Lanchero lanchero = obtenerLancheroParaActualizar(id);
        validarPendiente(lanchero);
        lanchero.setEstadoSolicitud("RECHAZADO");
        lanchero.setMotivoRechazo(motivoNormalizado);
        lanchero.setFechaDecision(FechaHoraUtc.ahora(clock));
        lancheroRepository.saveAndFlush(lanchero);
        notificacionService.enviarDecisionRechazada(
                lanchero.getIdLanchero(),
                lanchero.getNombreCompleto(),
                lanchero.getCorreo(),
                lanchero.getMotivoRechazo()
        );
        return new RechazoSolicitudDTO(id, lanchero.getEstadoSolicitud(), lanchero.getMotivoRechazo());
    }

    @Transactional(readOnly = true)
    public PaginaLancherosDTO listarLancheros(String estado, int page, int size) {
        PaginacionValidator.validar(page, size);
        Sort sort = Sort.by(Sort.Direction.DESC, "fechaRegistro")
                .and(Sort.by(Sort.Direction.DESC, "idLanchero"));
        PageRequest request = PageRequest.of(page, size, sort);
        Page<Lanchero> resultados = estado == null || "TODOS".equals(estado)
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

    @Transactional
    public LancheroAdminDTO actualizarEstadoOperativo(Long id, EstadoOperativoRequest solicitud) {
        Lanchero lanchero = obtenerLancheroParaActualizar(id);
        if (!"APROBADO".equals(lanchero.getEstadoSolicitud())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede cambiar el estado operativo de un lanchero aprobado."
            );
        }
        if ("EN_VIAJE".equals(lanchero.getEstadoViaje())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede cambiar el estado operativo mientras el lanchero está en viaje."
            );
        }
        lanchero.setEstadoViaje(solicitud.estadoViaje().name());
        lancheroRepository.save(lanchero);
        return toAdminDTO(lanchero);
    }

    private Lanchero obtenerLancheroParaActualizar(Long id) {
        return lancheroRepository.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchero no encontrado."));
    }

    private void validarPendiente(Lanchero lanchero) {
        if (!"PENDIENTE".equals(lanchero.getEstadoSolicitud())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya fue decidida.");
        }
    }

    private LancheroAdminDTO toAdminDTO(Lanchero lanchero) {
        return new LancheroAdminDTO(
                lanchero.getIdLanchero(),
                lanchero.getNombreCompleto(),
                lanchero.getFechaRegistro(),
                lanchero.getEstadoSolicitud(),
                lanchero.getEstadoViaje()
        );
    }
}
