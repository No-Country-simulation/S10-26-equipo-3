package com.nautiflow.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nautiflow.util.FechaHoraUtc;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

@Configuration
public class JacksonConfig {
    private static final DateTimeFormatter FORMATO_UTC_MICROSEGUNDOS = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 6, 6, true)
            .appendLiteral('Z')
            .toFormatter(Locale.ROOT);

    @Bean
    ObjectMapper objectMapper() {
        SimpleModule moduloFechasUtc = new SimpleModule("nautiflow-utc-datetime");
        moduloFechasUtc.addSerializer(OffsetDateTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(OffsetDateTime value, JsonGenerator generator,
                                  SerializerProvider serializers) throws IOException {
                generator.writeString(FORMATO_UTC_MICROSEGUNDOS.format(FechaHoraUtc.normalizar(value)));
            }
        });

        return new ObjectMapper()
                .findAndRegisterModules()
                .registerModule(moduloFechasUtc)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    JsonMapperBuilderCustomizer utcJsonMapperCustomizer() {
        return builder -> builder.addModule(utcDateTimeModule());
    }

    private tools.jackson.databind.JacksonModule utcDateTimeModule() {
        tools.jackson.databind.module.SimpleModule moduloFechasUtc =
                new tools.jackson.databind.module.SimpleModule("nautiflow-utc-datetime");
        moduloFechasUtc.addSerializer(OffsetDateTime.class,
                new tools.jackson.databind.ser.std.StdScalarSerializer<>(OffsetDateTime.class) {
                    @Override
                    public void serialize(OffsetDateTime value, tools.jackson.core.JsonGenerator generator,
                                          tools.jackson.databind.SerializationContext context)
                            throws tools.jackson.core.JacksonException {
                        generator.writeString(FORMATO_UTC_MICROSEGUNDOS.format(FechaHoraUtc.normalizar(value)));
                    }
                });
        return moduloFechasUtc;
    }
}
