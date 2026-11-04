package com.nautiflow.service;

import com.nautiflow.dto.ResenaPublicaDTO;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.ResenaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResenasLancheroServiceTest {
    @Mock
    private LancheroRepository lancheroRepository;

    @Mock
    private ResenaRepository resenaRepository;

    @InjectMocks
    private ResenasLancheroService service;

    @Test
    void listaResenasDelLancheroEnOrdenPaginado() {
        var fechaReciente = OffsetDateTime.parse("2026-11-03T18:20:00Z");
        var fechaAntigua = OffsetDateTime.parse("2026-11-01T10:00:00Z");
        var reciente = new ResenaPublicaDTO(25L, 5, "Excelente experiencia.", fechaReciente);
        var antigua = new ResenaPublicaDTO(19L, 4, "Muy buen paseo.", fechaAntigua);

        when(lancheroRepository.existsById(12L)).thenReturn(true);
        when(resenaRepository.findPublicasByLancheroId(12L, PageRequest.of(1, 2)))
                .thenReturn(new PageImpl<>(List.of(reciente, antigua), PageRequest.of(1, 2), 4));

        var resultado = service.listar(12L, 1, 2);

        assertEquals(List.of(reciente, antigua), resultado.content());
        assertEquals(1, resultado.page());
        assertEquals(2, resultado.size());
        assertEquals(4, resultado.totalElements());
        assertEquals(2, resultado.totalPages());
        verify(resenaRepository).findPublicasByLancheroId(12L, PageRequest.of(1, 2));
    }

    @Test
    void lancheroInexistenteDevuelve404SinConsultarResenas() {
        when(lancheroRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.listar(99L, 0, 10)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(resenaRepository, never()).findPublicasByLancheroId(99L, PageRequest.of(0, 10));
    }

    @Test
    void lancheroExistenteSinResenasDevuelvePaginaVacia() {
        when(lancheroRepository.existsById(12L)).thenReturn(true);
        when(resenaRepository.findPublicasByLancheroId(12L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        var resultado = service.listar(12L, 0, 10);

        assertTrue(resultado.content().isEmpty());
        assertEquals(0, resultado.totalElements());
        assertEquals(0, resultado.totalPages());
    }

    @Test
    void rechazaPaginacionInvalidaAntesDeConsultarLaLancha() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.listar(12L, -1, 10)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(lancheroRepository, never()).existsById(12L);
    }

    @Test
    void rechazaUnTamanoDePaginaSuperiorAlMaximoPermitido() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.listar(12L, 0, 101)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(lancheroRepository, never()).existsById(12L);
    }
}
