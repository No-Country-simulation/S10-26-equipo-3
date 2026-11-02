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
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
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
        verify(notificacionService).notificarResena(12L, "ana@example.com", 45L, 25L, 5);
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
        verify(notificacionService).notificarResena(12L, "ana@example.com", 46L, 25L, 1);
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
        when(resenaRepository.existsByViaje_IdViaje(25L)).thenReturn(false);

        var resultado = service.obtenerViaje("token-unico");

        assertEquals(25L, resultado.idViaje());
        assertEquals("Pasajera guardada", resultado.nombreUsuario());
        assertEquals("Origen", resultado.zonaOrigen());
        assertEquals("Destino", resultado.zonaDestino());
        assertFalse(resultado.resenaRegistrada());
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
