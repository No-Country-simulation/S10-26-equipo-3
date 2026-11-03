package com.nautiflow.service;

import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.dto.RegistroLancheroResponse;
import com.nautiflow.exception.ConflictoRegistroException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LancheroServiceTest {
    private final LancheroPersistenceService persistenceService = mock(LancheroPersistenceService.class);
    private final ArchivoLancheroStorage archivoStorage = mock(ArchivoLancheroStorage.class);
    private final LancheroService service = new LancheroService(
            persistenceService, archivoStorage, Runnable::run
    );

    @Test
    void correoDuplicadoSeRechazaAntesDeSubirArchivos() {
        LancheroRegistroDTO solicitud = new LancheroRegistroDTO(
                "Ana Pérez",
                "+573001234567",
                "ana@example.com",
                "password-segura",
                "Lancha",
                8,
                List.of(1L)
        );
        doThrow(new ConflictoRegistroException("Ya existe un lanchero con ese correo electrónico."))
                .when(persistenceService).validarCorreoDisponible("ana@example.com");

        assertThrows(ConflictoRegistroException.class, () -> service.registrarLanchero(
                solicitud, mock(MultipartFile.class), mock(MultipartFile.class),
                mock(MultipartFile.class), List.of()
        ));

        verify(persistenceService).validarCorreoDisponible("ana@example.com");
        verify(persistenceService, never()).registrar(
                any(), any(), any(), any(), any(), any(), any(), any()
        );
        verifyNoInteractions(archivoStorage);
    }

    @Test
    void esperaATodasLasSubidasYLimpiaLasCompletadasSiUnaFalla() throws Exception {
        MultipartFile licencia = archivo();
        MultipartFile seguro = archivo();
        MultipartFile antecedentes = archivo();
        when(archivoStorage.guardar(licencia, ArchivoLancheroStorage.TipoArchivo.DOCUMENTO, "licencia"))
                .thenReturn(new ArchivoLancheroStorage.ArchivoAlmacenado("licencia", "application/pdf"));
        when(archivoStorage.guardar(seguro, ArchivoLancheroStorage.TipoArchivo.DOCUMENTO, "seguro"))
                .thenThrow(new IOException("fallo simulado"));
        when(archivoStorage.guardar(antecedentes, ArchivoLancheroStorage.TipoArchivo.DOCUMENTO, "antecedentes"))
                .thenReturn(new ArchivoLancheroStorage.ArchivoAlmacenado("antecedentes", "application/pdf"));

        assertThrows(RuntimeException.class, () -> service.registrarLanchero(
                solicitud(), licencia, seguro, antecedentes, List.of()
        ));

        verify(archivoStorage).eliminar(List.of("licencia", "antecedentes"));
        verify(persistenceService, never()).registrar(
                any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void ejecutaSubidasEnParaleloSinPersistirAntesDeQueTodasTerminen() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            LancheroService servicioParalelo =
                    new LancheroService(persistenceService, archivoStorage, executor);
            AtomicInteger activas = new AtomicInteger();
            AtomicInteger maximoConcurrente = new AtomicInteger();
            CountDownLatch dosSubidasActivas = new CountDownLatch(2);
            when(archivoStorage.guardar(any(), eq(ArchivoLancheroStorage.TipoArchivo.DOCUMENTO), anyString()))
                    .thenAnswer(invocation -> {
                        int activasAhora = activas.incrementAndGet();
                        maximoConcurrente.accumulateAndGet(activasAhora, Math::max);
                        dosSubidasActivas.countDown();
                        try {
                            assertTrue(dosSubidasActivas.await(5, TimeUnit.SECONDS));
                            return new ArchivoLancheroStorage.ArchivoAlmacenado(
                                    invocation.getArgument(2, String.class),
                                    "application/pdf"
                            );
                        } finally {
                            activas.decrementAndGet();
                        }
                    });
            when(persistenceService.registrar(
                    any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList()
            )).thenReturn(new RegistroLancheroResponse(12L, 5L, "PENDIENTE",
                    "Solicitud de registro recibida correctamente."));

            RegistroLancheroResponse response = servicioParalelo.registrarLanchero(
                    solicitud(), archivo(), archivo(), archivo(), new ArrayList<>()
            );

            assertEquals(12L, response.idLanchero());
            assertTrue(maximoConcurrente.get() >= 2);
            verify(persistenceService).registrar(
                    any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList()
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private LancheroRegistroDTO solicitud() {
        return new LancheroRegistroDTO(
                "Ana Pérez", "+573001234567", "ana@example.com", "password-segura", "Lancha", 8, List.of(1L)
        );
    }

    private MultipartFile archivo() {
        return mock(MultipartFile.class);
    }
}
