package com.nautiflow;

import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.RegistroEmailOutboxRepository;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.model.EstadoSolicitud;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class LanchaRepositoryAvailabilityTest {
    @Autowired
    private LanchaRepository lanchaRepository;

    @Autowired
    private RegistroEmailOutboxRepository registroEmailOutboxRepository;

    @Test
    void availabilityQueryExecutesAgainstConfiguredDatabase() {
        OffsetDateTime fechaHoraConsulta = OffsetDateTime.now(ZoneOffset.UTC);
        assertNotNull(lanchaRepository.buscarDisponibles(
                null,
                null,
                null,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                fechaHoraConsulta
        ));
        assertNotNull(lanchaRepository.buscarDisponibles(
                4,
                1L,
                2L,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                fechaHoraConsulta
        ));
    }

    @Test
    @Transactional
    void outboxClaimQueryExecutesWithinRollbackTransaction() {
        registroEmailOutboxRepository.reclamarSiguiente(5, 120);
    }
}
