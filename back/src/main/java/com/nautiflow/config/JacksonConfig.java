package com.nautiflow.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nautiflow.util.FechaHoraUtc;
import com.nautiflow.util.ZonaNegocio;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

/**
 * Configura la serialización JSON de las fechas en la zona horaria de Argentina.
 */
@Configuration
public class JacksonConfig {
    private static final DateTimeFormatter FORMATO_NEGOCIO_MICROSEGUNDOS = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true)
            .appendOffset("+HH:MM", "Z")
            .toFormatter(Locale.ROOT);

    /**
     * Crea el ObjectMapper de Jackson 2 con un serializador propio para OffsetDateTime en hora argentina.
     */
    @Bean
    ObjectMapper objectMapper() {
        SimpleModule moduloFechasNegocio = new SimpleModule("nautiflow-business-datetime");
        moduloFechasNegocio.addSerializer(OffsetDateTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(OffsetDateTime value, JsonGenerator generator,
                                  SerializerProvider serializers) throws IOException {
                generator.writeString(FORMATO_NEGOCIO_MICROSEGUNDOS.format(enZonaNegocio(value)));
            }
        });

        return new ObjectMapper()
                .findAndRegisterModules()
                .registerModule(moduloFechasNegocio)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Personaliza el JsonMapper de Jackson 3 que usa Spring Boot para serializar fechas en hora argentina.
     */
    @Bean
    JsonMapperBuilderCustomizer businessZoneJsonMapperCustomizer() {
        return builder -> builder.addModule(businessZoneDateTimeModule());
    }

    /**
     * Construye el módulo de Jackson 3 que serializa OffsetDateTime con seis decimales y su offset argentino.
     */
    private tools.jackson.databind.JacksonModule businessZoneDateTimeModule() {
        tools.jackson.databind.module.SimpleModule moduloFechasNegocio =
                new tools.jackson.databind.module.SimpleModule("nautiflow-business-datetime");
        moduloFechasNegocio.addSerializer(OffsetDateTime.class,
                new tools.jackson.databind.ser.std.StdScalarSerializer<>(OffsetDateTime.class) {
                    @Override
                    public void serialize(OffsetDateTime value, tools.jackson.core.JsonGenerator generator,
                                          tools.jackson.databind.SerializationContext context)
                            throws tools.jackson.core.JacksonException {
                        generator.writeString(FORMATO_NEGOCIO_MICROSEGUNDOS.format(enZonaNegocio(value)));
                    }
                });
        return moduloFechasNegocio;
    }

    private static OffsetDateTime enZonaNegocio(OffsetDateTime value) {
        return FechaHoraUtc.normalizar(value).atZoneSameInstant(ZonaNegocio.ZONA).toOffsetDateTime();
    }
}
