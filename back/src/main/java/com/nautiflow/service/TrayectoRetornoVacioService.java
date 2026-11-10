package com.nautiflow.service;

import com.nautiflow.dto.CrearTramoVacioRequest;
import com.nautiflow.dto.MensajeTramoVacioDTO;
import com.nautiflow.dto.TramoVacioDTO;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.model.EstadoTramoVacio;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.TrayectoRetornoVacio;
import com.nautiflow.model.Viaje;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.TrayectoRetornoVacioRepository;
import com.nautiflow.repository.ViajeRepository;
import com.nautiflow.util.FechaHoraUtc;
import com.nautiflow.util.ZonaNegocio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Publica, consulta y cierra retornos vacíos, manteniendo sincronizado el estado operativo del lanchero.
 */
@Service
public class TrayectoRetornoVacioService {
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final String PREGUNTA_MENSAJE =
            " hs. ¿Alguien necesita un rescate o envío en ese trayecto?";

    private final LancheroRepository lancheroRepository;
    private final ViajeRepository viajeRepository;
    private final TrayectoRetornoVacioRepository trayectoRepository;
    private final Clock clock;

    public TrayectoRetornoVacioService(
            LancheroRepository lancheroRepository,
            ViajeRepository viajeRepository,
            TrayectoRetornoVacioRepository trayectoRepository,
            Clock clock
    ) {
        this.lancheroRepository = lancheroRepository;
        this.viajeRepository = viajeRepository;
        this.trayectoRepository = trayectoRepository;
        this.clock = clock;
    }

