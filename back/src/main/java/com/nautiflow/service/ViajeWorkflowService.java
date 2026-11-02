package com.nautiflow.service;

import com.nautiflow.dto.CrearViajeRequest;
import com.nautiflow.dto.PaginaViajesDTO;
import com.nautiflow.dto.ViajeAdminDTO;
import com.nautiflow.dto.ViajeFinalizadoDTO;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Viaje;
import com.nautiflow.model.Zona;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.ViajeRepository;
import com.nautiflow.repository.ZonaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;

@Service
public class ViajeWorkflowService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final LancheroRepository lancheroRepository;
    private final LanchaRepository lanchaRepository;
    private final ViajeRepository viajeRepository;
    private final ZonaRepository zonaRepository;

    public ViajeWorkflowService(
            LancheroRepository lancheroRepository,
            LanchaRepository lanchaRepository,
            ViajeRepository viajeRepository,
            ZonaRepository zonaRepository
    ) {
        this.lancheroRepository = lancheroRepository;
        this.lanchaRepository = lanchaRepository;
        this.viajeRepository = viajeRepository;
        this.zonaRepository = zonaRepository;
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
        viaje.setFechaHoraInicio(OffsetDateTime.now());
        viaje.setCantidadPasajeros(solicitud.cantidadPasajeros());
        viaje.setTuristaNombreCompleto(solicitud.turistaNombreCompleto().strip());
        viaje.setTuristaWhatsapp(solicitud.turistaWhatsapp().strip());
        viaje = viajeRepository.saveAndFlush(viaje);

        lanchero.setEstadoViaje("EN_VIAJE");
        lancheroRepository.save(lanchero);
        return toDTO(viaje);
    }

    @Transactional(readOnly = true)
    public PaginaViajesDTO listarEnCurso(int page, int size) {
        PaginacionValidator.validar(page, size);
        Sort sort = Sort.by(Sort.Direction.DESC, "fechaHoraInicio")
                .and(Sort.by(Sort.Direction.DESC, "idViaje"));
        Page<ViajeAdminDTO> resultados = viajeRepository
                .findByLancha_Lanchero_EstadoViajeAndFechaHoraFinIsNull(
                        "EN_VIAJE",
                        PageRequest.of(page, size, sort)
                )
                .map(this::toDTO);
        return new PaginaViajesDTO(
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

        viaje.setFechaHoraFin(OffsetDateTime.now());
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
        Lanchero lanchero = viaje.getLancha().getLanchero();
        return new ViajeAdminDTO(
                viaje.getIdViaje(),
                viaje.getLancha().getIdLancha(),
                lanchero.getIdLanchero(),
                lanchero.getNombreCompleto(),
                viaje.getZonaOrigen().getNombre(),
                viaje.getZonaDestino().getNombre(),
                viaje.getFechaHoraInicio(),
                viaje.getFechaHoraFin(),
                viaje.getCantidadPasajeros(),
                viaje.getTuristaNombreCompleto()
        );
    }
}
