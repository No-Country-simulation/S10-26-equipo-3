package com.nautiflow.util;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class FechaHoraUtc {
    private FechaHoraUtc() {
    }

    public static OffsetDateTime ahora() {
        return ahora(Clock.systemUTC());
    }

    public static OffsetDateTime ahora(Clock clock) {
        return normalizar(OffsetDateTime.now(clock));
    }

    public static OffsetDateTime normalizar(OffsetDateTime fechaHora) {
        Instant instante = fechaHora.toInstant();
        long nanosRedondeados = ((instante.getNano() + 500L) / 1_000L) * 1_000L;
        return OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(instante.getEpochSecond(), nanosRedondeados),
                ZoneOffset.UTC
        );
    }
}
