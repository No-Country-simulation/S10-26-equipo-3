package com.nautiflow.service;

import com.nautiflow.dto.*;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Resena;
import com.nautiflow.model.Viaje;
import com.nautiflow.repository.ResenaRepository;
import com.nautiflow.repository.ViajeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResenaService {
    private final ViajeRepository viajeRepository;
    private final ResenaRepository resenaRepository;
    private final RegistroNotificacionService notificacionService;

    public ResenaService(
            ViajeRepository viajeRepository,
            ResenaRepository resenaRepository,
            RegistroNotificacionService notificacionService
    ) {
        this.viajeRepository = viajeRepository;
        this.resenaRepository = resenaRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public ResenaViajeDTO obtenerViaje(String tokenResena) {
        Viaje viaje = obtenerViajePorToken(tokenResena);
        if (viaje.getFechaHoraFin() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace de reseña no válido.");
        }
        return new ResenaViajeDTO(
                viaje.getIdViaje(),
                viaje.getTuristaNombreCompleto(),
                viaje.getZonaOrigen().getNombre(),
                viaje.getZonaDestino().getNombre(),
                viaje.getFechaHoraInicio(),
                viaje.getFechaHoraFin(),
                resenaRepository.existsByViaje_IdViaje(viaje.getIdViaje())
        );
    }

    @Transactional
    public ResenaCreadaDTO crear(CrearResenaRequest solicitud, String tokenResena) {
        Viaje viaje = obtenerViajePorToken(tokenResena);
        if (viaje.getFechaHoraFin() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace de reseña no válido.");
        }
        if (resenaRepository.existsByViaje_IdViaje(viaje.getIdViaje())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El viaje ya tiene una reseña.");
        }

        Resena resena = new Resena();
        resena.setViaje(viaje);
        resena.setEstrellas(solicitud.puntuacion());
        resena.setComentario(solicitud.comentario());
        try {
            resena = resenaRepository.saveAndFlush(resena);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El viaje ya tiene una reseña.");
        }

        Lanchero lanchero = viaje.getLancha().getLanchero();
        notificacionService.notificarResena(
                lanchero.getIdLanchero(),
                lanchero.getCorreo(),
                resena.getIdResena(),
                viaje.getIdViaje(),
                resena.getEstrellas()
        );
        return new ResenaCreadaDTO(
                resena.getIdResena(),
                viaje.getIdViaje(),
                resena.getEstrellas(),
                resena.getComentario(),
                resena.getFecha()
        );
    }

    private Viaje obtenerViajePorToken(String tokenResena) {
        if (tokenResena == null || tokenResena.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace de reseña no válido.");
        }
        return viajeRepository.findByTokenResena(tokenResena)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Enlace de reseña no válido."
                ));
    }

    @Transactional(readOnly = true)
    public PaginaResenasDTO listar(int page, int size) {
        PaginacionValidator.validar(page, size);
        Page<Resena> resultados = resenaRepository.findAllWithLanchero(PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "idResena"))
        ));
        Page<ResenaAdminDTO> contenido = resultados.map(resena -> {
            Lanchero lanchero = resena.getViaje().getLancha().getLanchero();
            return new ResenaAdminDTO(
                    resena.getIdResena(),
                    resena.getViaje().getIdViaje(),
                    resena.getEstrellas(),
                    resena.getComentario(),
                    lanchero.getIdLanchero(),
                    lanchero.getNombreCompleto(),
                    resena.getFecha()
            );
        });
        return new PaginaResenasDTO(
                contenido.getContent(),
                contenido.getNumber(),
                contenido.getSize(),
                contenido.getTotalElements(),
                contenido.getTotalPages()
        );
    }
}
