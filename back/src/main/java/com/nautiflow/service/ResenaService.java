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

/**
 * Servicio de reseñas: consulta el viaje por token, registra la reseña y lista las reseñas para el administrador.
 */
@Service
public class ResenaService {
    private final ViajeRepository viajeRepository;
    private final ResenaRepository resenaRepository;
    private final RegistroNotificacionService notificacionService;

    /**
     * Inyecta los repositorios de viajes y reseñas y el servicio de notificaciones.
     */
    public ResenaService(
            ViajeRepository viajeRepository,
            ResenaRepository resenaRepository,
            RegistroNotificacionService notificacionService
    ) {
        this.viajeRepository = viajeRepository;
        this.resenaRepository = resenaRepository;
        this.notificacionService = notificacionService;
    }

    /**
     * Obtiene los datos del viaje asociado a un token de reseña y si ya tiene una reseña registrada.
     */
    @Transactional
    public ResenaViajeDTO obtenerViaje(String tokenResena) {
        Viaje viaje = obtenerViajePorToken(tokenResena);
        if (viaje.getFechaHoraFin() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace de reseña no válido.");
        }
        var resena = resenaRepository.findByViaje_IdViaje(viaje.getIdViaje());
        return new ResenaViajeDTO(
                viaje.getIdViaje(),
                viaje.getTuristaNombreCompleto(),
                viaje.getZonaOrigen().getNombre(),
                viaje.getZonaDestino().getNombre(),
                viaje.getFechaHoraInicio(),
                viaje.getFechaHoraFin(),
                resena.isPresent(),
                resena.map(this::toResenaTokenDTO).orElse(null)
        );
    }

    /**
     * Crea la reseña de un viaje finalizado y notifica al lanchero.
     */
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
        Double promedio = resenaRepository.promedioPorLancha(viaje.getLancha().getIdLancha());
        notificacionService.notificarResena(
                lanchero.getIdLanchero(),
                lanchero.getCorreo(),
                viaje.getTuristaNombreCompleto(),
                resena.getEstrellas(),
                viaje.getZonaOrigen().getNombre(),
                viaje.getZonaDestino().getNombre(),
                resena.getComentario(),
                promedio == null ? null : Math.round(promedio * 10) / 10.0
        );
        return new ResenaCreadaDTO(
                resena.getIdResena(),
                viaje.getIdViaje(),
                resena.getEstrellas(),
                resena.getComentario(),
                resena.getFecha()
        );
    }

    /**
     * Busca el viaje por su token de reseña o responde 404 si no existe.
     */
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

    /**
     * Lista todas las reseñas con paginación para el panel administrativo.
     */
    @Transactional(readOnly = true)
    public PaginaResenasDTO listar(int page, int size) {
        PaginacionValidator.validar(page, size);
        Page<Resena> resultados = resenaRepository.findAllWithLanchero(PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "idResena"))
        ));
        Page<ResenaAdminDTO> contenido = resultados.map(registro ->
                toResenaAdminDTO(registro, registro.getViaje()));
        return new PaginaResenasDTO(
                contenido.getContent(),
                contenido.getNumber(),
                contenido.getSize(),
                contenido.getTotalElements(),
                contenido.getTotalPages()
        );
    }

    /**
     * Convierte una reseña y su viaje al DTO del panel administrativo.
     */
    private ResenaAdminDTO toResenaAdminDTO(Resena resena, Viaje viaje) {
        Lanchero lanchero = viaje.getLancha().getLanchero();
        return new ResenaAdminDTO(
                resena.getIdResena(),
                viaje.getIdViaje(),
                resena.getEstrellas(),
                resena.getComentario(),
                lanchero.getIdLanchero(),
                lanchero.getNombreCompleto(),
                resena.getFecha(),
                viaje.getTuristaNombreCompleto(),
                viaje.getZonaOrigen().getNombre(),
                viaje.getZonaDestino().getNombre(),
                viaje.getFechaHoraInicio(),
                viaje.getFechaHoraFin()
        );
    }

    /**
     * Convierte una reseña al DTO que se muestra en el enlace público.
     */
    private ResenaTokenDTO toResenaTokenDTO(Resena resena) {
        Lanchero lanchero = resena.getViaje().getLancha().getLanchero();
        return new ResenaTokenDTO(
                resena.getIdResena(),
                resena.getEstrellas(),
                resena.getComentario(),
                lanchero.getIdLanchero(),
                lanchero.getNombreCompleto(),
                resena.getFecha()
        );
    }
}
