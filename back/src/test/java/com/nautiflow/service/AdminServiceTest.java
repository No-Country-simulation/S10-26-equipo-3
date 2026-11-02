package com.nautiflow.service;

import com.nautiflow.model.Lanchero;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock
    private LancheroRepository lancheroRepository;
    @Mock
    private LanchaRepository lanchaRepository;
    @Mock
    private ArchivoLancheroStorage archivoStorage;

    @InjectMocks
    private AdminService adminService;

    @Test
    void dashboardCalculaLosTresIndicadoresConConsultasDeConteo() {
        when(lancheroRepository.countByEstadoSolicitud("PENDIENTE")).thenReturn(12L);
        when(lanchaRepository.countByLanchero_EstadoSolicitud("APROBADO")).thenReturn(28L);
        when(lancheroRepository.countByEstadoViaje("EN_VIAJE")).thenReturn(4L);

        var dashboard = adminService.obtenerDashboard();

        assertEquals(12L, dashboard.solicitudesPendientes());
        assertEquals(28L, dashboard.lanchasVerificadas());
        assertEquals(4L, dashboard.viajesActivos());
    }

    @Test
    void listadoSinEstadoConsultaTodosConPaginacionYOrdenDeterminista() {
        when(lancheroRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 5), 0));

        var resultado = adminService.listarSolicitudes(null, 1, 5);

        assertEquals(1, resultado.page());
        assertEquals(5, resultado.size());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(lancheroRepository).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("fechaRegistro").getDirection());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("idLanchero").getDirection());
    }

    @Test
    void listadoPorEstadoUsaConsultaPaginadaEnRepositorio() {
        when(lancheroRepository.findByEstadoSolicitud(eq("APROBADO"), argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 10
        ))).thenReturn(new PageImpl<>(List.of()));

        adminService.listarSolicitudes("APROBADO", 0, 10);

        verify(lancheroRepository).findByEstadoSolicitud(eq("APROBADO"), argThat(pageable ->
                pageable.getSort().getOrderFor("fechaRegistro").getDirection() == Sort.Direction.DESC
        ));
    }
}
