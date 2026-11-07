package com.nautiflow.service;

import com.nautiflow.dto.LanchaDestacadaDTO;
import com.nautiflow.dto.PaginaLanchasDestacadasDTO;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.repository.DocumentoRepository;
import com.nautiflow.repository.LanchaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanchasDestacadasServiceTest {
    @Mock
    private LanchaRepository lanchaRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private ArchivoLancheroStorage archivoStorage;

    @InjectMocks
    private LanchasDestacadasService service;

    @Test
    void listaLanchasConPromedioResenasYFotoPrincipalSinExponerOtrosDatos() {
        LanchaRepository.DestacadaProjection calificada = lancha(
                7L, 70L, "La Porteña III", "Juan Pablo Gómez González", 4.0, 12L
        );
        LanchaRepository.DestacadaProjection sinResenas = lancha(
                8L, 80L, "Brisa Marina", "Carlos Pérez", null, 0L
        );
        DocumentoRepository.FotoLanchaProjection fotoPrincipal = foto("nautiflow/fotos/primera.webp");
        DocumentoRepository.FotoLanchaProjection fotoSecundaria = foto("nautiflow/fotos/segunda.webp");

        when(lanchaRepository.buscarDestacadas(EstadoSolicitud.APROBADO, PageRequest.of(0, 10)))
                .thenReturn(List.of(calificada, sinResenas));
        when(lanchaRepository.countByLanchero_EstadoSolicitud(EstadoSolicitud.APROBADO)).thenReturn(12L);
        when(documentoRepository.buscarFotosLancha(List.of(70L, 80L)))
                .thenReturn(List.of(fotoPrincipal, fotoSecundaria));
        when(archivoStorage.generarUrlPrefirmada("nautiflow/fotos/primera.webp"))
                .thenReturn("https://storage.example/primera.webp");

        PaginaLanchasDestacadasDTO resultado = service.listar();

        assertEquals(2, resultado.content().size());
        LanchaDestacadaDTO primera = resultado.content().get(0);
        assertEquals(7L, primera.idLancha());
        assertEquals("La Porteña III", primera.nombreEmbarcacion());
        assertEquals("Juan Pablo Gómez González", primera.lancheroNombreCompleto());
        assertEquals("https://storage.example/primera.webp", primera.fotoPrincipalUrl());
        assertEquals(4.0, primera.promedioCalificacion());
        assertEquals(12L, primera.totalResenas());
        assertTrue(primera.verificada());

        LanchaDestacadaDTO nueva = resultado.content().get(1);
        assertNull(nueva.promedioCalificacion());
        assertEquals(0L, nueva.totalResenas());
        assertNull(nueva.fotoPrincipalUrl());
        assertTrue(nueva.verificada());
        assertEquals(0, resultado.page());
        assertEquals(10, resultado.size());
        assertEquals(12L, resultado.totalElements());
        assertEquals(2, resultado.totalPages());

        verify(lanchaRepository).buscarDestacadas(EstadoSolicitud.APROBADO, PageRequest.of(0, 10));
        verify(documentoRepository).buscarFotosLancha(List.of(70L, 80L));
        verify(archivoStorage).generarUrlPrefirmada("nautiflow/fotos/primera.webp");
    }

    private LanchaRepository.DestacadaProjection lancha(
            Long idLancha,
            Long idLanchero,
            String nombre,
            String nombreLanchero,
            Double promedio,
            Long totalResenas
    ) {
        LanchaRepository.DestacadaProjection lancha = mock(LanchaRepository.DestacadaProjection.class);
        when(lancha.getIdLancha()).thenReturn(idLancha);
        when(lancha.getIdLanchero()).thenReturn(idLanchero);
        when(lancha.getNombreEmbarcacion()).thenReturn(nombre);
        when(lancha.getLancheroNombreCompleto()).thenReturn(nombreLanchero);
        when(lancha.getPromedioCalificacion()).thenReturn(promedio);
        when(lancha.getTotalResenas()).thenReturn(totalResenas);
        return lancha;
    }

    private DocumentoRepository.FotoLanchaProjection foto(String clave) {
        DocumentoRepository.FotoLanchaProjection foto = mock(DocumentoRepository.FotoLanchaProjection.class);
        when(foto.getIdLanchero()).thenReturn(70L);
        when(foto.getClaveArchivo()).thenReturn(clave);
        return foto;
    }
}
