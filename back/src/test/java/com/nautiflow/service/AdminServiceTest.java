package com.nautiflow.service;

import com.nautiflow.dto.SolicitudDetalleDTO;
import com.nautiflow.model.Documento;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Zona;
import com.nautiflow.repository.DocumentoRepository;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private DocumentoRepository documentoRepository;
    @Mock
    private ArchivoLancheroStorage archivoStorage;

    @InjectMocks
    private AdminService adminService;

    @Test
    void dashboardCalculaLosTresIndicadoresConConsultasDeConteo() {
        when(lancheroRepository.countByEstadoSolicitud(EstadoSolicitud.PENDIENTE)).thenReturn(12L);
        when(lanchaRepository.countByLanchero_EstadoSolicitud(EstadoSolicitud.APROBADO)).thenReturn(28L);
        when(lancheroRepository.countByEstadoViaje(EstadoOperativoLanchero.EN_VIAJE)).thenReturn(4L);

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
        when(lancheroRepository.findByEstadoSolicitud(eq(EstadoSolicitud.APROBADO), argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 10
        ))).thenReturn(new PageImpl<>(List.of()));

        adminService.listarSolicitudes(EstadoSolicitud.APROBADO, 0, 10);

        verify(lancheroRepository).findByEstadoSolicitud(eq(EstadoSolicitud.APROBADO), argThat(pageable ->
                pageable.getSort().getOrderFor("fechaRegistro").getDirection() == Sort.Direction.DESC
        ));
    }

    @Test
    void fechaRegistroDeSolicitudSeCalculaEnUtcCercaDeMedianoche() {
        Lanchero lanchero = new Lanchero();
        lanchero.setIdLanchero(12L);
        lanchero.setNombreCompleto("Ana Pérez");
        lanchero.setEstadoSolicitud(EstadoSolicitud.PENDIENTE);
        lanchero.setFechaRegistro(OffsetDateTime.parse("2026-11-02T00:30:00+05:00"));
        when(lancheroRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lanchero), PageRequest.of(0, 10), 1));

        var resultado = adminService.listarSolicitudes(null, 0, 10);

        assertEquals(LocalDate.of(2026, 11, 1), resultado.content().getFirst().fechaRegistro());
    }

    @Test
    void detalleCargaDocumentosSeparadosYConservaDocumentosDelMismoTipo() {
        Lanchero lanchero = new Lanchero();
        lanchero.setIdLanchero(12L);
        lanchero.setNombreCompleto("Ana Pérez");

        Zona zona = new Zona();
        zona.setNombre("Primera Sección");
        Lancha lancha = new Lancha();
        lancha.setIdLancha(34L);
        lancha.setNombre("La Aurora");
        lancha.setCapacidad(8);
        lancha.setZonas(Set.of(zona));
        lanchero.setLancha(lancha);

        Documento licencia1 = documento(lanchero, 1L, "LICENCIA", "licencia-1");
        Documento licencia2 = documento(lanchero, 2L, "LICENCIA", "licencia-2");
        Documento seguro = documento(lanchero, 3L, "SEGURO", "seguro");
        Documento fotoLancha = documento(lanchero, 4L, "FOTO_LANCHA", "foto-lancha");
        when(lancheroRepository.findByIdLanchero(12L)).thenReturn(Optional.of(lanchero));
        when(documentoRepository.findByLanchero_IdLancheroOrderByIdDocumentoAsc(12L))
                .thenReturn(List.of(licencia1, licencia2, seguro, fotoLancha));
        when(archivoStorage.generarUrlPrefirmada("licencia-1")).thenReturn("https://r2.example/licencia-1");
        when(archivoStorage.generarUrlPrefirmada("licencia-2")).thenReturn("https://r2.example/licencia-2");
        when(archivoStorage.generarUrlPrefirmada("seguro")).thenReturn("https://r2.example/seguro");
        when(archivoStorage.generarUrlPrefirmada("foto-lancha")).thenReturn("https://r2.example/foto-lancha");

        var detalle = adminService.obtenerDetalle(12L);

        assertEquals(34L, detalle.embarcacion().id());
        assertEquals(List.of("Primera Sección"), detalle.embarcacion().zonas());
        assertEquals(4, detalle.documentos().size());
        assertEquals(List.of(
                new SolicitudDetalleDTO.DocumentoDTO("FOTO_LANCHA", "https://r2.example/foto-lancha"),
                new SolicitudDetalleDTO.DocumentoDTO("LICENCIA", "https://r2.example/licencia-1"),
                new SolicitudDetalleDTO.DocumentoDTO("LICENCIA", "https://r2.example/licencia-2"),
                new SolicitudDetalleDTO.DocumentoDTO("SEGURO", "https://r2.example/seguro")
        ), detalle.documentos());
        verify(archivoStorage).generarUrlPrefirmada("licencia-1");
        verify(archivoStorage).generarUrlPrefirmada("licencia-2");
        verify(archivoStorage).generarUrlPrefirmada("seguro");
        verify(archivoStorage).generarUrlPrefirmada("foto-lancha");
    }

    private Documento documento(Lanchero lanchero, Long id, String tipo, String clave) {
        Documento documento = new Documento();
        documento.setIdDocumento(id);
        documento.setLanchero(lanchero);
        documento.setTipoDocumento(tipo);
        documento.setClaveArchivo(clave);
        return documento;
    }
}
