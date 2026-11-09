package com.nautiflow.service;

import com.nautiflow.dto.CrearTramoVacioRequest;
import com.nautiflow.dto.TramoVacioDTO;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.TrayectoRetornoVacio;
import com.nautiflow.model.Viaje;
import com.nautiflow.model.Zona;
import com.nautiflow.repository.TrayectoRetornoVacioRepository;
import com.nautiflow.repository.ViajeRepository;
import com.nautiflow.util.ZonaNegocio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TramoVacioServiceTest {
    @Mock
    private TrayectoRetornoVacioRepository tramoRepository;
    @Mock
    private ViajeRepository viajeRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZonaNegocio.ZONA);

    private TramoVacioService service;

    @BeforeEach
    void setUp() {
        service = new TramoVacioService(tramoRepository, viajeRepository, clock);
    }

    @Test
    void publicaElRetornoDesdeElDestinoDelViajeConElMensajeListoParaCopiar() {
        Viaje viaje = viaje(OffsetDateTime.parse("2026-11-09T13:00:00Z"));
        when(viajeRepository.findById(7L)).thenReturn(Optional.of(viaje));
        when(tramoRepository.existsByViaje_IdViajeAndEstado(7L, "ACTIVO")).thenReturn(false);
        when(tramoRepository.saveAndFlush(any(TrayectoRetornoVacio.class))).thenAnswer(invocation -> {
            TrayectoRetornoVacio tramo = invocation.getArgument(0);
            tramo.setIdTramo(3L);
            return tramo;
        });

        TramoVacioDTO tramo = service.publicar(7L, new CrearTramoVacioRequest(LocalTime.of(18, 30)));

        assertEquals(3L, tramo.id());
        assertEquals("Delta", tramo.zonaOrigen());
        assertEquals("Tigre", tramo.zonaDestino());
        assertEquals(OffsetDateTime.parse("2026-11-09T21:30:00Z"), tramo.fechaHoraRetorno());
        assertEquals(
                "Lancha La Gaviota vuelve vacía desde Delta a las 18:30. ¿Alguien necesita un rescate o envío en ese trayecto?",
                tramo.mensaje()
        );
    }

    @Test
    void siLaHoraEsAnteriorAlInicioDelViajeElRetornoEsAlDiaSiguiente() {
        Viaje viaje = viaje(OffsetDateTime.parse("2026-11-09T13:00:00Z"));
        when(viajeRepository.findById(7L)).thenReturn(Optional.of(viaje));
        when(tramoRepository.saveAndFlush(any(TrayectoRetornoVacio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.publicar(7L, new CrearTramoVacioRequest(LocalTime.of(8, 0)));

        ArgumentCaptor<TrayectoRetornoVacio> captor = ArgumentCaptor.forClass(TrayectoRetornoVacio.class);
        verify(tramoRepository).saveAndFlush(captor.capture());
        assertEquals(OffsetDateTime.parse("2026-11-10T11:00:00Z"), captor.getValue().getFechaHoraRetorno());
    }

    @Test
    void noPermitePublicarDosRetornosActivosParaElMismoViaje() {
        when(viajeRepository.findById(7L)).thenReturn(Optional.of(viaje(OffsetDateTime.parse("2026-11-09T13:00:00Z"))));
        when(tramoRepository.existsByViaje_IdViajeAndEstado(7L, "ACTIVO")).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.publicar(7L, new CrearTramoVacioRequest(LocalTime.of(18, 30)))
        );

        assertEquals(409, error.getStatusCode().value());
        verify(tramoRepository, never()).saveAndFlush(any());
    }

    @Test
    void listaSoloLosTramosActivosDesdeElInicioDelDia() {
        TrayectoRetornoVacio tramo = new TrayectoRetornoVacio();
        Viaje viaje = viaje(OffsetDateTime.parse("2026-11-09T13:00:00Z"));
        tramo.setIdTramo(4L);
        tramo.setViaje(viaje);
        tramo.setZonaOrigen(viaje.getZonaDestino());
        tramo.setZonaDestino(viaje.getZonaOrigen());
        tramo.setFechaHoraRetorno(OffsetDateTime.parse("2026-11-09T21:30:00Z"));
        when(tramoRepository.findByEstadoAndFechaHoraRetornoGreaterThanEqualOrderByFechaHoraRetornoAscIdTramoAsc(
                "ACTIVO",
                OffsetDateTime.parse("2026-11-09T00:00:00-03:00")
        )).thenReturn(List.of(tramo));

        List<TramoVacioDTO> tramos = service.listarVigentes();

        assertEquals(1, tramos.size());
        assertEquals("La Gaviota", tramos.getFirst().nombreEmbarcacion());
    }

    @Test
    void cerrarMarcaElTramoComoCerrado() {
        TrayectoRetornoVacio tramo = new TrayectoRetornoVacio();
        tramo.setIdTramo(4L);
        when(tramoRepository.findById(4L)).thenReturn(Optional.of(tramo));

        service.cerrar(4L);

        assertEquals("CERRADO", tramo.getEstado());
        verify(tramoRepository).save(tramo);
    }

    @Test
    void cerrarUnTramoInexistenteDevuelve404() {
        when(tramoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.cerrar(99L));

        assertEquals(404, error.getStatusCode().value());
    }

    private Viaje viaje(OffsetDateTime inicio) {
        Zona tigre = new Zona();
        tigre.setNombre("Tigre");
        Zona delta = new Zona();
        delta.setNombre("Delta");
        Lancha lancha = new Lancha();
        lancha.setIdLancha(2L);
        lancha.setNombre("La Gaviota");
        lancha.setCapacidad(6);
        Viaje viaje = new Viaje();
        viaje.setIdViaje(7L);
        viaje.setLancha(lancha);
        viaje.setZonaOrigen(tigre);
        viaje.setZonaDestino(delta);
        viaje.setFechaHoraInicio(inicio);
        return viaje;
    }
}
