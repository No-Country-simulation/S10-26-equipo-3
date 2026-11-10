package com.nautiflow.service;

import com.nautiflow.dto.CrearViajeRequest;
import com.nautiflow.dto.EstadoOperativoRequest;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Viaje;
import com.nautiflow.model.Zona;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.TrayectoRetornoVacioRepository;
import com.nautiflow.repository.ViajeRepository;
import com.nautiflow.repository.ZonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminWorkflowServiceTest {
    @Mock
    private LancheroRepository lancheroRepository;
    @Mock
    private LanchaRepository lanchaRepository;
    @Mock
    private ViajeRepository viajeRepository;
    @Mock
    private TrayectoRetornoVacioRepository trayectoRetornoVacioRepository;
    @Mock
    private ZonaRepository zonaRepository;
    @Mock
    private RegistroNotificacionService notificacionService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    private AdminWorkflowService service;
    private ViajeWorkflowService viajeService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        Clock clock = Clock.systemUTC();
        service = new AdminWorkflowService(lancheroRepository, notificacionService, clock);
        viajeService = new ViajeWorkflowService(
                lancheroRepository,
                lanchaRepository,
                viajeRepository,
                trayectoRetornoVacioRepository,
                zonaRepository,
                eventPublisher,
                clock
        );
    }

    @Test
    void aprobarActivaAlLancheroYNotificaAlCorreoRegistrado() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoViaje(EstadoOperativoLanchero.INACTIVO);
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        var resultado = service.aprobarSolicitud(12L);

        assertEquals("APROBADO", resultado.estado());
        assertEquals(EstadoOperativoLanchero.ACTIVO, lanchero.getEstadoViaje());
        assertNotNull(lanchero.getFechaDecision());
        verify(lancheroRepository).saveAndFlush(lanchero);
        verify(notificacionService).enviarDecisionAprobada(12L, "Ana Pérez", "ana@example.com");
    }

    @Test
    void listadoIncluyeWhatsAppYLanchaDesdeEntidadesYConservaPaginacion() {
        Lanchero lanchero = pendiente();
        lanchero.setWhatsapp("+573001234567");
        Lancha lancha = new Lancha();
        lancha.setNombre("La Aurora");
        lanchero.setLancha(lancha);
        when(lancheroRepository.findByEstadoSolicitudWithLancha(eq(EstadoSolicitud.PENDIENTE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lanchero), PageRequest.of(1, 5), 6));

        var pagina = service.listarLancheros(EstadoSolicitud.PENDIENTE, 1, 5);

        var dto = pagina.content().getFirst();
        assertEquals("+573001234567", dto.whatsapp());
        assertEquals("La Aurora", dto.nombreLancha());
        assertEquals("PENDIENTE", dto.estadoSolicitud());
        assertEquals("INACTIVO", dto.estadoViaje());
        assertEquals(1, pagina.page());
        assertEquals(5, pagina.size());
        assertEquals(6, pagina.totalElements());
        assertEquals(2, pagina.totalPages());
        verify(lancheroRepository).findByEstadoSolicitudWithLancha(eq(EstadoSolicitud.PENDIENTE), any(Pageable.class));
    }

    @Test
    void listadoSinLanchaDevuelveNombreLanchaNulo() {
        Lanchero lanchero = pendiente();
        lanchero.setWhatsapp("3001234567");
        when(lancheroRepository.findAllWithLancha(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lanchero), PageRequest.of(0, 10), 1));

        var pagina = service.listarLancheros(null, 0, 10);

        assertNull(pagina.content().getFirst().nombreLancha());
        assertEquals("3001234567", pagina.content().getFirst().whatsapp());
        verify(lancheroRepository).findAllWithLancha(any(Pageable.class));
    }

    @Test
    void rechazarPersisteElMotivoAntesDeEnviarloAlDestinatarioRegistrado() {
        Lanchero lanchero = pendiente();
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        var resultado = service.rechazarSolicitud(12L, "  Documento ilegible  ");

        assertEquals("Documento ilegible", resultado.motivo());
        assertEquals("Documento ilegible", lanchero.getMotivoRechazo());
        assertEquals(EstadoSolicitud.RECHAZADO, lanchero.getEstadoSolicitud());
        verify(lancheroRepository).saveAndFlush(lanchero);
        verify(notificacionService).enviarDecisionRechazada(
                12L, "Ana Pérez", "ana@example.com", "Documento ilegible"
        );
    }

    @Test
    void aprobarSoloPermiteSolicitudesPendientesYNoModificaSolicitudesDecididas() {
        Lanchero lanchero = pendiente();
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        for (EstadoSolicitud estado : List.of(EstadoSolicitud.APROBADO, EstadoSolicitud.RECHAZADO)) {
            lanchero.setEstadoSolicitud(estado);
            lanchero.setMotivoRechazo("Motivo existente");

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> service.aprobarSolicitud(12L)
            );

            assertEquals(409, exception.getStatusCode().value());
            assertEquals(estado, lanchero.getEstadoSolicitud());
            assertEquals("Motivo existente", lanchero.getMotivoRechazo());
        }

        verify(lancheroRepository, never()).saveAndFlush(any(Lanchero.class));
        verifyNoInteractions(notificacionService);
    }

    @Test
    void rechazarSoloPermiteSolicitudesPendientesYNoModificaSolicitudesDecididas() {
        Lanchero lanchero = pendiente();
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        for (EstadoSolicitud estado : List.of(EstadoSolicitud.RECHAZADO, EstadoSolicitud.APROBADO)) {
            lanchero.setEstadoSolicitud(estado);
            lanchero.setMotivoRechazo("Motivo existente");

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> service.rechazarSolicitud(12L, "Nuevo motivo")
            );

            assertEquals(409, exception.getStatusCode().value());
            assertEquals(estado, lanchero.getEstadoSolicitud());
            assertEquals("Motivo existente", lanchero.getMotivoRechazo());
        }

        verify(lancheroRepository, never()).saveAndFlush(any(Lanchero.class));
        verifyNoInteractions(notificacionService);
    }

    @Test
    void aprobarYRechazarSolicitudInexistenteDevuelven404() {
        when(lancheroRepository.findLockedById(404L)).thenReturn(Optional.empty());

        assertEquals(404, assertThrows(
                ResponseStatusException.class,
                () -> service.aprobarSolicitud(404L)
        ).getStatusCode().value());
        assertEquals(404, assertThrows(
                ResponseStatusException.class,
                () -> service.rechazarSolicitud(404L, "Documento ilegible")
        ).getStatusCode().value());

        verify(lancheroRepository, never()).saveAndFlush(any(Lanchero.class));
        verifyNoInteractions(notificacionService);
    }

    @Test
    void motivoDeRechazoEsOpcionalYSeNormalizaAVacioNulo() {
        Lanchero lanchero = pendiente();
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        var resultado = service.rechazarSolicitud(12L, "  ");

        assertEquals("RECHAZADO", resultado.estado());
        assertNull(resultado.motivo());
        assertNull(lanchero.getMotivoRechazo());
        verify(lancheroRepository).saveAndFlush(lanchero);
        verify(notificacionService).enviarDecisionRechazada(12L, "Ana Pérez", "ana@example.com", null);
    }

    @Test
    void motivoDeRechazoMayorA500CaracteresDevuelve400SinModificarLaSolicitud() {
        assertEquals(400, assertThrows(
                ResponseStatusException.class,
                () -> service.rechazarSolicitud(12L, "a".repeat(501))
        ).getStatusCode().value());

        verifyNoInteractions(lancheroRepository, notificacionService);
    }

    @Test
    void estableceEstadoActivoEnLancheroAprobadoInactivo() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.INACTIVO);
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        var resultado = service.actualizarEstadoOperativo(
                12L,
                new EstadoOperativoRequest(EstadoOperativoRequest.EstadoOperativo.ACTIVO)
        );

        assertEquals("APROBADO", resultado.estadoSolicitud());
        assertEquals("ACTIVO", resultado.estadoViaje());
        verify(lancheroRepository).save(lanchero);
    }

    @Test
    void estableceEstadoInactivoEnLancheroAprobadoActivo() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        var resultado = service.actualizarEstadoOperativo(
                12L,
                new EstadoOperativoRequest(EstadoOperativoRequest.EstadoOperativo.INACTIVO)
        );

        assertEquals("APROBADO", resultado.estadoSolicitud());
        assertEquals("INACTIVO", resultado.estadoViaje());
        verify(lancheroRepository).save(lanchero);
    }

    @Test
    void establecerElEstadoOperativoActualNoLoAlterna() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        var resultado = service.actualizarEstadoOperativo(
                12L,
                new EstadoOperativoRequest(EstadoOperativoRequest.EstadoOperativo.ACTIVO)
        );

        assertEquals("ACTIVO", resultado.estadoViaje());
        assertEquals(EstadoOperativoLanchero.ACTIVO, lanchero.getEstadoViaje());
    }

    @Test
    void noPermiteCambiarEstadoOperativoDeLancheroEnViaje() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_VIAJE);
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizarEstadoOperativo(
                        12L,
                        new EstadoOperativoRequest(EstadoOperativoRequest.EstadoOperativo.INACTIVO)
                )
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals(EstadoOperativoLanchero.EN_VIAJE, lanchero.getEstadoViaje());
        verify(lancheroRepository, never()).save(lanchero);
    }

    @Test
    void noPermiteCambiarEstadoOperativoDeLancheroConRetornoComprometido() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_RETORNO);
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizarEstadoOperativo(
                        12L,
                        new EstadoOperativoRequest(EstadoOperativoRequest.EstadoOperativo.INACTIVO)
                )
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals(EstadoOperativoLanchero.EN_RETORNO, lanchero.getEstadoViaje());
        verify(lancheroRepository, never()).save(lanchero);
    }

    @Test
    void crearViajeValidoIniciaViajeYActualizaSoloEstadoOperativo() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        Lancha lancha = new Lancha();
        lancha.setIdLancha(34L);
        lancha.setLanchero(lanchero);
        lancha.setCapacidad(8);
        Zona origen = zona(2L, "Primera Sección");
        Zona destino = zona(3L, "Segunda Sección");
        lancha.setZonas(new HashSet<>(List.of(origen, destino)));
        when(lanchaRepository.findLancheroIdByIdLancha(34L)).thenReturn(Optional.of(12L));
        when(lanchaRepository.findById(34L)).thenReturn(Optional.of(lancha));
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));
        when(zonaRepository.findById(2L)).thenReturn(Optional.of(origen));
        when(zonaRepository.findById(3L)).thenReturn(Optional.of(destino));
        when(viajeRepository.saveAndFlush(any(Viaje.class))).thenAnswer(invocation -> {
            Viaje viaje = invocation.getArgument(0);
            viaje.setIdViaje(25L);
            return viaje;
        });

        var resultado = viajeService.crearEIniciar(
                new CrearViajeRequest(34L, 2L, 3L, 4, "Turista", "5491123456789")
        );

        assertEquals(25L, resultado.id());
        assertEquals("5491123456789", resultado.turistaWhatsapp());
        assertEquals(EstadoOperativoLanchero.EN_VIAJE, lanchero.getEstadoViaje());
        assertEquals(EstadoSolicitud.APROBADO, lanchero.getEstadoSolicitud());
        ArgumentCaptor<Viaje> captor = ArgumentCaptor.forClass(Viaje.class);
        verify(viajeRepository).saveAndFlush(captor.capture());
        assertNotNull(captor.getValue().getFechaHoraInicio());
        verify(eventPublisher).publishEvent(new ViajeProgramadoEvent(
                25L,
                12L,
                "ana@example.com",
                "Ana Pérez",
                "Primera Sección",
                "Segunda Sección",
                captor.getValue().getFechaHoraInicio(),
                4,
                "Turista",
                "5491123456789"
        ));
    }

    @Test
    void noPermiteCrearViajeSiLaLanchaNoEstaActiva() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.INACTIVO);
        Lancha lancha = new Lancha();
        lancha.setLanchero(lanchero);
        when(lanchaRepository.findLancheroIdByIdLancha(34L)).thenReturn(Optional.of(12L));
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));
        when(lanchaRepository.findById(34L)).thenReturn(Optional.of(lancha));

        assertEquals(409, assertThrows(
                ResponseStatusException.class,
                () -> viajeService.crearEIniciar(
                        new CrearViajeRequest(34L, 2L, 3L, 2, "Turista", "5491123456789")
                )
        ).getStatusCode().value());
        verify(viajeRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void noNotificaSiFallaLaPersistenciaDelEstadoDelLanchero() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        Lancha lancha = new Lancha();
        lancha.setLanchero(lanchero);
        lancha.setCapacidad(8);
        Zona origen = zona(2L, "Origen");
        Zona destino = zona(3L, "Destino");
        lancha.setZonas(new HashSet<>(List.of(origen, destino)));
        when(lanchaRepository.findLancheroIdByIdLancha(34L)).thenReturn(Optional.of(12L));
        when(lanchaRepository.findById(34L)).thenReturn(Optional.of(lancha));
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));
        when(zonaRepository.findById(2L)).thenReturn(Optional.of(origen));
        when(zonaRepository.findById(3L)).thenReturn(Optional.of(destino));
        when(viajeRepository.saveAndFlush(any(Viaje.class))).thenAnswer(invocation -> {
            Viaje viaje = invocation.getArgument(0);
            viaje.setIdViaje(25L);
            return viaje;
        });
        doThrow(new IllegalStateException("database failure"))
                .when(lancheroRepository).save(lanchero);

        assertThrows(IllegalStateException.class, () -> viajeService.crearEIniciar(
                new CrearViajeRequest(34L, 2L, 3L, 2, "Turista", "5491123456789")
        ));

        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void finalizarViajeGeneraTokenYLiberaLanchero() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_VIAJE);
        Lancha lancha = new Lancha();
        lancha.setLanchero(lanchero);
        Viaje viaje = new Viaje();
        viaje.setIdViaje(25L);
        viaje.setLancha(lancha);
        viaje.setTuristaNombreCompleto("Turista");
        viaje.setTuristaWhatsapp("5491123456789");
        when(viajeRepository.findLancheroIdByIdViaje(25L)).thenReturn(Optional.of(12L));
        when(viajeRepository.findById(25L)).thenReturn(Optional.of(viaje));
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));
        var resultado = viajeService.finalizar(25L);

        assertEquals(EstadoOperativoLanchero.ACTIVO, lanchero.getEstadoViaje());
        assertNotNull(resultado.fechaHoraFin());
        assertEquals(viaje.getTokenResena(), resultado.tokenResena());
        assertEquals(64, resultado.tokenResena().length());
        assertEquals("Turista", resultado.nombreUsuario());
        assertEquals("5491123456789", resultado.whatsappUsuario());
        assertEquals(
                "Hola Turista, esperamos que hayas disfrutado tu viaje. Nos gustaría conocer tu experiencia. "
                        + "Nos encantaría que dejaras una reseña.",
                resultado.mensajeResena()
        );
        verify(viajeRepository).saveAndFlush(viaje);
    }

    @Test
    void finalizarViajeConRetornoPublicadoConservaEstadoEnRetorno() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_RETORNO);
        Lancha lancha = new Lancha();
        lancha.setLanchero(lanchero);
        Viaje viaje = new Viaje();
        viaje.setIdViaje(25L);
        viaje.setLancha(lancha);
        viaje.setTuristaNombreCompleto("Turista");
        viaje.setTuristaWhatsapp("5491123456789");
        when(viajeRepository.findLancheroIdByIdViaje(25L)).thenReturn(Optional.of(12L));
        when(viajeRepository.findById(25L)).thenReturn(Optional.of(viaje));
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));
        when(trayectoRetornoVacioRepository.existsByViaje_IdViajeAndEstado(
                25L,
                com.nautiflow.model.EstadoTramoVacio.ACTIVO
        )).thenReturn(true);
        when(viajeRepository.existsByLancha_Lanchero_IdLancheroAndIdViajeNotAndFechaHoraFinIsNull(12L, 25L))
                .thenReturn(false);
        when(trayectoRetornoVacioRepository.existsByViaje_Lancha_Lanchero_IdLancheroAndEstado(
                12L,
                com.nautiflow.model.EstadoTramoVacio.ACTIVO
        )).thenReturn(true);

        viajeService.finalizar(25L);

        assertEquals(EstadoOperativoLanchero.EN_RETORNO, lanchero.getEstadoViaje());
        verify(viajeRepository).saveAndFlush(viaje);
        verify(lancheroRepository).save(lanchero);
    }

    @Test
    void listaViajesIncluyeWhatsappDelTuristaYConservaPaginacion() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_RETORNO);
        Lancha lancha = new Lancha();
        lancha.setIdLancha(34L);
        lancha.setLanchero(lanchero);
        Zona origen = zona(2L, "Primera Sección");
        Zona destino = zona(3L, "Segunda Sección");
        Viaje viaje = new Viaje();
        viaje.setIdViaje(25L);
        viaje.setLancha(lancha);
        viaje.setZonaOrigen(origen);
        viaje.setZonaDestino(destino);
        viaje.setFechaHoraInicio(java.time.OffsetDateTime.parse("2026-11-02T03:37:41.082632Z"));
        viaje.setCantidadPasajeros(2);
        viaje.setTuristaNombreCompleto("Ana Pérez");
        viaje.setTuristaWhatsapp("+573001234567");
        when(viajeRepository.findByFechaHoraFinIsNull(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(viaje), PageRequest.of(0, 2), 3));

        var resultado = viajeService.listarEnCurso(0, 2);

        assertEquals(0, resultado.page());
        assertEquals(2, resultado.size());
        assertEquals(3, resultado.totalElements());
        assertEquals(2, resultado.totalPages());
        var viajeDTO = resultado.content().getFirst();
        assertEquals(25L, viajeDTO.id());
        assertEquals(34L, viajeDTO.idLancha());
        assertEquals(12L, viajeDTO.idLanchero());
        assertEquals("Ana Pérez", viajeDTO.turistaNombreCompleto());
        assertEquals("+573001234567", viajeDTO.turistaWhatsapp());
        assertInstanceOf(String.class, viajeDTO.turistaWhatsapp());
        assertEquals("Primera Sección", viajeDTO.zonaOrigen());
        assertEquals("Segunda Sección", viajeDTO.zonaDestino());
        assertEquals(2, viajeDTO.cantidadPasajeros());

        verify(viajeRepository).findByFechaHoraFinIsNull(any(Pageable.class));
    }

    @Test
    void buscarDisponiblesConstruyeLaFechaActualConLaHoraYFiltraLasZonasPedidas() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T22:06:00Z"), ZoneOffset.UTC);
        ViajeWorkflowService servicio = new ViajeWorkflowService(
                lancheroRepository,
                lanchaRepository,
                viajeRepository,
                trayectoRetornoVacioRepository,
                zonaRepository,
                eventPublisher,
                clock
        );
        Zona origen = zona(2L, "Delta");
        Zona destino = zona(3L, "Primera Seccion");
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud(EstadoSolicitud.APROBADO);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        lanchero.setWhatsapp("+573001234567");
        Lancha lancha = new Lancha();
        lancha.setIdLancha(34L);
        lancha.setNombre("Brisa Marina");
        lancha.setCapacidad(8);
        lancha.setLanchero(lanchero);
        lancha.setZonas(new HashSet<>(List.of(origen, destino)));
        when(zonaRepository.findByNombreIgnoreCase("DELTA")).thenReturn(Optional.of(origen));
        when(zonaRepository.findByNombreIgnoreCase("PRIMERA_SECCION")).thenReturn(Optional.of(destino));
        when(lanchaRepository.buscarDisponibles(
                eq(5), eq(2L), eq(3L), eq(true), eq(EstadoSolicitud.APROBADO),
                eq(EstadoOperativoLanchero.ACTIVO), any()
        ))
                .thenReturn(List.of(lancha));

        var resultado = servicio.buscarDisponibles(5, "DELTA", "PRIMERA_SECCION", LocalTime.of(20, 0));

        assertEquals(1, resultado.size());
        assertEquals(34L, resultado.getFirst().id());
        assertEquals("Brisa Marina", resultado.getFirst().nombre());
        assertEquals(8, resultado.getFirst().capacidad());
        assertTrue(resultado.getFirst().verificada());
        assertEquals("ACTIVO", resultado.getFirst().estadoOperativo());
        assertEquals("+573001234567", resultado.getFirst().whatsappLanchero());
        ArgumentCaptor<OffsetDateTime> fechaConsulta = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(lanchaRepository).buscarDisponibles(
                eq(5), eq(2L), eq(3L), eq(true), eq(EstadoSolicitud.APROBADO),
                eq(EstadoOperativoLanchero.ACTIVO), fechaConsulta.capture()
        );
        assertEquals(OffsetDateTime.parse("2026-11-02T20:00:00-03:00"), fechaConsulta.getValue());
        assertEquals(OffsetDateTime.parse("2026-11-02T23:00:00Z").toInstant(), fechaConsulta.getValue().toInstant());
    }

    @Test
    void buscarDisponiblesRechazaCapacidadInvalidaYHoraPasada() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T19:30:00Z"), ZoneOffset.UTC);
        ViajeWorkflowService servicio = new ViajeWorkflowService(
                lancheroRepository,
                lanchaRepository,
                viajeRepository,
                trayectoRetornoVacioRepository,
                zonaRepository,
                eventPublisher,
                clock
        );

        assertEquals(400, assertThrows(
                ResponseStatusException.class,
                () -> servicio.buscarDisponibles(0, null, null, LocalTime.of(15, 0))
        ).getStatusCode().value());
        assertEquals(400, assertThrows(
                ResponseStatusException.class,
                () -> servicio.buscarDisponibles(null, null, null, LocalTime.of(14, 0))
        ).getStatusCode().value());
        verifyNoInteractions(lanchaRepository);
    }

    @Test
    void propagaElErrorRealDeLaConsultaDeDisponibilidad() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T22:06:00Z"), ZoneOffset.UTC);
        ViajeWorkflowService servicio = new ViajeWorkflowService(
                lancheroRepository,
                lanchaRepository,
                viajeRepository,
                trayectoRetornoVacioRepository,
                zonaRepository,
                eventPublisher,
                clock
        );
        DataAccessResourceFailureException errorConsulta =
                new DataAccessResourceFailureException("fallo de consulta");
        when(lanchaRepository.buscarDisponibles(
                eq(null), eq(null), eq(null), eq(false), eq(EstadoSolicitud.APROBADO),
                eq(EstadoOperativoLanchero.ACTIVO), any()
        ))
                .thenThrow(errorConsulta);

        DataAccessResourceFailureException errorPropagado = assertThrows(
                DataAccessResourceFailureException.class,
                () -> servicio.buscarDisponibles(null, null, null, LocalTime.of(23, 0))
        );

        assertSame(errorConsulta, errorPropagado);
    }

    @Test
    void seguimientoIncluyeViajesActivosFinalizadosYDestinoMasRecienteConfirmado() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoViaje(EstadoOperativoLanchero.EN_VIAJE);
        Lancha lancha = new Lancha();
        lancha.setIdLancha(34L);
        lancha.setNombre("Brisa Marina");
        lancha.setLanchero(lanchero);
        Zona origen = zona(2L, "Delta");
        Zona destinoActual = zona(3L, "Primera Seccion");
        Zona destinoAnterior = zona(4L, "Segunda Seccion");
        Viaje activo = new Viaje();
        activo.setIdViaje(26L);
        activo.setLancha(lancha);
        activo.setZonaOrigen(origen);
        activo.setZonaDestino(destinoActual);
        activo.setFechaHoraInicio(OffsetDateTime.parse("2026-11-02T14:00:00Z"));
        Viaje finalizado = new Viaje();
        finalizado.setIdViaje(25L);
        finalizado.setLancha(lancha);
        finalizado.setZonaOrigen(origen);
        finalizado.setZonaDestino(destinoAnterior);
        finalizado.setFechaHoraInicio(OffsetDateTime.parse("2026-11-01T14:00:00Z"));
        finalizado.setFechaHoraFin(OffsetDateTime.parse("2026-11-01T14:30:00Z"));
        when(viajeRepository.findForSeguimiento(eq("TODOS"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activo, finalizado), PageRequest.of(0, 10), 2));
        when(viajeRepository.findFirstByLancha_IdLanchaAndFechaHoraFinIsNotNullOrderByFechaHoraFinDescIdViajeDesc(34L))
                .thenReturn(Optional.of(finalizado));

        var resultado = viajeService.listarSeguimiento(0, 10);

        assertEquals(2, resultado.totalElements());
        assertEquals("EN_VIAJE", resultado.content().getFirst().estado());
        assertNull(resultado.content().getFirst().fechaHoraFin());
        assertEquals("Segunda Seccion", resultado.content().getFirst().ultimaUbicacionConfirmada());
        assertEquals("FINALIZADO", resultado.content().get(1).estado());
        assertEquals("Segunda Seccion", resultado.content().get(1).ultimaUbicacionConfirmada());
        assertEquals(OffsetDateTime.parse("2026-11-01T14:30:00Z"), resultado.content().get(1).fechaHoraFin());
    }

    private Lanchero pendiente() {
        Lanchero lanchero = new Lanchero();
        lanchero.setIdLanchero(12L);
        lanchero.setNombreCompleto("Ana Pérez");
        lanchero.setCorreo("ana@example.com");
        lanchero.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        return lanchero;
    }

    private Zona zona(Long id, String nombre) {
        Zona zona = new Zona();
        zona.setIdZona(id);
        zona.setNombre(nombre);
        return zona;
    }
}
