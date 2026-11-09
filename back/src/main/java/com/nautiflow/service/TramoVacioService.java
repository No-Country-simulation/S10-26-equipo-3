package com.nautiflow.service;

import com.nautiflow.dto.CrearTramoVacioRequest;
import com.nautiflow.dto.TramoVacioDTO;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.TrayectoRetornoVacio;
import com.nautiflow.model.Viaje;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TramoVacioService {
    static final String ACTIVO = "ACTIVO";
    static final String CERRADO = "CERRADO";
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final TrayectoRetornoVacioRepository tramoRepository;
    private final ViajeRepository viajeRepository;
    private final Clock clock;

    public TramoVacioService(
            TrayectoRetornoVacioRepository tramoRepository,
            ViajeRepository viajeRepository,
            Clock clock
    ) {
        this.tramoRepository = tramoRepository;
        this.viajeRepository = viajeRepository;
        this.clock = clock;
    }

    @Transactional
    public TramoVacioDTO publicar(Long idViaje, CrearTramoVacioRequest solicitud) {
        Viaje viaje = viajeRepository.findById(idViaje)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado."));
        if (tramoRepository.existsByViaje_IdViajeAndEstado(idViaje, ACTIVO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El viaje ya tiene un retorno vacío publicado.");
        }

        ZonedDateTime inicioViaje = viaje.getFechaHoraInicio().atZoneSameInstant(ZonaNegocio.ZONA);
        ZonedDateTime retorno = LocalDateTime.of(hoy(), solicitud.horaRetorno()).atZone(ZonaNegocio.ZONA);
        if (retorno.isBefore(inicioViaje)) {
            retorno = retorno.plusDays(1);
        }

        TrayectoRetornoVacio tramo = new TrayectoRetornoVacio();
        tramo.setViaje(viaje);
        tramo.setZonaOrigen(viaje.getZonaDestino());
        tramo.setZonaDestino(viaje.getZonaOrigen());
        tramo.setFechaHoraRetorno(FechaHoraUtc.normalizar(retorno.toOffsetDateTime()));
        tramo.setEstado(ACTIVO);
        return toDTO(tramoRepository.saveAndFlush(tramo));
    }

    @Transactional(readOnly = true)
    public List<TramoVacioDTO> listarVigentes() {
        return tramoRepository
                .findByEstadoAndFechaHoraRetornoGreaterThanEqualOrderByFechaHoraRetornoAscIdTramoAsc(
                        ACTIVO,
                        inicioDeHoy()
                )
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void cerrar(Long idTramo) {
        TrayectoRetornoVacio tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramo vacío no encontrado."));
        if (!CERRADO.equals(tramo.getEstado())) {
            tramo.setEstado(CERRADO);
            tramoRepository.save(tramo);
        }
    }

    private LocalDate hoy() {
        return LocalDate.now(clock.withZone(ZonaNegocio.ZONA));
    }

    private OffsetDateTime inicioDeHoy() {
        return hoy().atStartOfDay(ZonaNegocio.ZONA).toOffsetDateTime();
    }

    private TramoVacioDTO toDTO(TrayectoRetornoVacio tramo) {
        Lancha lancha = tramo.getViaje().getLancha();
        String hora = tramo.getFechaHoraRetorno().atZoneSameInstant(ZonaNegocio.ZONA).format(FORMATO_HORA);
        String mensaje = "Lancha " + lancha.getNombre()
                + " vuelve vacía desde " + tramo.getZonaOrigen().getNombre()
                + " a las " + hora
                + ". ¿Alguien necesita un rescate o envío en ese trayecto?";
        return new TramoVacioDTO(
                tramo.getIdTramo(),
                tramo.getViaje().getIdViaje(),
                lancha.getIdLancha(),
                lancha.getNombre(),
                lancha.getCapacidad(),
                tramo.getZonaOrigen().getNombre(),
                tramo.getZonaDestino().getNombre(),
                tramo.getFechaHoraRetorno(),
                mensaje
        );
    }
}
