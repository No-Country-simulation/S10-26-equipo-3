package com.nautiflow.util;

import java.time.ZoneId;

/** Zona horaria oficial para fechas y horarios de negocio. */
public final class ZonaNegocio {
    public static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private ZonaNegocio() {
    }
}
