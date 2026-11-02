package com.nautiflow.service;

import com.nautiflow.dto.CrearViajeRequest;
import com.nautiflow.dto.LanchaDisponibleDTO;
import com.nautiflow.dto.PaginaViajesDTO;
import com.nautiflow.dto.PaginaSeguimientoViajesDTO;
import com.nautiflow.dto.ViajeAdminDTO;
import com.nautiflow.dto.ViajeEnCursoDTO;
import com.nautiflow.dto.ViajeFinalizadoDTO;
import com.nautiflow.dto.ViajeSeguimientoDTO;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Viaje;
import com.nautiflow.model.Zona;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.ViajeRepository;
import com.nautiflow.repository.ZonaRepository;
import com.nautiflow.time.FechaHoraUtc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;

@Service
public class ViajeWorkflowService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Bogota");

    private final LancheroRepository lancheroRepository;
    private final LanchaRepository lanchaRepository;
    private final ViajeRepository viajeRepository;
    private final ZonaRepository zonaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public ViajeWorkflowService(
            LancheroRepository lancheroRepository,
            LanchaRepository lanchaRepository,
            ViajeRepository viajeRepository,
            ZonaRepository zonaRepository,
            ApplicationEventPublisher eventPublisher,
            Clock clock
    ) {
        this.lancheroRepository = lancheroRepository;
        this.lanchaRepository = lanchaRepository;
        this.viajeRepository = viajeRepository;
        this.zonaRepository = zonaRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public ViajeAdminDTO crearEIniciar(CrearViajeRequest solicitud) {
        Long idLanchero = lanchaRepository.findLancheroIdByIdLancha(solicitud.idLancha())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lancha no encontrada."));
        Lanchero lanchero = obtenerLancheroParaActualizar(idLanchero);
        Lancha lancha = lanchaRepository.findById(solicitud.idLancha())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lancha no encontrada."));
        lancha.setLanchero(lanchero);
        validarLancheroDisponible(lanchero);
        validarNoHayViajeEnCurso(idLanchero);
        validarCapacidadYZonas(solicitud, lancha);

        Zona origen = zonaRepository.findById(solicitud.idZonaOrigen())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zona de origen no encontrada."));
        Zona destino = zonaRepository.findById(solicitud.idZonaDestino())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zona de destino no encontrada."));

        Viaje viaje = new Viaje();
        viaje.setLancha(lancha);
        viaje.setZonaOrigen(origen);
        viaje.setZonaDestino(destino);
        viaje.setFechaHoraInicio(FechaHoraUtc.ahora(clock));
        viaje.setCantidadPasajeros(solicitud.cantidadPasajeros());
        viaje.setTuristaNombreCompleto(solicitud.turistaNombreCompleto().strip());
        viaje.setTuristaWhatsapp(solicitud.turistaWhatsapp().strip());
        viaje = viajeRepository.saveAndFlush(viaje);

        lanchero.setEstadoViaje("EN_VIAJE");
        lancheroRepository.save(lanchero);
        eventPublisher.publishEvent(new ViajeProgramadoEvent(
                viaje.getIdViaje(),
                lanchero.getIdLanchero(),
                lanchero.getCorreo(),
                lanchero.getNombreCompleto(),
                origen.getNombre(),
                destino.getNombre(),
                viaje.getFechaHoraInicio().toString(),
                viaje.getCantidadPasajeros(),
                viaje.getTuristaNombreCompleto(),
                viaje.getTuristaWhatsapp()
        ));
        return toDTO(viaje);
    }

    @Transactional(readOnly = true)
    public PaginaViajesDTO listarEnCurso(int page, int size) {
        PaginacionValidator.validar(page, size);
        Sort sort = Sort.by(Sort.Direction.DESC, "fechaHoraInicio")
                .and(Sort.by(Sort.Direction.DESC, "idViaje"));
        Page<ViajeEnCursoDTO> resultados = viajeRepository
                .findByLancha_Lanchero_EstadoViajeAndFechaHoraFinIsNull(
                        "EN_VIAJE",
                        PageRequest.of(page, size, sort)
                )
                .map(this::toViajeEnCursoDTO);
        return new PaginaViajesDTO(
                resultados.getContent(),
                resultados.getNumber(),
                resultados.getSize(),
                resultados.getTotalElements(),
                resultados.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public List<LanchaDisponibleDTO> buscarDisponibles(
            Integer capacidad,
            String zonaOrigen,
            String zonaDestino,
            LocalTime hora
    ) {
        if (hora == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora seleccionada es obligatoria.");
        }
        if (capacidad != null && capacidad < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La capacidad debe ser mayor que cero.");
        }

        Clock clockNegocio = clock.withZone(ZONA_NEGOCIO);
        LocalDateTime fechaHoraLocal = LocalDateTime.of(LocalDate.now(clockNegocio), hora);
        OffsetDateTime fechaHoraConsulta = fechaHoraLocal.atZone(ZONA_NEGOCIO).toOffsetDateTime();
        if (fechaHoraConsulta.isBefore(OffsetDateTime.now(clockNegocio))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora seleccionada ya pasó para la fecha actual.");
        }

        Zona origen = validarZona(zonaOrigen, "origen");
        Zona destino = validarZona(zonaDestino, "destino");
        if (origen != null && destino != null && origen.getIdZona().equals(destino.getIdZona())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El origen y el destino deben ser distintos.");
        }
        return lanchaRepository.buscarDisponibles(
                        capacidad,
                        origen == null ? null : origen.getIdZona(),
                        destino == null ? null : destino.getIdZona(),
                        fechaHoraConsulta
                ).stream()
                .map(this::toLanchaDisponibleDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginaSeguimientoViajesDTO listarSeguimiento(int page, int size) {
        PaginacionValidator.validar(page, size);
        Sort sort = Sort.by(Sort.Direction.DESC, "fechaHoraInicio")
                .and(Sort.by(Sort.Direction.DESC, "idViaje"));
        Page<ViajeSeguimientoDTO> resultados = viajeRepository
                .findAllForSeguimiento(PageRequest.of(page, size, sort))
                .map(this::toViajeSeguimientoDTO);
        return new PaginaSeguimientoViajesDTO(
                resultados.getContent(),
                resultados.getNumber(),
                resultados.getSize(),
                resultados.getTotalElements(),
                resultados.getTotalPages()
        );
    }

    @Transactional
    public ViajeFinalizadoDTO finalizar(Long idViaje) {
        Long idLanchero = viajeRepository.findLancheroIdByIdViaje(idViaje)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado."));
        Lanchero lanchero = obtenerLancheroParaActualizar(idLanchero);
        Viaje viaje = viajeRepository.findById(idViaje)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado."));
        viaje.getLancha().setLanchero(lanchero);
        if (!"EN_VIAJE".equals(lanchero.getEstadoViaje()) || viaje.getFechaHoraFin() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El viaje no está en curso.");
        }

        viaje.setFechaHoraFin(FechaHoraUtc.ahora(clock));
        viaje.setTokenResena(generarTokenResena());
        String mensajeResena = "Hola " + viaje.getTuristaNombreCompleto()
                + ", esperamos que hayas disfrutado tu viaje. Nos gustaría conocer tu experiencia. "
                + "Nos encantaría que dejaras una reseña.";
        if (!viajeRepository.existsByLancha_Lanchero_IdLancheroAndIdViajeNotAndFechaHoraFinIsNull(
                idLanchero,
                idViaje
        )) {
            lanchero.setEstadoViaje("ACTIVO");
        }
        viajeRepository.saveAndFlush(viaje);
        lancheroRepository.save(lanchero);
        return new ViajeFinalizadoDTO(
                viaje.getIdViaje(),
                viaje.getFechaHoraFin(),
                viaje.getTokenResena(),
                viaje.getTuristaNombreCompleto(),
                viaje.getTuristaWhatsapp(),
                mensajeResena
        );
    }

    private Lanchero obtenerLancheroParaActualizar(Long id) {
        return lancheroRepository.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchero no encontrado."));
    }

    private void validarLancheroDisponible(Lanchero lanchero) {
        if (!"APROBADO".equals(lanchero.getEstadoSolicitud()) || !"ACTIVO".equals(lanchero.getEstadoViaje())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La lancha debe estar aprobada y activa para iniciar un viaje."
            );
        }
    }

    private void validarNoHayViajeEnCurso(Long idLanchero) {
        if (viajeRepository.existsByLancha_Lanchero_IdLancheroAndFechaHoraFinIsNull(idLanchero)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El lanchero ya tiene un viaje sin finalizar.");
        }
    }

    private void validarCapacidadYZonas(CrearViajeRequest solicitud, Lancha lancha) {
        if (solicitud.cantidadPasajeros() > lancha.getCapacidad()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad supera la capacidad de la lancha.");
        }
        if (solicitud.idZonaOrigen().equals(solicitud.idZonaDestino())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El origen y el destino deben ser distintos.");
        }
    }

    private String generarTokenResena() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private ViajeAdminDTO toDTO(Viaje viaje) {
        return new ViajeAdminDTO(
                viaje.getIdViaje(),
                viaje.getLancha().getIdLancha(),
                viaje.getLancha().getLanchero().getIdLanchero(),
                viaje.getLancha().getLanchero().getNombreCompleto(),
                viaje.getZonaOrigen().getNombre(),
                viaje.getZonaDestino().getNombre(),
                viaje.getFechaHoraInicio(),
                viaje.getFechaHoraFin(),
                viaje.getCantidadPasajeros(),
                viaje.getTuristaNombreCompleto(),
                viaje.getTuristaWhatsapp()
        );
    }

    private ViajeEnCursoDTO toViajeEnCursoDTO(Viaje viaje) {
        Lanchero lanchero = viaje.getLancha().getLanchero();
        return new ViajeEnCursoDTO(
                viaje.getIdViaje(),
                viaje.getLancha().getIdLancha(),
                lanchero.getIdLanchero(),
                lanchero.getNombreCompleto(),
                viaje.getZonaOrigen().getNombre(),
                viaje.getZonaDestino().getNombre(),
                viaje.getFechaHoraInicio(),
                viaje.getFechaHoraFin(),
                viaje.getCantidadPasajeros(),
                viaje.getTuristaNombreCompleto(),
                viaje.getTuristaWhatsapp()
        );
    }

    private Zona validarZona(String filtro, String tipo) {
        if (filtro == null) {
            return null;
        }
        String filtroNormalizado = filtro.strip();
        if (filtroNormalizado.isEmpty() || filtroNormalizado.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La zona de " + tipo + " debe tener entre 1 y 100 caracteres."
            );
        }
        try {
            long idZona = Long.parseLong(filtroNormalizado);
            if (idZona < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de zona debe ser mayor que cero.");
            }
            return zonaRepository.findById(idZona)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Zona de " + tipo + " no encontrada."
                    ));
        } catch (NumberFormatException exception) {
            return zonaRepository.findByNombreIgnoreCase(filtroNormalizado)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Zona de " + tipo + " no encontrada."
                    ));
        }
    }

    private LanchaDisponibleDTO toLanchaDisponibleDTO(Lancha lancha) {
        Lanchero lanchero = lancha.getLanchero();
        return new LanchaDisponibleDTO(
                lancha.getIdLancha(),
                lancha.getNombre(),
                lancha.getCapacidad(),
                lancha.getZonas().stream().map(Zona::getNombre).sorted().toList(),
                "APROBADO".equals(lanchero.getEstadoSolicitud()),
                lanchero.getEstadoViaje(),
                lanchero.getWhatsapp()
        );
    }

    private ViajeSeguimientoDTO toViajeSeguimientoDTO(Viaje viaje) {
        String ultimaUbicacionConfirmada = viajeRepository
                .findFirstByLancha_IdLanchaAndFechaHoraFinIsNotNullOrderByFechaHoraFinDescIdViajeDesc(
                        viaje.getLancha().getIdLancha()
                )
                .map(ultimoViajeFinalizado -> ultimoViajeFinalizado.getZonaDestino().getNombre())
                .orElse(null);
        return new ViajeSeguimientoDTO(
                viaje.getIdViaje(),
                viaje.getLancha().getNombre(),
                viaje.getZonaOrigen().getNombre(),
                viaje.getZonaDestino().getNombre(),
                viaje.getFechaHoraInicio(),
                viaje.getFechaHoraFin(),
                viaje.getFechaHoraFin() == null ? "EN_VIAJE" : "FINALIZADO",
                ultimaUbicacionConfirmada
        );
    }
}
