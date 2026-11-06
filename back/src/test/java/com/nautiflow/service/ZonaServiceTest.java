package com.nautiflow.service;

import com.nautiflow.model.Zona;
import com.nautiflow.repository.ZonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZonaServiceTest {
    @Mock
    private ZonaRepository zonaRepository;

    @Test
    void listaSoloIdYNombresDeZonasOrdenadasPorNombre() {
        Zona centro = zona(3L, "Zona Centro");
        Zona norte = zona(1L, "Zona Norte");
        when(zonaRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"))).thenReturn(List.of(centro, norte));

        assertEquals(
                List.of(new com.nautiflow.dto.ZonaDTO(3L, "Zona Centro"),
                        new com.nautiflow.dto.ZonaDTO(1L, "Zona Norte")),
                new ZonaService(zonaRepository).listar()
        );
        verify(zonaRepository).findAll(Sort.by(Sort.Direction.ASC, "nombre"));
    }

    private Zona zona(Long id, String nombre) {
        Zona zona = new Zona();
        zona.setIdZona(id);
        zona.setNombre(nombre);
        return zona;
    }
}
