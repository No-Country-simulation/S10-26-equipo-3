package com.nautiflow.service;

import com.nautiflow.dto.CrearViajeRequest;
import com.nautiflow.dto.EstadoOperativoRequest;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Viaje;
import com.nautiflow.model.Zona;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.ViajeRepository;
import com.nautiflow.repository.ZonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminWorkflowServiceTest {
    @Mock private LancheroRepository lancheroRepository;
    @Mock private LanchaRepository lanchaRepository;
    @Mock private ViajeRepository viajeRepository;
    @Mock private ZonaRepository zonaRepository;
    @Mock private RegistroNotificacionService notificacionService;
    private AdminWorkflowService service;
    private ViajeWorkflowService viajeService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new AdminWorkflowService(lancheroRepository, notificacionService);
        viajeService = new ViajeWorkflowService(lancheroRepository, lanchaRepository, viajeRepository, zonaRepository);
    }

    @Test
    void aprobarActivaAlLancheroYNotificaAlCorreoRegistrado() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoViaje("INACTIVO");
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        var resultado = service.aprobarSolicitud(12L);

        assertEquals("APROBADO", resultado.estado());
        assertEquals("ACTIVO", lanchero.getEstadoViaje());
        assertNotNull(lanchero.getFechaDecision());
        verify(lancheroRepository).saveAndFlush(lanchero);
        verify(notificacionService).enviarDecisionAprobada(12L, "Ana Pérez", "ana@example.com");
    }

    @Test
    void rechazarPersisteElMotivoAntesDeEnviarloAlDestinatarioRegistrado() {
        Lanchero lanchero = pendiente();
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        var resultado = service.rechazarSolicitud(12L, "  Documento ilegible  ");

        assertEquals("Documento ilegible", resultado.motivo());
        assertEquals("Documento ilegible", lanchero.getMotivoRechazo());
        assertEquals("RECHAZADO", lanchero.getEstadoSolicitud());
        verify(lancheroRepository).saveAndFlush(lanchero);
        verify(notificacionService).enviarDecisionRechazada(
                12L, "Ana Pérez", "ana@example.com", "Documento ilegible"
        );
    }

    @Test
    void noPermiteDecidirUnaSolicitudYaProcesada() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud("APROBADO");
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.rechazarSolicitud(12L, "Documento ilegible")
        );

        assertEquals(409, exception.getStatusCode().value());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void solicitudInexistenteDevuelve404() {
        when(lancheroRepository.findLockedById(404L)).thenReturn(Optional.empty());

        assertEquals(404, assertThrows(
                ResponseStatusException.class,
                () -> service.aprobarSolicitud(404L)
        ).getStatusCode().value());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void motivoDeRechazoVacioNoSeAcepta() {
        assertEquals(400, assertThrows(
                ResponseStatusException.class,
                () -> service.rechazarSolicitud(12L, " ")
        ).getStatusCode().value());
        verifyNoInteractions(lancheroRepository, notificacionService);
    }

    @Test
    void estableceEstadoActivoEnLancheroAprobadoInactivo() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud("APROBADO");
        lanchero.setEstadoViaje("INACTIVO");
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
        lanchero.setEstadoSolicitud("APROBADO");
        lanchero.setEstadoViaje("ACTIVO");
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
        lanchero.setEstadoSolicitud("APROBADO");
        lanchero.setEstadoViaje("ACTIVO");
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        var resultado = service.actualizarEstadoOperativo(
                12L,
                new EstadoOperativoRequest(EstadoOperativoRequest.EstadoOperativo.ACTIVO)
        );

        assertEquals("ACTIVO", resultado.estadoViaje());
        assertEquals("ACTIVO", lanchero.getEstadoViaje());
    }

    @Test
    void noPermiteCambiarEstadoOperativoDeLancheroEnViaje() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud("APROBADO");
        lanchero.setEstadoViaje("EN_VIAJE");
        when(lancheroRepository.findLockedById(12L)).thenReturn(Optional.of(lanchero));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizarEstadoOperativo(
                        12L,
                        new EstadoOperativoRequest(EstadoOperativoRequest.EstadoOperativo.INACTIVO)
                )
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("EN_VIAJE", lanchero.getEstadoViaje());
        verify(lancheroRepository, never()).save(lanchero);
    }

    @Test
    void crearViajeValidoIniciaViajeYActualizaSoloEstadoOperativo() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud("APROBADO");
        lanchero.setEstadoViaje("ACTIVO");
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
        assertEquals("EN_VIAJE", lanchero.getEstadoViaje());
        assertEquals("APROBADO", lanchero.getEstadoSolicitud());
        ArgumentCaptor<Viaje> captor = ArgumentCaptor.forClass(Viaje.class);
        verify(viajeRepository).saveAndFlush(captor.capture());
        assertNotNull(captor.getValue().getFechaHoraInicio());
    }

    @Test
    void noPermiteCrearViajeSiLaLanchaNoEstaActiva() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoSolicitud("APROBADO");
        lanchero.setEstadoViaje("INACTIVO");
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
    }

    @Test
    void finalizarViajeGeneraTokenYLiberaLanchero() {
        Lanchero lanchero = pendiente();
        lanchero.setEstadoViaje("EN_VIAJE");
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

        assertEquals("ACTIVO", lanchero.getEstadoViaje());
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
    void listaViajesFiltrandoPorEstadoOperativoDelLanchero() {
        when(viajeRepository.findByLancha_Lanchero_EstadoViajeAndFechaHoraFinIsNull(
                eq("EN_VIAJE"), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        assertEquals(0, viajeService.listarEnCurso(0, 10).totalElements());
        verify(viajeRepository).findByLancha_Lanchero_EstadoViajeAndFechaHoraFinIsNull(
                eq("EN_VIAJE"), any(Pageable.class)
        );
    }

    private Lanchero pendiente() {
        Lanchero lanchero = new Lanchero();
        lanchero.setIdLanchero(12L);
        lanchero.setNombreCompleto("Ana Pérez");
        lanchero.setCorreo("ana@example.com");
        lanchero.setEstadoSolicitud("PENDIENTE");
        return lanchero;
    }

    private Zona zona(Long id, String nombre) {
        Zona zona = new Zona();
        zona.setIdZona(id);
        zona.setNombre(nombre);
        return zona;
    }
}
