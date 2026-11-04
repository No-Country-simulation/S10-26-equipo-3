package com.nautiflow.util;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Utilidad para obtener y normalizar fechas en UTC con precisión de microsegundos.
 */
public final class FechaHoraUtc {
    /**
     * Evita instanciar la clase; solo se usan sus métodos estáticos.
     */
    private FechaHoraUtc() {
    }

    /**
     * Obtiene la fecha y hora actual en UTC.
     */
    public static OffsetDateTime ahora() {
        return ahora(Clock.systemUTC());
    }

    /**
     * Obtiene la fecha y hora actual usando el reloj indicado y la normaliza a UTC.
     */
    public static OffsetDateTime ahora(Clock clock) {
        return normalizar(OffsetDateTime.now(clock));
    }

    /**
     * Convierte una fecha a UTC y la redondea a microsegundos.
     */
    public static OffsetDateTime normalizar(OffsetDateTime fechaHora) {
        Instant instante = fechaHora.toInstant();
        long nanosRedondeados = ((instante.getNano() + 500L) / 1_000L) * 1_000L;
        return OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(instante.getEpochSecond(), nanosRedondeados),
                ZoneOffset.UTC
        );
    }
}
