package com.nautiflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import com.nautiflow.util.ZonaNegocio;

/**
 * Expone el reloj de negocio en la zona horaria de Argentina.
 */
@Configuration
public class UtcClockConfig {
    /**
     * Crea el reloj de negocio en la zona horaria de Argentina.
     */
    @Bean
    Clock nautiflowClock() {
        return Clock.system(ZonaNegocio.ZONA);
    }
}
