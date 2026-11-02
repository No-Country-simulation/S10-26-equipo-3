package com.nautiflow;

import com.nautiflow.repository.LanchaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class LanchaRepositoryAvailabilityTest {
    @Autowired
    private LanchaRepository lanchaRepository;

    @Test
    void availabilityQueryExecutesAgainstConfiguredDatabase() {
        OffsetDateTime fechaHoraConsulta = OffsetDateTime.now(ZoneOffset.UTC);
        assertNotNull(lanchaRepository.buscarDisponibles(
                null,
                null,
                null,
                fechaHoraConsulta
        ));
        assertNotNull(lanchaRepository.buscarDisponibles(
                4,
                1L,
                2L,
                fechaHoraConsulta
        ));
    }
}
