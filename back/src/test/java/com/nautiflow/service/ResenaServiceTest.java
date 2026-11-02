package com.nautiflow.service;

import com.nautiflow.dto.CrearResenaRequest;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Resena;
import com.nautiflow.model.Viaje;
import com.nautiflow.repository.ResenaRepository;
import com.nautiflow.repository.ViajeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {
    @Mock private ViajeRepository viajeRepository;
    @Mock private ResenaRepository resenaRepository;
    @Mock private RegistroNotificacionService notificacionService;

    @InjectMocks
    private ResenaService service;

    @Test
    void pasajeroConTokenPuedeCrearResenaYNotificaAlLanchero() {
        Viaje viaje = viajeTerminado();
        when(viajeRepository.findByTokenResena("token-unico")).thenReturn(Optional.of(viaje));
        when(resenaRepository.existsByViaje_IdViaje(25L)).thenReturn(false);
        when(resenaRepository.saveAndFlush(any(Resena.class))).thenAnswer(invocation -> {
            Resena resena = invocation.getArgument(0);
            resena.setIdResena(45L);
            return resena;
        });

        var resultado = service.crear(new CrearResenaRequest(5, "Excelente"), "token-unico");

        assertEquals(5, resultado.puntuacion());
        assertEquals(25L, resultado.idViaje());
        ArgumentCaptor<Resena> captor = ArgumentCaptor.forClass(Resena.class);
        verify(resenaRepository).saveAndFlush(captor.capture());
        assertEquals(5, captor.getValue().getEstrellas());
        verify(notificacionService).notificarResena(
                12L, "ana@example.com", "Ana Pérez", 5, "Zona origen", "Zona destino", "Excelente"
        );
    }

    @Test
    void permiteUnaEstrellaYComentarioAusente() {
        when(viajeRepository.findByTokenResena("token-unico")).thenReturn(Optional.of(viajeTerminado()));
        when(resenaRepository.existsByViaje_IdViaje(25L)).thenReturn(false);
        when(resenaRepository.saveAndFlush(any(Resena.class))).thenAnswer(invocation -> {
            Resena resena = invocation.getArgument(0);
            resena.setIdResena(46L);
            return resena;
        });

        var resultado = service.crear(new CrearResenaRequest(1, null), "token-unico");

        assertEquals(1, resultado.puntuacion());
        assertNull(resultado.comentario());
        verify(notificacionService).notificarResena(
                12L, "ana@example.com", "Ana Pérez", 1, "Zona origen", "Zona destino", null
        );
    }

    @Test
    void tokenAusenteOAjenoNoAutoriza() {
        assertEquals(404, assertThrows(
                ResponseStatusException.class,
                () -> service.crear(new CrearResenaRequest(1, null), null)
        ).getStatusCode().value());

        when(viajeRepository.findByTokenResena("token-ajeno")).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(
                ResponseStatusException.class,
                () -> service.crear(new CrearResenaRequest(1, null), "token-ajeno")
        ).getStatusCode().value());
        verifyNoInteractions(resenaRepository, notificacionService);
    }

    @Test
    void viajeSoloPuedeTenerUnaResena() {
        when(viajeRepository.findByTokenResena("token-unico")).thenReturn(Optional.of(viajeTerminado()));
        when(resenaRepository.existsByViaje_IdViaje(25L)).thenReturn(true);

        assertEquals(409, assertThrows(
                ResponseStatusException.class,
                () -> service.crear(new CrearResenaRequest(4, null), "token-unico")
        ).getStatusCode().value());
        verify(resenaRepository, never()).saveAndFlush(any());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void obtieneLosDatosDelUsuarioDesdeLaReservaSinDevolverSuWhatsapp() {
        Viaje viaje = viajeTerminado();
        viaje.setTuristaNombreCompleto("Pasajera guardada");
        viaje.setZonaOrigen(zona(1L, "Origen"));
        viaje.setZonaDestino(zona(2L, "Destino"));
        when(viajeRepository.findByTokenResena("token-unico")).thenReturn(Optional.of(viaje));
        when(resenaRepository.findByViaje_IdViaje(25L)).thenReturn(Optional.empty());

        var resultado = service.obtenerViaje("token-unico");

        assertEquals(25L, resultado.idViaje());
        assertEquals("Pasajera guardada", resultado.nombreUsuario());
        assertEquals("Origen", resultado.zonaOrigen());
        assertEquals("Destino", resultado.zonaDestino());
        assertFalse(resultado.resenaRegistrada());
        assertNull(resultado.resena());
    }

    @Test
    void consultarTokenIncluyeLosDatosCompletosDeLaResenaExistente() {
        Viaje viaje = viajeTerminado();
        OffsetDateTime inicio = OffsetDateTime.parse("2026-11-02T10:00:00Z");
        OffsetDateTime fin = OffsetDateTime.parse("2026-11-02T11:00:00Z");
        OffsetDateTime fechaResena = OffsetDateTime.parse("2026-11-02T11:05:00Z");
        viaje.setFechaHoraInicio(inicio);
        viaje.setFechaHoraFin(fin);
        viaje.getLancha().getLanchero().setNombreCompleto("María Pérez");

        Resena resena = new Resena();
        resena.setIdResena(45L);
        resena.setViaje(viaje);
        resena.setEstrellas(5);
        resena.setComentario("Excelente servicio");
        resena.setFecha(fechaResena);
        when(viajeRepository.findByTokenResena("token-unico")).thenReturn(Optional.of(viaje));
        when(resenaRepository.findByViaje_IdViaje(25L)).thenReturn(Optional.of(resena));

        var resultado = service.obtenerViaje("token-unico");

        assertTrue(resultado.resenaRegistrada());
        assertEquals(45L, resultado.resena().idResena());
        assertEquals(5, resultado.resena().puntuacion());
        assertEquals("Excelente servicio", resultado.resena().comentario());
        assertEquals(12L, resultado.resena().idLanchero());
        assertEquals("María Pérez", resultado.resena().nombreLanchero());
        assertEquals(fechaResena, resultado.resena().fecha());
    }

    @Test
    void listaResenasConContextoDeViajeYConservaPaginacion() {
        Viaje viaje = viajeTerminado();
        OffsetDateTime inicio = OffsetDateTime.parse("2026-11-02T10:00:00Z");
        OffsetDateTime fin = OffsetDateTime.parse("2026-11-02T11:00:00Z");
        viaje.setFechaHoraInicio(inicio);
        viaje.setFechaHoraFin(fin);
        Resena resena = new Resena();
        resena.setIdResena(45L);
        resena.setViaje(viaje);
        resena.setEstrellas(5);
        resena.setComentario("Excelente servicio");
        OffsetDateTime fechaResena = OffsetDateTime.parse("2026-11-02T11:05:00Z");
        resena.setFecha(fechaResena);

        when(resenaRepository.findAllWithLanchero(any())).thenReturn(
                new PageImpl<>(List.of(resena), PageRequest.of(0, 10), 11)
        );

        var resultado = service.listar(0, 10);

        var dto = resultado.content().getFirst();
        assertEquals(0, resultado.page());
        assertEquals(10, resultado.size());
        assertEquals(11, resultado.totalElements());
        assertEquals(2, resultado.totalPages());
        assertEquals(45L, dto.idResena());
        assertEquals(25L, dto.idViaje());
        assertEquals(5, dto.puntuacion());
        assertEquals("Excelente servicio", dto.comentario());
        assertEquals(12L, dto.idLanchero());
        assertEquals("Ana Pérez", dto.turistaNombreCompleto());
        assertEquals("Zona origen", dto.zonaOrigen());
        assertEquals("Zona destino", dto.zonaDestino());
        assertEquals(fechaResena, dto.fecha());
        assertEquals(inicio, dto.fechaHoraInicio());
        assertEquals(fin, dto.fechaHoraFin());
    }

    @Test
    void tokenInvalidoNoPermiteConsultarLaReserva() {
        when(viajeRepository.findByTokenResena("invalido")).thenReturn(Optional.empty());

        assertEquals(404, assertThrows(
                ResponseStatusException.class,
                () -> service.obtenerViaje("invalido")
        ).getStatusCode().value());
    }

    private Viaje viajeTerminado() {
        Lanchero lanchero = new Lanchero();
        lanchero.setIdLanchero(12L);
        lanchero.setCorreo("ana@example.com");
        Lancha lancha = new Lancha();
        lancha.setLanchero(lanchero);
        Viaje viaje = new Viaje();
        viaje.setIdViaje(25L);
        viaje.setTokenResena("token-unico");
        viaje.setFechaHoraFin(OffsetDateTime.now());
        viaje.setTuristaNombreCompleto("Ana Pérez");
        viaje.setTuristaWhatsapp("5491123456789");
        viaje.setZonaOrigen(zona(1L, "Zona origen"));
        viaje.setZonaDestino(zona(2L, "Zona destino"));
        viaje.setLancha(lancha);
        return viaje;
    }

    private com.nautiflow.model.Zona zona(Long id, String nombre) {
        com.nautiflow.model.Zona zona = new com.nautiflow.model.Zona();
        zona.setIdZona(id);
        zona.setNombre(nombre);
        return zona;
    }
}
