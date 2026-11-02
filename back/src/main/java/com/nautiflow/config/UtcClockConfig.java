package com.nautiflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class UtcClockConfig {
    @Bean
    Clock nautiflowClock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}