    @Transactional
    public TramoVacioDTO publicar(Long viajeId, CrearTramoVacioRequest solicitud) {
        Long idLanchero = viajeRepository.findLancheroIdByIdViaje(viajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado."));
        Lanchero lanchero = obtenerLancheroParaActualizar(idLanchero);
        Viaje viaje = viajeRepository.findWithDetailsByIdViaje(viajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado."));

        validarEstadoCompatible(lanchero, viaje);
        if (viajeRepository.existsByLancha_Lanchero_IdLancheroAndIdViajeNotAndFechaHoraFinIsNull(
                idLanchero,
                viajeId
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El lanchero tiene otro viaje en curso.");
        }
        if (trayectoRepository.existsByViaje_Lancha_Lanchero_IdLancheroAndEstado(
                idLanchero,
                EstadoTramoVacio.ACTIVO
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El lanchero ya tiene un retorno activo.");
        }

        TrayectoRetornoVacio tramo = new TrayectoRetornoVacio();
        tramo.setViaje(viaje);
        tramo.setZonaOrigen(viaje.getZonaDestino());
        tramo.setZonaDestino(viaje.getZonaOrigen());
        LocalDate fechaRetorno = LocalDate.now(clock.withZone(ZonaNegocio.ZONA));
        tramo.setFechaHoraRetorno(
                LocalDateTime.of(fechaRetorno, solicitud.horaRetorno())
                        .atZone(ZonaNegocio.ZONA)
                        .toOffsetDateTime()
        );
        tramo.setTuristaNombreCompleto(normalizar(solicitud.turistaNombreCompleto()));
        tramo.setTuristaWhatsapp(normalizar(solicitud.turistaWhatsapp()));
        tramo.setEstado(EstadoTramoVacio.ACTIVO);
        tramo.setFechaCreacion(FechaHoraUtc.ahora(clock));
        tramo = trayectoRepository.saveAndFlush(tramo);

        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_RETORNO);
        lancheroRepository.saveAndFlush(lanchero);
        return toDTO(tramo);
    }

    @Transactional(readOnly = true)
    public List<TramoVacioDTO> listar(LocalDate fechaDesde, LocalDate fechaHasta, String estado) {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaDesde no puede ser posterior a fechaHasta.");
        }
        EstadoTramoVacio estadoFiltro = parsearEstado(estado);
        OffsetDateTime inicio = fechaDesde == null
                ? null
                : fechaDesde.atStartOfDay(ZonaNegocio.ZONA).toOffsetDateTime();
        OffsetDateTime finExclusivo = fechaHasta == null
                ? null
                : fechaHasta.plusDays(1).atStartOfDay(ZonaNegocio.ZONA).toOffsetDateTime();
        return trayectoRepository.buscar(inicio, finExclusivo, estadoFiltro).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MensajeTramoVacioDTO obtenerMensaje(Long id) {
        TrayectoRetornoVacio tramo = trayectoRepository.findWithDetailsByIdTramo(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Retorno vacío no encontrado."));
        return new MensajeTramoVacioDTO(generarMensaje(tramo));
    }

    @Transactional
    public TramoVacioDTO cancelar(Long id) {
        return cerrar(id, false);
    }

    @Transactional
    public TramoVacioDTO finalizar(Long id) {
        return cerrar(id, true);
    }

    private TramoVacioDTO cerrar(Long id, boolean requiereViajeFinalizado) {
        Long idLanchero = trayectoRepository.findLancheroIdByIdTramo(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Retorno vacío no encontrado."));
        Lanchero lanchero = obtenerLancheroParaActualizar(idLanchero);
        TrayectoRetornoVacio tramo = trayectoRepository.findWithDetailsByIdTramo(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Retorno vacío no encontrado."));

        if (tramo.getEstado() != EstadoTramoVacio.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El retorno vacío ya no está activo.");
        }
        if (requiereViajeFinalizado && tramo.getViaje().getFechaHoraFin() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede finalizar el retorno mientras el viaje original siga en curso."
            );
        }

        tramo.setEstado(EstadoTramoVacio.INACTIVO);
        trayectoRepository.saveAndFlush(tramo);
        liberarSiNoTieneCompromisos(lanchero);
        return toDTO(tramo);
    }

    private void liberarSiNoTieneCompromisos(Lanchero lanchero) {
        if (lanchero.getEstadoViaje() != EstadoOperativoLanchero.EN_RETORNO) {
            return;
        }
        if (viajeRepository.existsByLancha_Lanchero_IdLancheroAndFechaHoraFinIsNull(
                lanchero.getIdLanchero()
        )) {
            lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_VIAJE);
        } else if (trayectoRepository.existsByViaje_Lancha_Lanchero_IdLancheroAndEstado(
                lanchero.getIdLanchero(),
                EstadoTramoVacio.ACTIVO
        )) {
            lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_RETORNO);
        } else {
            lanchero.setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        }
        lancheroRepository.saveAndFlush(lanchero);
    }

    private void validarEstadoCompatible(Lanchero lanchero, Viaje viaje) {
        boolean viajeEnCurso = viaje.getFechaHoraFin() == null;
        boolean lancheroEnViaje = lanchero.getEstadoViaje() == EstadoOperativoLanchero.EN_VIAJE;
        boolean lancheroActivo = lanchero.getEstadoViaje() == EstadoOperativoLanchero.ACTIVO;
        if (!(lancheroEnViaje && viajeEnCurso || lancheroActivo && !viajeEnCurso)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El estado del lanchero no es compatible con la publicación del retorno."
            );
        }
    }

    private Lanchero obtenerLancheroParaActualizar(Long id) {
        return lancheroRepository.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchero no encontrado."));
    }

    private EstadoTramoVacio parsearEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return EstadoTramoVacio.ACTIVO;
        }
        try {
            return EstadoTramoVacio.valueOf(estado.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado no válido. Valores permitidos: ACTIVO, INACTIVO."
            );
        }
    }

    private TramoVacioDTO toDTO(TrayectoRetornoVacio tramo) {
        Viaje viaje = tramo.getViaje();
        return new TramoVacioDTO(
                tramo.getIdTramo(),
                viaje.getIdViaje(),
                viaje.getLancha().getNombre(),
                viaje.getLancha().getLanchero().getNombreCompleto(),
                tramo.getZonaOrigen().getNombre(),
                tramo.getZonaDestino().getNombre(),
                tramo.getFecha(),
                tramo.getHoraRetorno(),
                tramo.getTuristaNombreCompleto(),
                tramo.getTuristaWhatsapp(),
                tramo.getEstado().name(),
                generarMensaje(tramo)
        );
    }

    private String generarMensaje(TrayectoRetornoVacio tramo) {
        Viaje viaje = tramo.getViaje();
        return viaje.getLancha().getNombre()
                + " vuelve vacía desde "
                + tramo.getZonaOrigen().getNombre()
                + " a "
                + tramo.getZonaDestino().getNombre()
                + " a las "
                + FORMATO_HORA.format(tramo.getHoraRetorno())
                + PREGUNTA_MENSAJE;
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.strip();
    }
}
