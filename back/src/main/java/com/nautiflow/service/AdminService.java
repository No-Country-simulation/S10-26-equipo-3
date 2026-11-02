package com.nautiflow.service;

import com.nautiflow.dto.AdminDashboardDTO;
import com.nautiflow.dto.PaginaSolicitudesDTO;
import com.nautiflow.dto.SolicitudDetalleDTO;
import com.nautiflow.dto.SolicitudResumenDTO;
import com.nautiflow.model.Documento;
import com.nautiflow.model.Lanchero;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class AdminService {
    private final LancheroRepository lancheroRepository;
    private final LanchaRepository lanchaRepository;
    private final ArchivoLancheroStorage archivoStorage;

    public AdminService(
            LancheroRepository lancheroRepository,
            LanchaRepository lanchaRepository,
            ArchivoLancheroStorage archivoStorage
    ) {
        this.lancheroRepository = lancheroRepository;
        this.lanchaRepository = lanchaRepository;
        this.archivoStorage = archivoStorage;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDTO obtenerDashboard() {
        return new AdminDashboardDTO(
                lancheroRepository.countByEstadoSolicitud("PENDIENTE"),
                lanchaRepository.countByLanchero_EstadoSolicitud("APROBADO"),
                lancheroRepository.countByEstadoViaje("EN_VIAJE")
        );
    }

    @Transactional(readOnly = true)
    public PaginaSolicitudesDTO listarSolicitudes(String estado, int page, int size) {
        PaginacionValidator.validar(page, size);
        Sort sort = Sort.by(Sort.Direction.DESC, "fechaRegistro")
                .and(Sort.by(Sort.Direction.DESC, "idLanchero"));
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Lanchero> resultados = estado == null || "TODOS".equals(estado)
                ? lancheroRepository.findAll(pageRequest)
                : lancheroRepository.findByEstadoSolicitud(estado, pageRequest);

        List<SolicitudResumenDTO> content = resultados.getContent().stream()
                .map(lanchero -> new SolicitudResumenDTO(
                        lanchero.getIdLanchero(),
                        lanchero.getNombreCompleto(),
                        lanchero.getFechaRegistro().toLocalDate(),
                        lanchero.getEstadoSolicitud()
                ))
                .toList();

        return new PaginaSolicitudesDTO(
                content,
                resultados.getNumber(),
                resultados.getSize(),
                resultados.getTotalElements(),
                resultados.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public SolicitudDetalleDTO obtenerDetalle(Long id) {
        Lanchero lanchero = lancheroRepository.findByIdLanchero(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));

        SolicitudDetalleDTO.EmbarcacionDTO embarcacion = lanchero.getLancha() == null
                ? null
                : new SolicitudDetalleDTO.EmbarcacionDTO(
                        lanchero.getLancha().getIdLancha(),
                        lanchero.getLancha().getNombre(),
                        lanchero.getLancha().getCapacidad(),
                        lanchero.getLancha().getZonas().stream()
                                .map(zona -> zona.getNombre())
                                .sorted()
                                .toList()
                );

        List<SolicitudDetalleDTO.DocumentoDTO> documentos = lanchero.getDocumentos().stream()
                .sorted(Comparator.comparing(Documento::getTipoDocumento))
                .map(documento -> new SolicitudDetalleDTO.DocumentoDTO(
                        documento.getTipoDocumento(),
                        archivoStorage.generarUrlPrefirmada(documento.getClaveArchivo())
                ))
                .toList();

        return new SolicitudDetalleDTO(
                lanchero.getIdLanchero(),
                lanchero.getNombreCompleto(),
                lanchero.getCorreo(),
                lanchero.getWhatsapp(),
                lanchero.getEstadoSolicitud(),
                lanchero.getFechaRegistro(),
                lanchero.getFechaDecision(),
                lanchero.getMotivoRechazo(),
                embarcacion,
                documentos
        );
    }
}
