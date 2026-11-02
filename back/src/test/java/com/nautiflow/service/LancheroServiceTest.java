package com.nautiflow.service;

import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.exception.ConflictoRegistroException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LancheroServiceTest {
    private final LancheroPersistenceService persistenceService = mock(LancheroPersistenceService.class);
    private final ArchivoLancheroStorage archivoStorage = mock(ArchivoLancheroStorage.class);
    private final RegistroNotificacionService notificacionService = mock(RegistroNotificacionService.class);
    private final LancheroService service =
            new LancheroService(persistenceService, archivoStorage, notificacionService);

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
        verifyNoInteractions(archivoStorage, notificacionService);
    }
}
