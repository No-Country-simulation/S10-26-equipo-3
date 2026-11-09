package com.nautiflow.service;

import com.nautiflow.dto.ActividadMensualDTO;
import com.nautiflow.dto.AdminDashboardDTO;
import com.nautiflow.dto.PaginaSolicitudesDTO;
import com.nautiflow.dto.SolicitudDetalleDTO;
import com.nautiflow.dto.SolicitudResumenDTO;
import com.nautiflow.model.Documento;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.model.Lanchero;
import com.nautiflow.repository.DocumentoRepository;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.TrayectoRetornoVacioRepository;
import com.nautiflow.repository.ViajeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.nautiflow.util.ZonaNegocio;

/**
 * Servicio de consultas del panel administrativo: métricas, listado y detalle de solicitudes.
 */
@Service
public class AdminService {
    private final LancheroRepository lancheroRepository;
    private final LanchaRepository lanchaRepository;
    private final DocumentoRepository documentoRepository;
    private final ArchivoLancheroStorage archivoStorage;
    private final TrayectoRetornoVacioRepository tramoRepository;
    private final ViajeRepository viajeRepository;
    private final Clock clock;

    /**
     * Inyecta los repositorios y el servicio de archivos usados por el panel.
     */
    public AdminService(
            LancheroRepository lancheroRepository,
            LanchaRepository lanchaRepository,
            DocumentoRepository documentoRepository,
            ArchivoLancheroStorage archivoStorage,
            TrayectoRetornoVacioRepository tramoRepository,
            ViajeRepository viajeRepository,
            Clock clock
    ) {
        this.lancheroRepository = lancheroRepository;
        this.lanchaRepository = lanchaRepository;
        this.documentoRepository = documentoRepository;
        this.archivoStorage = archivoStorage;
        this.tramoRepository = tramoRepository;
        this.viajeRepository = viajeRepository;
        this.clock = clock;
    }

    /**
     * Cuenta solicitudes pendientes, lanchas verificadas y viajes activos para el panel administrativo.
     */
    @Transactional(readOnly = true)
    public AdminDashboardDTO obtenerDashboard() {
        LocalDate hoy = LocalDate.now(clock.withZone(ZonaNegocio.ZONA));
        return new AdminDashboardDTO(
                lancheroRepository.countByEstadoSolicitud(EstadoSolicitud.PENDIENTE),
                lanchaRepository.countByLanchero_EstadoSolicitud(EstadoSolicitud.APROBADO),
                lancheroRepository.countByEstadoViaje(EstadoOperativoLanchero.EN_VIAJE),
                tramoRepository.countByEstadoAndFechaHoraRetornoGreaterThanEqual(
                        TramoVacioService.ACTIVO,
                        hoy.atStartOfDay(ZonaNegocio.ZONA).toOffsetDateTime()
                ),
                actividadMensual(YearMonth.from(hoy))
        );
    }

    private List<ActividadMensualDTO> actividadMensual(YearMonth mesActual) {
        YearMonth primerMes = mesActual.minusMonths(5);
        OffsetDateTime desde = primerMes.atDay(1).atStartOfDay(ZonaNegocio.ZONA).toOffsetDateTime();
        Map<YearMonth, Long> viajesPorMes = viajeRepository.findFechasInicioDesde(desde).stream()
                .collect(Collectors.groupingBy(
                        fecha -> YearMonth.from(fecha.atZoneSameInstant(ZonaNegocio.ZONA)),
                        Collectors.counting()
                ));
        List<ActividadMensualDTO> actividad = new ArrayList<>();
        for (YearMonth mes = primerMes; !mes.isAfter(mesActual); mes = mes.plusMonths(1)) {
            actividad.add(new ActividadMensualDTO(mes.toString(), viajesPorMes.getOrDefault(mes, 0L)));
        }
        return actividad;
    }

    /**
     * Lista las solicitudes de registro con paginación y filtro opcional por estado, de la más reciente a la más antigua.
     */
    @Transactional(readOnly = true)
    public PaginaSolicitudesDTO listarSolicitudes(EstadoSolicitud estado, int page, int size) {
        PaginacionValidator.validar(page, size);
        Sort sort = Sort.by(Sort.Direction.DESC, "fechaRegistro")
                .and(Sort.by(Sort.Direction.DESC, "idLanchero"));
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Lanchero> resultados = estado == null
                ? lancheroRepository.findAll(pageRequest)
                : lancheroRepository.findByEstadoSolicitud(estado, pageRequest);

        List<SolicitudResumenDTO> content = resultados.getContent().stream()
                .map(lanchero -> new SolicitudResumenDTO(
                        lanchero.getIdLanchero(),
                        lanchero.getNombreCompleto(),
                        lanchero.getFechaRegistro().atZoneSameInstant(ZonaNegocio.ZONA).toLocalDate(),
                        lanchero.getEstadoSolicitud().name()
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

    /**
     * Obtiene el detalle de una solicitud, incluida su embarcación, zonas y URLs prefirmadas de los documentos.
     */
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

        List<SolicitudDetalleDTO.DocumentoDTO> documentos =
                documentoRepository.findByLanchero_IdLancheroOrderByIdDocumentoAsc(lanchero.getIdLanchero()).stream()
                        .sorted(Comparator
                                .comparing((Documento documento) -> !"FOTO_LANCHA".equals(documento.getTipoDocumento()))
                                .thenComparing(Documento::getTipoDocumento))
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
                lanchero.getEstadoSolicitud().name(),
                lanchero.getFechaRegistro(),
                lanchero.getFechaDecision(),
                lanchero.getMotivoRechazo(),
                embarcacion,
                documentos
        );
    }
}
