package com.nautiflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Expone el reloj de negocio. Aunque la clase se llama Utc, el reloj se configura en la zona America/Bogota porque las fechas de negocio se calculan en hora colombiana.
 */
@Configuration
public class UtcClockConfig {
    /**
     * Crea el reloj de negocio en la zona horaria America/Bogota para calcular fechas locales.
     */
    @Bean
    Clock nautiflowClock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}
