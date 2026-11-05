package com.nautiflow.service;

import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.exception.SolicitudInvalidaException;
import com.nautiflow.model.Documento;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Zona;
import com.nautiflow.repository.DocumentoRepository;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.RegistroEmailOutboxRepository;
import com.nautiflow.repository.ZonaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LancheroPersistenceServiceTest {
    @Mock private LancheroRepository lancheroRepository;
    @Mock private LanchaRepository lanchaRepository;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private RegistroEmailOutboxRepository registroEmailOutboxRepository;
    @Mock private ZonaRepository zonaRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private LancheroPersistenceService service;

    @BeforeEach
    void setUp() {
        service = new LancheroPersistenceService(
                lancheroRepository,
                lanchaRepository,
                documentoRepository,
                registroEmailOutboxRepository,
                zonaRepository,
                passwordEncoder
        );
        lenient().when(lancheroRepository.existsByCorreo("maria@example.com")).thenReturn(false);
        lenient().when(passwordEncoder.encode("ClaveSegura123")).thenReturn("hash");
        lenient().when(lancheroRepository.save(any(Lanchero.class))).thenAnswer(invocation -> {
            Lanchero lanchero = invocation.getArgument(0);
            lanchero.setIdLanchero(12L);
            return lanchero;
        });
        lenient().when(lanchaRepository.save(any(Lancha.class))).thenAnswer(invocation -> {
            Lancha lancha = invocation.getArgument(0);
            lancha.setIdLancha(5L);
            return lancha;
        });
    }

    @Test
    void registraUnaLanchaYEncolaLaNotificacionDentroDeLaPersistenciaSinLlamarAlProveedor() {
        Zona norte = zona(1L, "Zona Norte");
        Zona centro = zona(3L, "Zona Centro");
        when(zonaRepository.findAllById(List.of(1L, 3L))).thenReturn(List.of(norte, centro));

        var resultado = service.registrar(solicitud(List.of(1L, 3L)), "licencia.webp", "image/webp",
                "seguro.pdf", "application/pdf", "antecedentes.webp", "image/webp", List.of());

        assertEquals(
                Set.of("idLanchero", "idLancha", "estado", "mensaje"),
                new ObjectMapper().valueToTree(resultado).properties().stream()
                        .map(java.util.Map.Entry::getKey)
                        .collect(java.util.stream.Collectors.toSet())
        );
        assertEquals(12L, resultado.idLanchero());
        assertEquals(5L, resultado.idLancha());
        assertEquals("PENDIENTE", resultado.estado());
        assertEquals("Solicitud de registro recibida correctamente.", resultado.mensaje());
        ArgumentCaptor<Lancha> lanchaCaptor = ArgumentCaptor.forClass(Lancha.class);
        verify(lanchaRepository).save(lanchaCaptor.capture());
        assertEquals(Set.of(norte, centro), lanchaCaptor.getValue().getZonas());
        ArgumentCaptor<Documento> documentoCaptor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository, org.mockito.Mockito.times(3)).save(documentoCaptor.capture());
        verify(registroEmailOutboxRepository).encolar(12L, "maria@example.com");
        org.mockito.InOrder ordenPersistencia = org.mockito.Mockito.inOrder(
                documentoRepository, registroEmailOutboxRepository
        );
        ordenPersistencia.verify(documentoRepository, org.mockito.Mockito.times(3)).save(any(Documento.class));
        ordenPersistencia.verify(registroEmailOutboxRepository).encolar(12L, "maria@example.com");
        assertEquals(
                List.of("image/webp", "application/pdf", "image/webp"),
                documentoCaptor.getAllValues().stream().map(Documento::getTipoContenido).toList()
        );
    }

    @Test
    void permiteRegistroConUnaZona() {
        Zona norte = zona(1L, "Zona Norte");
        when(zonaRepository.findAllById(List.of(1L))).thenReturn(List.of(norte));

        service.registrar(solicitud(List.of(1L)), "l.webp", "image/webp",
                "s.pdf", "application/pdf", "a.pdf", "application/pdf", List.of());

        ArgumentCaptor<Lancha> lanchaCaptor = ArgumentCaptor.forClass(Lancha.class);
        verify(lanchaRepository).save(lanchaCaptor.capture());
        assertEquals(Set.of(norte), lanchaCaptor.getValue().getZonas());
        ArgumentCaptor<Lanchero> lancheroCaptor = ArgumentCaptor.forClass(Lanchero.class);
        verify(lancheroRepository).save(lancheroCaptor.capture());
        assertEquals("hash", lancheroCaptor.getValue().getPasswordHash());
    }

    @Test
    void permiteRegistroSinZonasSinCrearZonas() {
        when(zonaRepository.findAllById(List.of())).thenReturn(List.of());

        service.registrar(solicitud(null), "l.pdf", "application/pdf",
                "s.pdf", "application/pdf", "a.pdf", "application/pdf", List.of());

        ArgumentCaptor<Lancha> lanchaCaptor = ArgumentCaptor.forClass(Lancha.class);
        verify(lanchaRepository).save(lanchaCaptor.capture());
        assertEquals(Set.of(), lanchaCaptor.getValue().getZonas());
        verify(zonaRepository).findAllById(List.of());
        verify(zonaRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void rechazaRegistroSinContrasena() {
        when(zonaRepository.findAllById(List.of())).thenReturn(List.of());
        LancheroRegistroDTO solicitud = new LancheroRegistroDTO(
                "María Pérez",
                "+573212684025",
                "maria@example.com",
                null,
                "Brisa Marina",
                8,
                null
        );

        assertThrows(
                SolicitudInvalidaException.class,
                () -> service.registrar(solicitud, "l.pdf", "application/pdf",
                        "s.pdf", "application/pdf", "a.pdf", "application/pdf", List.of())
        );

        verify(lancheroRepository, never()).save(any());
        verifyNoInteractions(lanchaRepository, documentoRepository, passwordEncoder);
    }

    @Test
    void rechazaIdsDeZonaInexistentesAntesDeCrearRegistros() {
        when(zonaRepository.findAllById(List.of(1L, 999L))).thenReturn(List.of(zona(1L, "Zona Norte")));

        assertThrows(
                SolicitudInvalidaException.class,
                () -> service.registrar(solicitud(List.of(1L, 999L)), "l.pdf", "application/pdf",
                        "s.pdf", "application/pdf", "a.pdf", "application/pdf", List.of())
        );

        verifyNoInteractions(lancheroRepository, lanchaRepository, documentoRepository);
    }

    @Test
    void rechazaIdsDuplicadosSinConsultarNiCrearZonas() {
        assertThrows(
                SolicitudInvalidaException.class,
                () -> service.registrar(solicitud(List.of(1L, 1L)), "l.pdf", "application/pdf",
                        "s.pdf", "application/pdf", "a.pdf", "application/pdf", List.of())
        );

        verifyNoInteractions(zonaRepository, lancheroRepository, lanchaRepository, documentoRepository);
    }

    private LancheroRegistroDTO solicitud(List<Long> zonasIds) {
        return new LancheroRegistroDTO(
                "María Pérez",
                "+573212684025",
                "maria@example.com",
                "ClaveSegura123",
                "Brisa Marina",
                8,
                zonasIds
        );
    }

    private Zona zona(Long id, String nombre) {
        Zona zona = new Zona();
        zona.setIdZona(id);
        zona.setNombre(nombre);
        return zona;
    }
}
