package com.nautiflow.service;

import com.nautiflow.dto.CrearTramoVacioRequest;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.model.EstadoTramoVacio;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.TrayectoRetornoVacio;
import com.nautiflow.model.Viaje;
import com.nautiflow.model.Zona;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.TrayectoRetornoVacioRepository;
import com.nautiflow.repository.ViajeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrayectoRetornoVacioServiceTest {
    private static final String MENSAJE =
            "Lancha X vuelve vacía desde Zona B a Zona A a las 15:00 hs. ¿Alguien necesita un rescate o envío en ese trayecto?";

    @Mock
    private LancheroRepository lancheroRepository;
    @Mock
    private ViajeRepository viajeRepository;
    @Mock
    private TrayectoRetornoVacioRepository trayectoRepository;

    private TrayectoRetornoVacioService service;
    private Lanchero lanchero;
    private Viaje viaje;
    private TrayectoRetornoVacio tramo;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-09T14:00:00Z"), ZoneOffset.UTC);
        service = new TrayectoRetornoVacioService(
                lancheroRepository,
                viajeRepository,
                trayectoRepository,
                clock
        );
        lanchero = new Lanchero();
        lanchero.setIdLanchero(12L);
        lanchero.setNombreCompleto("Ana Pérez");
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_VIAJE);

        Lancha lancha = new Lancha();
        lancha.setNombre("Lancha X");
        lancha.setLanchero(lanchero);

        Zona origen = new Zona();
        origen.setNombre("Zona A");
        Zona destino = new Zona();
        destino.setNombre("Zona B");
        viaje = new Viaje();
        viaje.setIdViaje(25L);
        viaje.setLancha(lancha);
        viaje.setZonaOrigen(origen);
        viaje.setZonaDestino(destino);
        viaje.setFechaHoraInicio(OffsetDateTime.parse("2026-11-09T12:00:00Z"));
        viaje.setCantidadPasajeros(2);
        viaje.setTuristaNombreCompleto("Turista original");
        viaje.setTuristaWhatsapp("+5491100000000");

        tramo = new TrayectoRetornoVacio();
        tramo.setIdTramo(15L);
        tramo.setViaje(viaje);
        tramo.setZonaOrigen(destino);
        tramo.setZonaDestino(origen);
        tramo.setFechaHoraRetorno(OffsetDateTime.parse("2026-11-09T15:00:00-03:00"));
        tramo.setEstado(EstadoTramoVacio.ACTIVO);
        tramo.setFechaCreacion(OffsetDateTime.parse("2026-11-09T14:00:00Z"));

    }

    @Test
    void publicaRetornoInversoYComprometeAlLanchero() {
        prepararPublicacion();
        prepararPersistenciaDeCierre();
        var resultado = service.publicar(25L, new CrearTramoVacioRequest(LocalTime.of(15, 0), null, null));

        assertEquals("Zona B", resultado.zonaOrigen());
        assertEquals("Zona A", resultado.zonaDestino());
        assertEquals("ACTIVO", resultado.estado());
        assertEquals(MENSAJE, resultado.mensaje());
        assertNull(resultado.turistaNombreCompleto());
        assertNull(resultado.turistaWhatsapp());
        assertEquals(EstadoOperativoLanchero.EN_RETORNO, lanchero.getEstadoViaje());
        verify(lancheroRepository).saveAndFlush(lanchero);
        verify(trayectoRepository).saveAndFlush(any(TrayectoRetornoVacio.class));
    }

    @Test
    void publicaRetornoConInteresadoDistintoAlTuristaOriginal() {
        prepararPublicacion();
        prepararPersistenciaDeCierre();
        var resultado = service.publicar(
                25L,
                new CrearTramoVacioRequest(LocalTime.of(15, 0), "  Pedro Gómez  ", " +5491123456789 ")
        );

        ArgumentCaptor<TrayectoRetornoVacio> captor = ArgumentCaptor.forClass(TrayectoRetornoVacio.class);
        verify(trayectoRepository).saveAndFlush(captor.capture());
        assertEquals("Pedro Gómez", captor.getValue().getTuristaNombreCompleto());
        assertEquals("+5491123456789", captor.getValue().getTuristaWhatsapp());
        assertEquals(LocalDate.of(2026, 11, 9), captor.getValue().getFecha());
        assertNotNull(captor.getValue().getFechaCreacion());
        assertEquals("Pedro Gómez", resultado.turistaNombreCompleto());
        assertEquals("Turista original", viaje.getTuristaNombreCompleto());
    }

    @Test
    void rechazaPublicacionCuandoExisteOtroCompromiso() {
        prepararPublicacion();
        when(trayectoRepository.existsByViaje_Lancha_Lanchero_IdLancheroAndEstado(
                12L,
                EstadoTramoVacio.ACTIVO
        )).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.publicar(25L, new CrearTramoVacioRequest(LocalTime.of(15, 0), null, null))
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(EstadoOperativoLanchero.EN_VIAJE, lanchero.getEstadoViaje());
        verify(trayectoRepository, never()).saveAndFlush(any(TrayectoRetornoVacio.class));
        verify(lancheroRepository, never()).saveAndFlush(lanchero);
    }

    @Test
    void listaConFiltroEstadoYFecha() {
        when(trayectoRepository.buscar(
                OffsetDateTime.parse("2026-11-01T00:00:00-03:00"),
                OffsetDateTime.parse("2026-12-01T00:00:00-03:00"),
                EstadoTramoVacio.INACTIVO
        )).thenReturn(List.of(tramo));

        var resultados = service.listar(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30),
                "inactivo"
        );

        assertEquals(1, resultados.size());
        assertEquals("Zona B", resultados.getFirst().zonaOrigen());
        verify(trayectoRepository).buscar(
                OffsetDateTime.parse("2026-11-01T00:00:00-03:00"),
                OffsetDateTime.parse("2026-12-01T00:00:00-03:00"),
                EstadoTramoVacio.INACTIVO
        );
    }

    @Test
    void porDefectoConsultaSoloRetornosActivos() {
        when(trayectoRepository.buscar(null, null, EstadoTramoVacio.ACTIVO)).thenReturn(List.of());

        assertEquals(List.of(), service.listar(null, null, null));

        verify(trayectoRepository).buscar(null, null, EstadoTramoVacio.ACTIVO);
    }

    @Test
    void rechazaFiltrosInvalidos() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(
                ResponseStatusException.class,
                () -> service.listar(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1), null)
        ).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(
                ResponseStatusException.class,
                () -> service.listar(null, null, "CERRADO")
        ).getStatusCode());
    }

    @Test
    void recuperaMensajeGeneradoParaCompartir() {
        when(trayectoRepository.findWithDetailsByIdTramo(15L)).thenReturn(Optional.of(tramo));

        assertEquals(MENSAJE, service.obtenerMensaje(15L).mensaje());
    }

    @Test
    void cancelarRetornoLoConservaYLiberaLancheroSinOtrosCompromisos() {
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_RETORNO);
        viaje.setFechaHoraFin(OffsetDateTime.parse("2026-11-09T14:30:00Z"));
        prepararCierre();
        prepararPersistenciaDeCierre();
        when(viajeRepository.existsByLancha_Lanchero_IdLancheroAndFechaHoraFinIsNull(12L)).thenReturn(false);
        when(trayectoRepository.existsByViaje_Lancha_Lanchero_IdLancheroAndEstado(
                12L,
                EstadoTramoVacio.ACTIVO
        )).thenReturn(false);

        var resultado = service.cancelar(15L);

        assertEquals("INACTIVO", resultado.estado());
        assertEquals(EstadoOperativoLanchero.ACTIVO, lanchero.getEstadoViaje());
        verify(trayectoRepository).saveAndFlush(tramo);
        verify(lancheroRepository).saveAndFlush(lanchero);
    }

    @Test
    void cancelarNoLiberaLancheroConOtroViajeVigente() {
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_RETORNO);
        prepararCierre();
        prepararPersistenciaDeCierre();
        when(viajeRepository.existsByLancha_Lanchero_IdLancheroAndFechaHoraFinIsNull(12L)).thenReturn(true);

        service.cancelar(15L);

        assertEquals(EstadoOperativoLanchero.EN_VIAJE, lanchero.getEstadoViaje());
        verify(lancheroRepository).saveAndFlush(lanchero);
    }

    @Test
    void finalizarRetornoCompletoLoConservaYLiberaLanchero() {
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_RETORNO);
        viaje.setFechaHoraFin(OffsetDateTime.parse("2026-11-09T14:30:00Z"));
        prepararCierre();
        prepararPersistenciaDeCierre();
        when(viajeRepository.existsByLancha_Lanchero_IdLancheroAndFechaHoraFinIsNull(12L)).thenReturn(false);
        when(trayectoRepository.existsByViaje_Lancha_Lanchero_IdLancheroAndEstado(
                12L,
                EstadoTramoVacio.ACTIVO
        )).thenReturn(false);

        var resultado = service.finalizar(15L);

        assertEquals("INACTIVO", resultado.estado());
        assertEquals(EstadoOperativoLanchero.ACTIVO, lanchero.getEstadoViaje());
        verify(trayectoRepository).saveAndFlush(tramo);
        verify(lancheroRepository).saveAndFlush(lanchero);
    }

    @Test
    void impideCancelarElMismoRetornoDosVeces() {
        tramo.setEstado(EstadoTramoVacio.INACTIVO);
        prepararCierre();

        assertEquals(HttpStatus.CONFLICT, assertThrows(
                ResponseStatusException.class,
                () -> service.cancelar(15L)
        ).getStatusCode());
        verify(trayectoRepository, never()).saveAndFlush(any(TrayectoRetornoVacio.class));
        verify(lancheroRepository, never()).saveAndFlush(any(Lanchero.class));
    }

    @Test
    void finalizarSoloPermiteRetornoConViajeOriginalFinalizado() {
        prepararCierre();
        assertEquals(HttpStatus.CONFLICT, assertThrows(
                ResponseStatusException.class,
                () -> service.finalizar(15L)
        ).getStatusCode());
        verify(trayectoRepository, never()).saveAndFlush(any(TrayectoRetornoVacio.class));
    }

    @Test
    void metodosDeEscrituraSonTransaccionales() throws NoSuchMethodException {
        Method publicar = TrayectoRetornoVacioService.class.getMethod(
                "publicar",
                Long.class,
                CrearTramoVacioRequest.class
        );
        assertNotNull(publicar.getAnnotation(Transactional.class));
        assertNotNull(TrayectoRetornoVacioService.class.getMethod("cancelar", Long.class)
                .getAnnotation(Transactional.class));
        assertNotNull(TrayectoRetornoVacioService.class.getMethod("finalizar", Long.class)
                .getAnnotation(Transactional.class));
    }

    @Test
    void siFallaActualizarElLancheroLaOperacionNoSeCompleta() {
        prepararPublicacion();
        prepararPersistenciaDeCierre();
        doThrow(new IllegalStateException("database failure"))
                .when(lancheroRepository).saveAndFlush(lanchero);

        assertThrows(
                IllegalStateException.class,
                () -> service.publicar(25L, new CrearTramoVacioRequest(LocalTime.of(15, 0), null, null))
        );

        verify(trayectoRepository).saveAndFlush(any(TrayectoRetornoVacio.class));
    }

    private void prepararCierre() {
        when(trayectoRepository.findLancheroIdByIdTramo(15L)).thenReturn(Optional.of(12L));
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));
        when(trayectoRepository.findWithDetailsByIdTramo(15L)).thenReturn(Optional.of(tramo));
    }

    private void prepararPersistenciaDeCierre() {
        when(trayectoRepository.saveAndFlush(any(TrayectoRetornoVacio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void prepararPublicacion() {
        when(viajeRepository.findLancheroIdByIdViaje(25L)).thenReturn(Optional.of(12L));
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));
        when(viajeRepository.findWithDetailsByIdViaje(25L)).thenReturn(Optional.of(viaje));
        when(viajeRepository.existsByLancha_Lanchero_IdLancheroAndIdViajeNotAndFechaHoraFinIsNull(12L, 25L))
                .thenReturn(false);
        when(trayectoRepository.existsByViaje_Lancha_Lanchero_IdLancheroAndEstado(
                12L,
                EstadoTramoVacio.ACTIVO
        )).thenReturn(false);
    }
}
